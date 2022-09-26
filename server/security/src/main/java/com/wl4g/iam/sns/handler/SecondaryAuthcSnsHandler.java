/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.iam.sns.handler;

import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_AFTER_CALLBACK_AGENT;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_SNS_BASE;
import static com.wl4g.iam.common.model.SecondaryAuthcValidateModel.Status.IllegalAuthorizer;
import static com.wl4g.iam.common.model.SecondaryAuthcValidateModel.Status.InvalidAuthorizer;
import static com.wl4g.infra.context.web.BaseController.REDIRECT_PREFIX;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.Assert;

import com.google.common.base.Splitter;
import com.wl4g.iam.common.model.SecondaryAuthcValidateModel;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.common.subject.IamPrincipal.Parameter;
import com.wl4g.iam.common.subject.IamPrincipal.SnsAuthorizingParameter;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.config.properties.SnsProperties;
import com.wl4g.iam.configure.ServerSecurityConfigurer;
import com.wl4g.iam.core.authc.SecondaryAuthenticationException;
import com.wl4g.iam.core.cache.CacheKey;
import com.wl4g.iam.core.config.AbstractIamProperties.Which;
import com.wl4g.iam.sns.OAuth2ApiBinding;
import com.wl4g.iam.sns.OAuth2ApiBindingFactory;
import com.wl4g.iam.sns.support.Oauth2AccessToken;
import com.wl4g.iam.sns.support.Oauth2OpenId;
import com.wl4g.infra.common.lang.Assert2;
import com.wl4g.infra.common.web.WebUtils2;

/**
 * Secondary authentication SNS handler
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2019年2月24日
 * @since
 */
public class SecondaryAuthcSnsHandler extends AbstractSnsHandler {

    /**
     * Secondary authentication cache name
     */
    final public static String SECOND_AUTHC_CACHE = "second_auth_";

    public SecondaryAuthcSnsHandler(IamProperties config, SnsProperties snsConfig, OAuth2ApiBindingFactory connectFactory,
            ServerSecurityConfigurer context) {
        super(config, snsConfig, connectFactory, context);
    }

    @Override
    public String doGetAuthorizingUrl(Which which, String provider, String state, Map<String, String> connectParams) {
        // Connecting
        String authorizingUrl = super.doGetAuthorizingUrl(which, provider, state, connectParams);

        // Save connect parameters
        saveOauth2ConnectParameters(provider, state, connectParams);

        return REDIRECT_PREFIX + authorizingUrl;
    }

    @Override
    protected void checkConnectParameters(String provider, String state, Map<String, String> connectParams) {
        super.checkConnectParameters(provider, state, connectParams);

        // Check connect parameters
        Assert.notEmpty(connectParams, "Connect parameters must not be empty");

        // Check 'application'
        Assert.hasText(connectParams.get(config.getParam().getApplication()),
                String.format("'%s' must not be empty", config.getParam().getApplication()));

        // Check 'authorizers'
        Assert.hasText(connectParams.get(config.getParam().getAuthorizers()),
                String.format("'%s' must not be empty", config.getParam().getAuthorizers()));

        // Check 'agent'
        String agentKey = config.getParam().getAgent();
        String agentValue = connectParams.get(agentKey);
        // hasText(agentValue, format("Parmam '%s' value is required",
        // agentKey));
        Assert2.state(WebUtils2.isTrue(agentValue), format("Param %s current supports only enabled mode", agentKey));

        // Check 'funcId'
        Assert.hasText(connectParams.get(config.getParam().getFuncId()),
                String.format("'%s' must not be empty", config.getParam().getFuncId()));
    }

    @Override
    protected void checkCallbackParameters(String provider, String state, String code, Map<String, String> connectParams) {
        // Check 'state'
        Assert.notNull(connectParams, String.format("State '%s' is invalid or expired", state));

        super.checkCallbackParameters(provider, state, code, connectParams);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected String doGetAuthInfo(
            String provider,
            String code,
            OAuth2ApiBinding connect,
            Map<String, String> connectParams,
            HttpServletRequest request) {
        // Got required parameters
        String sourceApp = connectParams.get(config.getParam().getApplication());
        // Authorizers
        String authorizers = connectParams.get(config.getParam().getAuthorizers());
        // Access token
        Oauth2AccessToken ast = connect.getAccessToken(code);
        // User openId
        Oauth2OpenId openId = connect.getUserOpenId(ast);

        // Account by openId
        Parameter parameter = new SnsAuthorizingParameter(provider, openId.openId(), openId.unionId());
        IamPrincipal account = configurer.getIamUserDetail(parameter);

        // Second authentication assertion
        SecondaryAuthcValidateModel model = new SecondaryAuthcValidateModel(sourceApp, provider,
                connectParams.get(config.getParam().getFuncId()));
        try {
            // Assertion
            assertionSecondAuthentication(provider, openId, account, authorizers, connectParams);
            model.setPrincipal(account.getPrincipal());
            model.setValidFromDate(new Date());
        } catch (SecondaryAuthenticationException e) {
            log.error("Secondary authentication fail", e);
            model.setStatus(e.getStatus());
            model.setErrdesc(e.getMessage());
        }

        /*
         * Save authenticated to cache.
         * See:xx.iam.handler.DefaultAuthenticationHandler#secondValidate()
         */
        String secondAuthCode = generateSecondaryAuthcCode(sourceApp);
        CacheKey ekey = new CacheKey(secondAuthCode, snsConfig.getOauth2ConnectExpireMs());
        cacheManager.getIamCache(SECOND_AUTHC_CACHE).put(ekey, model);
        log.info("Saved secondary authentication. {}[{}], result[{}]", config.getParam().getSecondaryAuthCode(), secondAuthCode,
                model);

        return secondAuthCode;
    }

    @Override
    protected String postCallbackResponse(
            String provider,
            String secondAuthCode,
            Map<String, String> connectParams,
            HttpServletRequest request) {
        return secondAuthCode;
    }

    @Override
    protected String decorateCallbackRefreshUrl(
            String secondAuthCode,
            Map<String, String> connectParams,
            HttpServletRequest request) {
        StringBuffer url = new StringBuffer(WebUtils2.getRFCBaseURI(request, true));
        url.append(URI_IAM_SERVER_SNS_BASE).append("/");
        url.append(URI_IAM_SERVER_AFTER_CALLBACK_AGENT).append("?");
        url.append(config.getParam().getSecondaryAuthCode()).append("=");
        url.append(secondAuthCode);
        return url.toString();
    }

    @Override
    public Which which() {
        return Which.SECOND_AUTH;
    }

    private void assertionSecondAuthentication(
            String provider,
            Oauth2OpenId openId,
            IamPrincipal account,
            String authorizers,
            Map<String, String> connectParams) {
        // Check authorizer effectiveness
        if (isNull(account) || isBlank(account.getPrincipal())) {
            throw new SecondaryAuthenticationException(InvalidAuthorizer, format("Invalid authorizer, openId info[%s]", openId));
        }
        // Check authorizer matches
        else {
            List<String> authorizerList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(authorizers);
            if (!authorizerList.contains(account.getPrincipal())) {
                throw new SecondaryAuthenticationException(IllegalAuthorizer, String.format(
                        "Illegal authorizer, Please use [%s] account authorization bound by user [%s]", provider, authorizers));
            }
        }
    }

    /**
     * Generate second authentication code
     *
     * @param application
     * @return
     */
    private String generateSecondaryAuthcCode(String application) {
        return "sec_".concat(RandomStringUtils.randomAlphanumeric(32));
    }

}