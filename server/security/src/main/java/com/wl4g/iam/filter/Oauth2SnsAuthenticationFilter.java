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
package com.wl4g.iam.filter;

import static com.wl4g.iam.common.constant.FastCasIAMConstants.CACHE_PREFIX_IAM_SNSAUTH;
import static com.wl4g.iam.sns.web.AbstractSnsController.PARAM_SNS_CALLBACK_ID;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.state;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.infra.common.web.WebUtils2.writeJson;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.apache.shiro.web.util.WebUtils.getCleanParam;
import static org.apache.shiro.web.util.WebUtils.issueRedirect;
import static org.apache.shiro.web.util.WebUtils.toHttp;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ResolvableType;

import com.wl4g.iam.authc.Oauth2SnsAuthenticationToken;
import com.wl4g.iam.authc.ServerIamAuthenticationToken.RedirectInfo;
import com.wl4g.iam.common.bean.SocialAuthorizeInfo;
import com.wl4g.iam.core.cache.CacheKey;
import com.wl4g.iam.core.exception.Oauth2Exception;
import com.wl4g.iam.sns.handler.AbstractSnsHandler;
import com.wl4g.infra.common.web.rest.RespBase;

/**
 * SNS oauth2 authentication abstract filter
 *
 * @param <T>
 * @author wangl.sir
 * @version v1.0 2019年1月8日
 * @since
 */
public abstract class Oauth2SnsAuthenticationFilter<T extends Oauth2SnsAuthenticationToken>
        extends AbstractServerIamAuthenticationFilter<T> implements InitializingBean {

    /**
     * Oauth2 authentication token constructor.
     */
    private Constructor<T> authenticationTokenConstructor;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            initialize();
            state(authenticationTokenConstructor != null, "'authenticationTokenConstructor' is null");
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        // Add supported SNS provider
        if (enabled()) {
            ProviderSupport.addSupport(getName());
        }
    }

    @Override
    protected T doCreateToken(
            String remoteHost,
            RedirectInfo redirectInfo,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String callbackId = hasTextOf(getCleanParam(request, PARAM_SNS_CALLBACK_ID), PARAM_SNS_CALLBACK_ID);
        String callbackKey = AbstractSnsHandler.getOAuth2CallbackKey(callbackId);
        try {
            // from sns oauth2 callback authorized info,
            SocialAuthorizeInfo authorizedInfo = (SocialAuthorizeInfo) cacheManager.getIamCache(CACHE_PREFIX_IAM_SNSAUTH)
                    .get(new CacheKey(callbackKey, SocialAuthorizeInfo.class));

            // Create authentication token instance
            return authenticationTokenConstructor.newInstance(remoteHost, redirectInfo, authorizedInfo);
        } finally { // Cleanup temporary OAuth2 info.
            cacheManager.getIamCache(CACHE_PREFIX_IAM_SNSAUTH).remove(new CacheKey(callbackKey));
        }
    }

    protected boolean onLoginFailure(
            AuthenticationToken token,
            AuthenticationException ae,
            ServletRequest request,
            ServletResponse response) {

        Throwable rootex = getRootCause(ae);
        if (rootex instanceof Oauth2Exception) {
            // When the bound information is not obtained through the user
            // information of oauth2, redirect to the binding page or
            // response json.
            // TODO
            // TODO
            // TODO
            String redirect_uri = config.getBindingUri();
            Map<String, String> params = new HashMap<>();
            params.put("", "");
            if (isJSONResponse(request)) {
                try {
                    RespBase<String> resp = makeFailedResponse(redirect_uri, request, params, rootex.getMessage());
                    String failjson = toJSONString(resp);
                    log.info("resp: to binding - {}", failjson);
                    writeJson(toHttp(response), failjson);
                } catch (IOException e) {
                    log.error("resp:failed to binding", e);
                }
            } else {
                try {
                    log.info("redirect: to binding - {}", redirect_uri);
                    issueRedirect(request, response, redirect_uri, params, true);
                } catch (IOException e1) {
                    log.error("redirect:failed to redirect binding.", e1);
                }
            }
        }

        return super.onLoginFailure(token, ae, request, response);
    }

    /**
     * Initialize the constructor for obtaining authentication tokens
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    @SuppressWarnings("unchecked")
    private void initialize() throws NoSuchMethodException, SecurityException {
        ResolvableType resolveType = ResolvableType.forClass(this.getClass());
        Class<T> authenticationTokenClass = (Class<T>) resolveType.getSuperType().getGeneric(0).resolve();
        this.authenticationTokenConstructor = authenticationTokenClass
                .getConstructor(new Class[] { String.class, RedirectInfo.class, SocialAuthorizeInfo.class });
    }

    /**
     * Whether social networking authentication provider enabled
     *
     * @return
     */
    protected boolean enabled() {
        return false;
    }

}