/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;

import com.wl4g.infra.common.web.WebUtils2;

import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.Exceptions.getRootCausesString;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.web.WebUtils2.getRFCBaseURI;
import static com.wl4g.infra.common.web.WebUtils2.safeEncodeURL;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.BEAN_SESSION_RESOURCE_MSG_BUNDLER;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.CACHE_PREFIX_IAM_SNSAUTH;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_AFTER_CALLBACK_AGENT;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_SNS_BASE;
import static com.wl4g.iam.filter.AbstractServerIamAuthenticationFilter.URI_BASE_MAPPING;
import static com.wl4g.iam.sns.web.AbstractSnsController.KEY_SNS_CALLBACK_PARAMS;
import static com.wl4g.iam.sns.web.AbstractSnsController.PARAM_SNS_CALLBACK_ID;
import static com.wl4g.iam.sns.web.AbstractSnsController.PARAM_SNS_CODE;
import static com.wl4g.iam.sns.web.AbstractSnsController.PARAM_SNS_PRIVIDER;

import com.wl4g.iam.common.bean.SocialAuthorizeInfo;
import com.wl4g.iam.common.i18n.SessionResourceMessageBundler;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.config.properties.SnsProperties;
import com.wl4g.iam.configure.ServerSecurityConfigurer;
import com.wl4g.iam.core.cache.CacheKey;
import com.wl4g.iam.core.cache.IamCacheManager;
import com.wl4g.iam.core.config.AbstractIamProperties.Which;
import com.wl4g.iam.core.exception.SnsApiBindingException;
import com.wl4g.iam.filter.ProviderSupport;
import com.wl4g.iam.sns.CallbackResult;
import com.wl4g.iam.sns.OAuth2ApiBinding;
import com.wl4g.iam.sns.OAuth2ApiBindingFactory;
import com.wl4g.iam.sns.support.Oauth2AccessToken;
import com.wl4g.iam.sns.support.Oauth2OpenId;
import com.wl4g.iam.sns.support.Oauth2UserProfile;

/**
 * Abstract based social networking services handler
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2019年1月7日
 * @since
 */
public abstract class AbstractSnsHandler implements SnsHandler {

    protected final Logger log = getLogger(getClass());

    /**
     * IAM server properties configuration
     */
    protected final IamProperties config;

    /**
     * SNS properties configuration
     */
    protected final SnsProperties snsConfig;

    /**
     * IAM Social connection factory
     */
    protected final OAuth2ApiBindingFactory factory;

    /**
     * IAM security context handler
     */
    protected final ServerSecurityConfigurer configurer;

    /**
     * Enhanced cache manager.
     */
    @Autowired
    protected IamCacheManager cacheManager;

    /**
     * Delegate message source.
     */
    @Resource(name = BEAN_SESSION_RESOURCE_MSG_BUNDLER)
    protected SessionResourceMessageBundler bundle;

