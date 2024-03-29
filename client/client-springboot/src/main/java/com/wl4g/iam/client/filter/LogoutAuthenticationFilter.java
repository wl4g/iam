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
package com.wl4g.iam.client.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.SessionException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.core.web.error.handler.AbstractSmartErrorHandler;
import com.wl4g.iam.client.authc.LogoutAuthenticationToken;
import com.wl4g.iam.client.configure.ClientSecurityConfigurer;
import com.wl4g.iam.client.configure.ClientSecurityCoprocessor;
import com.wl4g.iam.core.annotation.IamFilter;
import com.wl4g.iam.common.model.LogoutModel;
import com.wl4g.iam.core.cache.JedisIamCacheManager;
import com.wl4g.iam.core.exception.GrantTicketNullException;
import com.wl4g.iam.core.exception.IamException;
import com.wl4g.iam.core.filter.IamAuthenticationFilter;

import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.apache.shiro.web.util.WebUtils.toHttp;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.web.WebUtils2.applyQueryURL;
import static com.wl4g.infra.common.web.WebUtils2.isTrue;
import static com.wl4g.infra.common.web.rest.RespBase.RetCode.*;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_CLIENT_LOGOUT;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_BASE;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_LOGOUT;
import static com.wl4g.iam.core.utils.IamSecurityHolder.getBindValue;
import static com.wl4g.iam.core.utils.IamSecurityHolder.getPrincipal;
import static com.wl4g.iam.core.utils.IamSecurityHolder.getSessionId;

/**
 * Logout authentication filter
 * 
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2018年12月6日
 * @since
 */
@IamFilter
public class LogoutAuthenticationFilter extends AbstractClientIamAuthenticationFilter<AuthenticationToken>
        implements IamAuthenticationFilter {
    final public static String NAME = "logoutFilter";

    final protected RestTemplate restTemplate;

    public LogoutAuthenticationFilter(AbstractSmartErrorHandler errorHandler, ClientSecurityConfigurer context,
            ClientSecurityCoprocessor coprocessor, JedisIamCacheManager cacheManager, RestTemplate restTemplate) {
        super(errorHandler, context, coprocessor, cacheManager);
        this.restTemplate = notNullOf(restTemplate, "restTemplate");
    }

    @Override
    protected AuthenticationToken doCreateToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Using coercion ignores remote exit failures
        final boolean forced = isTrue(request, config.getParam().getLogoutForced(), true);
        log.info("Signout forced: {}, sessionId: {}", forced, getSessionId());

        // Note: there is no need to assert when getting
        // the principal. e.g, avoid call '/logout' to report an error
        // when the current client is not authenticated.
        return new LogoutAuthenticationToken(forced, getPrincipal(false));
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        // Create logout token
        LogoutAuthenticationToken token = (LogoutAuthenticationToken) createToken(request, response);

        // Pre-logout processing.
        coprocessor.preLogout(token, toHttp(request), toHttp(response));

        // Post to remote logout
        LogoutModel logout = null;
        try {
            logout = doRequestRemoteLogout(token.isForced());
        } catch (Exception e) {
            if (e instanceof IamException)
                log.warn("Failed to remote logout. {}", getRootCauseMessage(e));
            else
                log.warn("Failed to remote logout.", e);
        }

        // Check server logout result.
        if (token.isForced() || checkLogoutModel(logout)) {
            try {
                // That session logout
                // try/catch added for SHIRO-298:
                getSubject(request, response).logout();
                log.info("logout client of sessionId: {}", getSessionId());
            } catch (SessionException e) {
                log.warn("Logout exception. This can generally safely be ignored.", e);
            }
        }

        // Redirection processing
        onLoginFailure(token, null, request, response);
        return false;
    }

    @Override
    protected void customFailureRedirectParams(
            AuthenticationToken token,
            Throwable cause,
            HttpServletRequest request,
            Map<String, String> params) {
        // When exiting, the principal will be pushed to the server along with
        // the redirection, so that the server can realize special handling of
        // the exit behavior, e.g, to customize different login pages for each
        // user.
        params.put(config.getParam().getPrincipalName(), valueOf(token.getPrincipal()));
    }

    @Override
    protected RespBase<Object> makeFailedResponse(AuthenticationToken token, String loginRedirectUrl, Throwable err) {
        RespBase<Object> resp = super.makeFailedResponse(token, loginRedirectUrl, err);
        // More useful than RetCode.UNAUTHC
        resp.setCode(OK);
        return resp;
    }

    /**
     * Request logout fast-CAS server
     * 
     * @param forced
     * @return
     */
    private LogoutModel doRequestRemoteLogout(boolean forced) {
        // Gets grantTicket
        String grantTicket = getBindValue(SAVE_GRANT_TICKET);

        // Post server logout URL by grantTicket
        String url = buildRemoteLogoutUrl(grantTicket, forced);

        RespBase<LogoutModel> resp = this.restTemplate
                .exchange(url, HttpMethod.POST, null, new ParameterizedTypeReference<RespBase<LogoutModel>>() {
                })
                .getBody();

        if (!RespBase.isSuccess(resp)) {
            throw new IamException(resp.getMessage());
        }
        return resp.getData();
    }

    /**
     * Check remote logged-out success
     * 
     * @param logout
     * @return
     */
    private boolean checkLogoutModel(LogoutModel logout) {
        return (!isNull(logout) && config.getServiceName().equals(valueOf(logout.getApplication())));
    }

    /**
     * Build remote logout URL
     * 
     * @param forced
     * @param grantTicket
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String buildRemoteLogoutUrl(String grantTicket, boolean forced) {
        if (isBlank(grantTicket)) {
            throw new GrantTicketNullException(
                    "Logout failed, because grant Ticket could not be empty, it may have logged out, do not need to repeat logout");
        }

        /*
         * Synchronize with xx.xx.session.mgt.IamSessionManager#getSessionId
         */
        StringBuffer uri = new StringBuffer(config.getServerUri()).append(URI_IAM_SERVER_BASE).append("/").append(
                URI_IAM_SERVER_LOGOUT);
        Map queryParams = new LinkedHashMap<>();
        queryParams.put(config.getParam().getApplication(), config.getServiceName());
        queryParams.put(config.getParam().getGrantTicket(), grantTicket);
        queryParams.put(config.getParam().getLogoutForced(), forced);
        // Full URL
        return applyQueryURL(uri.toString(), queryParams);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getUriMapping() {
        return "/" + URI_IAM_CLIENT_LOGOUT;
    }

}