    public AbstractSnsHandler(IamProperties config, SnsProperties snsConfig, OAuth2ApiBindingFactory connectFactory,
            ServerSecurityConfigurer configurer) {
        this.config = notNullOf(config, "config");
        this.snsConfig = notNullOf(snsConfig, "snsConfig");
        this.factory = notNullOf(connectFactory, "connectFactory");
        this.configurer = notNullOf(configurer, "configurer");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public String doGetAuthorizingUrl(Which which, String provider, String state, Map<String, String> connectParams) {
        // Check parameters
        checkConnectParameters(provider, state, connectParams);

        // Provider sns connection
        OAuth2ApiBinding connect = factory.getApiBinding(provider);

        // Get authorizeUrl parameters
        Map<String, String> queryParams = getAuthorizeUrlQueryParams(which, provider, state, connectParams);

        // Build URL
        String authorizingUrl = connect.getAuthorizeCodeUrl(state, queryParams);
        log.info("SNS connect provider[{}], state[{}], authorizingUrl[{}]", provider, state, authorizingUrl);
        return authorizingUrl;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public CallbackResult doCallback(Which which, String provider, String state, String code, HttpServletRequest request) {
        try {
            // Connect parameters
            Map<String, String> connectParams = getOauth2ConnectParameters(state, request);

            // Check parameters
            checkCallbackParameters(provider, state, code, connectParams);

            // Provider connection
            OAuth2ApiBinding api = factory.getApiBinding(provider);

            // Do callback
            String result = doGetAuthInfo(provider, code, api, connectParams, request);

            // Build response message
            String msg = postCallbackResponse(provider, result, connectParams, request);

            return new CallbackResult(decorateCallbackRefreshUrl(msg, connectParams, request));
        } finally {
            cleanup(state);
        }
    }

    /**
     * Check connect required parameters
     *
     * @param provider
     * @param refreshUrl
     */
    protected void checkConnectParameters(String provider, String state, Map<String, String> connectParams) {
        ProviderSupport.checkSupport(provider);
        hasTextOf(state, config.getParam().getState());
    }

    /**
     * Check connect callback required parameters
     *
     * @param provider
     * @param state
     * @param code
     */
    protected void checkCallbackParameters(String provider, String state, String code, Map<String, String> connectParams) {
        checkConnectParameters(provider, state, connectParams);
        hasTextOf(code, PARAM_SNS_CODE);
    }

    /**
     * Get authorize URL query parameters
     *
     * @param which
     * @param provider
     * @param state
     * @param connectParams
     * @return
     */
    protected Map<String, String> getAuthorizeUrlQueryParams(
            Which which,
            String provider,
            String state,
            Map<String, String> connectParams) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(config.getParam().getWhich(), which().name().toLowerCase());
        return queryParams;
    }

    /**
     * Save oauth2 connect parameters
     *
     * @param which
     * @param provider
     * @param state
     * @param connectParams
     */
    protected void saveOauth2ConnectParameters(String provider, String state, Map<String, String> connectParams) {
        // Basic parameters
        Map<String, String> totalConnectParams = new HashMap<>();
        totalConnectParams.put(PARAM_SNS_PRIVIDER, provider);
        totalConnectParams.put(config.getParam().getWhich(), connectParams.remove(config.getParam().getWhich()));
        totalConnectParams.put(config.getParam().getState(), connectParams.remove(config.getParam().getState()));

        // Check override parameters
        if (connectParams != null && !connectParams.isEmpty()) {
            connectParams.forEach((key, value) -> {
                hasText(key, "Empty parameter names are not allowed.");
                hasText(value, format("No empty parameter allowed, name '%s'", key));
                if (nonNull(totalConnectParams.putIfAbsent(key, value))) {
                    throw new IllegalStateException(String.format("Override parameter '%s' is not supported.", key));
                }
            });
        }

        // Save to cache
        cacheManager.getIamCache(CACHE_PREFIX_IAM_SNSAUTH).put(
                new CacheKey(KEY_SNS_CONNECT_PARAMS.concat(state), snsConfig.getOauth2ConnectExpireMs()), totalConnectParams);
    }

    /**
     * Get oauth2 connect parameters
     *
     * @param state
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    protected Map<String, String> getOauth2ConnectParameters(String state, HttpServletRequest request) {
        return (Map<String, String>) cacheManager.getIamCache(CACHE_PREFIX_IAM_SNSAUTH)
                .get(new CacheKey(KEY_SNS_CONNECT_PARAMS.concat(state), HashMap.class));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected String doGetAuthInfo(
            String provider,
            String code,
            OAuth2ApiBinding api,
            Map<String, String> connectParams,
            HttpServletRequest request) {

        // Gets access_token
        Oauth2AccessToken at = api.getAccessToken(code);

        // Gets user openId
        Oauth2OpenId openId = api.getUserOpenId(at);

        // Gets user info
        Map<String, Object> userProfile = emptyMap();
        try {
            Oauth2UserProfile profile = api.getUserInfo(at.accessToken(), openId.openId());
            userProfile = BeanMap.create(profile);
        } catch (SnsApiBindingException e) { // Ignore?
            log.warn("Could't get OAuth2 userInfo, provider: %s, accessToken: %s, openId: %s, caused by: %s", provider,
                    at.accessToken(), openId.openId(), getRootCausesString(e));
        }

        // Caching social callback user openId information.(Applicable only to
        // which=login|client_auth),
        // see:Oauth2SnsAuthorizingRealm#doAuthenticationInfo()
        SocialAuthorizeInfo authInfo = new SocialAuthorizeInfo(provider, openId.openId(), openId.unionId(), userProfile);
        String callbackId = generateCallbackId();
        cacheManager.getIamCache(CACHE_PREFIX_IAM_SNSAUTH).put(new CacheKey(getOAuth2CallbackKey(callbackId), 30), authInfo);
        return callbackId;
    }

    /**
     * Post response message.(Default:Refresh redirection URL)
     *
     * @param provider
     * @param result
     * @param connectParams
     * @param request
     * @return
     */
    protected abstract String postCallbackResponse(
            String provider,
            String result,
            Map<String, String> connectParams,
            HttpServletRequest request);

    /**
     * Get login submit URL. <br/>
     * Synchronized at com.wl4g.devops.iam.common.filter.Iam
     * AuthenticationFilter#getUriMapping <br/>
     *
     * <font color=red>Note: Social network login does not require login
     * account(principal)</font>
     *
     * @param provider
     *            SNS connect provider name
     * @param callbackId
     *            Oauth2 callback process id
     * @param request
     * @return
     */
    protected String getLoginSubmitUrl(String provider, String callbackId, HttpServletRequest request) {
        hasTextOf(callbackId, PARAM_SNS_CALLBACK_ID);
        notNull(request, "'request' must not be null");

        /*
         * xx.filter.Oauth2SnsAuthenticationFilter#createAuthenticationToken()
         */
        StringBuffer loginSubmissionUrl = new StringBuffer(WebUtils2.getRFCBaseURI(request, true));
        loginSubmissionUrl.append(URI_BASE_MAPPING).append(provider).append("?");
        loginSubmissionUrl.append(PARAM_SNS_CALLBACK_ID).append("=").append(callbackId);
        return loginSubmissionUrl.toString();
    }

    /**
     * Decorate sns callback response message.(Default: Wrap after callback
     * agent refresh redirection URL)
     *
     * @param refreshUrl
     *            Default: Refresh redirection URL, or other message
     * @param connectParams
     *            SNS connect parameters
     * @return
     */
    protected String decorateCallbackRefreshUrl(
            String refreshUrl,
            Map<String, String> connectParams,
            HttpServletRequest request) {
        hasTextOf(refreshUrl, "refreshUrl");

        /*
         * When using agent jumps, you need to code to pass parameters to the
         * past. See:this#afterCallbackAgent()
         */
        if (nonNull(connectParams) && WebUtils2.isTrue(connectParams.get(config.getParam().getAgent()))) {
            StringBuffer url = new StringBuffer(getRFCBaseURI(request, true));
            url.append(URI_IAM_SERVER_SNS_BASE).append("/");
            url.append(URI_IAM_SERVER_AFTER_CALLBACK_AGENT).append("?");
            url.append(config.getParam().getRefreshUrl()).append("=");
            url.append(safeEncodeURL(refreshUrl));
            refreshUrl = url.toString();
        }
        return refreshUrl;
    }

    /**
     * Release cleanup oauth2 connect temporary parameters.
     *
     * @param state
     */
    private void cleanup(String state) {
        hasTextOf(state, config.getParam().getState());
        cacheManager.getIamCache(CACHE_PREFIX_IAM_SNSAUTH).remove(new CacheKey(KEY_SNS_CONNECT_PARAMS + state));
    }

    /**
     * Generate callback-id
     *
     * @return
     */
    private String generateCallbackId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * Get OAuth2 callbackId key.
     * 
     * @param callbackId
     * @return
     */
    public static String getOAuth2CallbackKey(String callbackId) {
        return KEY_SNS_CALLBACK_PARAMS + callbackId;
    }

}