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

import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_ACCESSTOKEN_SIGN_NAME;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_AUTHC_TOKEN;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_DATA_CIPHER_NAME;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_ERR_SESSION_SAVED;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_SERVICE_ROLE;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_SERVICE_ROLE_VALUE_IAMSERVER;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_SESSIONINFO_NAME;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_AUTH_BASE;
import static com.wl4g.iam.core.utils.IamAuthenticatingUtils.SESSION_STATUS_AUTHC;
import static com.wl4g.iam.core.utils.IamAuthenticatingUtils.correctAuthenticaitorURI;
import static com.wl4g.iam.core.utils.IamAuthenticatingUtils.generateAccessToken;
import static com.wl4g.iam.core.utils.IamAuthenticatingUtils.generateAccessTokenSignKey;
import static com.wl4g.iam.core.utils.IamAuthenticatingUtils.generateDataCipherKey;
import static com.wl4g.iam.core.utils.IamAuthenticatingUtils.sessionStatus;
import static com.wl4g.iam.core.utils.IamSecurityHolder.bind;
import static com.wl4g.iam.core.utils.IamSecurityHolder.bindKVParameters;
import static com.wl4g.iam.core.utils.IamSecurityHolder.extParameterValue;
import static com.wl4g.iam.core.utils.IamSecurityHolder.getSession;
import static com.wl4g.iam.core.utils.IamSecurityHolder.getSessionId;
import static com.wl4g.iam.core.utils.IamSecurityHolder.unbind;
import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.infra.common.web.UserAgentUtils.isBrowser;
import static com.wl4g.infra.common.web.WebUtils.applyQueryURL;
import static com.wl4g.infra.common.web.WebUtils.cleanURI;
import static com.wl4g.infra.common.web.WebUtils.getBaseURIForDefault;
import static com.wl4g.infra.common.web.WebUtils.isRelativeUri;
import static com.wl4g.infra.common.web.WebUtils.isTrue;
import static com.wl4g.infra.common.web.WebUtils.safeEncodeURL;
import static com.wl4g.infra.common.web.WebUtils.toQueryParams;
import static com.wl4g.infra.common.web.WebUtils2.getHttpRemoteAddr;
import static com.wl4g.infra.common.web.WebUtils2.getRFCBaseURI;
import static com.wl4g.infra.common.web.WebUtils2.writeJson;
import static com.wl4g.infra.common.web.WebUtils2.ResponseType.DEFAULT_RESPTYPE_NAME;
import static com.wl4g.infra.common.web.WebUtils2.ResponseType.isRespJSON;
import static com.wl4g.infra.common.web.rest.RespBase.RetCode.OK;
import static com.wl4g.infra.common.web.rest.RespBase.RetCode.UNAUTHC;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Collections.singletonMap;
import static java.util.Objects.nonNull;
import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.apache.shiro.web.util.WebUtils.getCleanParam;
import static org.apache.shiro.web.util.WebUtils.issueRedirect;
import static org.apache.shiro.web.util.WebUtils.toHttp;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.IOException;
import java.net.URI;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.Cookie;
import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.iam.authc.ClientSecretIamAuthenticationToken;
import com.wl4g.iam.authc.ServerIamAuthenticationToken.RedirectInfo;
import com.wl4g.iam.common.bean.FastCasClientInfo;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.configure.ServerSecurityConfigurer;
import com.wl4g.iam.configure.ServerSecurityCoprocessor;
import com.wl4g.iam.core.authc.IamAuthenticationToken;
import com.wl4g.iam.core.authc.IamAuthenticationTokenWrapper;
import com.wl4g.iam.core.cache.IamCacheManager;
import com.wl4g.iam.core.exception.AccessRejectedException;
import com.wl4g.iam.core.exception.IamException;
import com.wl4g.iam.core.exception.IllegalRequestException;
import com.wl4g.iam.core.filter.BasedRiskIamAuthenticationFilter;
import com.wl4g.iam.core.handler.AuthenticatingHandler;
import com.wl4g.iam.core.session.IamSession.RelationAttrKey;
import com.wl4g.iam.core.web.model.SessionInfo;
import com.wl4g.iam.core.web.servlet.IamCookie;
import com.wl4g.iam.crypto.SecureCryptService;
import com.wl4g.iam.crypto.SecureCryptService.CryptKind;
//import com.wl4g.iam.rcm.eventbus.IamEventBusService;
import com.wl4g.infra.common.framework.operator.GenericOperatorAdapter;
import com.wl4g.infra.common.web.WebUtils2;
import com.wl4g.infra.common.web.rest.RespBase;

/**
 * Multiple channel login authentication submitted processing based filter
 * <p>
 * {@link org.apache.shiro.web.filter.mgt.DefaultFilterChainManager#proxy()}
 * {@link org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver#pathMatches()}
 * {@link org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver#getChain()}
 * {@link org.apache.shiro.web.servlet.AbstractShiroFilter#getExecutionChain()}
 * {@link org.apache.shiro.web.servlet.AbstractShiroFilter#executeChain()}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2018年11月27日
 * @since
 */
public abstract class AbstractServerIamAuthenticationFilter<T extends IamAuthenticationToken>
        extends BasedRiskIamAuthenticationFilter<IamProperties> {

    protected @Autowired AuthenticatingHandler authHandler;

    protected @Autowired ServerSecurityConfigurer configurer;

    protected @Autowired ServerSecurityCoprocessor coprocessor;

    /**
     * Secure asymmetric cryptic service.
     */
    protected @Autowired GenericOperatorAdapter<CryptKind, SecureCryptService> cryptAdapter;

    protected @Autowired IamCacheManager cacheManager;

//    protected @Autowired IamEventBusService<?> eventBusService;

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        /*
         * The request path must be e.g /auth/general or /auth/sms, etc. Just
         * return to false directly, that is, let it execute
         * this#onAccessDenied()
         */
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        log.debug("called:onAccessDenied url: {}", toHttp(request).getRequestURI());
        return executeLogin(request, response);
    }

    @Override
    protected IamAuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = toHttp(request);
        HttpServletResponse resp = toHttp(response);
        /*
         * Pre-handler before authentication, For example, the implementation of
         * restricting client IP white-list to prevent violent cracking of large
         * number of submission login requests.
         */
        if (!coprocessor.preCreateToken(this, req, resp)) {
            throw new AccessRejectedException(format("Access rejected of remoteIp: %s", getHttpRemoteAddr(req)));
        }

        // Success redirection.
        RedirectInfo redirect = getRedirectInfo(request);
        /**
         * Remember redirect parameters.</br>
         * e.g. Android submit login will bring redirectUrl and application,
         * which will be used for successful login redirection.
         */
        rememberRedirectInfo(redirect, request, response);

        // Remote client address.
        String clientRemoteAddr = getHttpRemoteAddr(req);

        // Create authentication token
        T token = doCreateToken(clientRemoteAddr, redirect, req, resp);

        /**
         * Bind authenticate token, e.g: for online session management and
         * analysis.
         * 
         * @See:{@link com.wl4g.devops.iam.common.web.GenericApiEndpoint#convertIamSessionInfo(IamSession)}
         */
        bind(new RelationAttrKey(KEY_AUTHC_TOKEN), new IamAuthenticationTokenWrapper(token));
        return token;
    }

    protected abstract T doCreateToken(
            String remoteHost,
            RedirectInfo redirectInfo,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response)
            throws Exception {
        try {
            IamAuthenticationToken tk = (IamAuthenticationToken) token;

            /*
             * Save the token at the time of authentication, which can then be
             * used for extended logic usage.
             */
            // bind(KEY_AUTHC_TOKEN, tk);

            // Determine(decorate) redirectUrl(authenticator URL).
            RedirectInfo redirect = determineSuccessRedirect(getRedirectInfo(request), tk, subject, request, response);

            // Granting ticket.
            String grantTicket = authHandler.loggedin(redirect.getFromAppName(), subject).getGrantTicket();

            // Build response parameter.
            Map fullParams = new HashMap();
            fullParams.put(config.getParam().getApplication(), redirect.getFromAppName());
            // Merge custom parameters
            fullParams.putAll(getLegalCustomParameters(request));

            // Response with JSON?
            if (isJSONResponse(request)) {
                try {
                    // Make logged JSON.
                    RespBase<String> resp = makeLoggedResponse(token, subject, request, response, grantTicket,
                            redirect.getRedirectUrl(), fullParams);

                    // Handle post custom success.
                    afterAuthenticatingSuccess(tk, subject, toHttp(request), toHttp(response), resp.asMap());

                    String logged = toJSONString(resp);
                    log.info("resp: to success - {}", logged);

                    writeJson(toHttp(response), logged);
                } catch (IOException e) {
                    log.error("resp:error to login success", e);
                }
            } else { // Redirection page?
                /*
                 * When the source application exists, the indication is that it
                 * needs to be redirected to the CAS client application, then
                 * grantTicket is required.
                 */
                if (!isBlank(grantTicket)) {
                    fullParams.put(config.getParam().getGrantTicket(), grantTicket);
                }

                // Sets secret tokens to cookies.
                putSuccessTokensCookieIfNecessary(token, request, response);
                // Sets authorization info to cookies.
                putAuthzInfoCookiesAndSecurityIfNecessary(token, request, response);
                // Sets refresh xsrf token.
                putXsrfTokenCookieIfNecessary(token, request, response);

                // Handle post custom success.
                afterAuthenticatingSuccess(tk, subject, toHttp(request), toHttp(response), fullParams);

                log.info("redirect: to '{}', param:{}", redirect.getRedirectUrl(), fullParams);
                issueRedirect(request, response, redirect.getRedirectUrl(), fullParams, true);
            }

        } catch (IamException e) {
            // Convert to authentication exception before it can be intercepted.
            // See:org.apache.shiro.web.filter.authc.AuthenticatingFilter#executeLogin
            throw new AuthenticationException(e);
        } finally {
            cleanup(token, subject, request, response); // Cleanup
        }

        // Redirection has been responded and no further execution is required.
        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected boolean onLoginFailure(
            AuthenticationToken token,
            AuthenticationException ae,
            ServletRequest request,
            ServletResponse response) {
        IamAuthenticationToken tk = (IamAuthenticationToken) token;

        Throwable rootex = getRootCause(ae);
        String errmsg = nonNull(rootex) ? rootex.getMessage() : null;
        String tip = format("Failed to authentication of token: %s", token);
        if (WebUtils2.isStacktraceRequest(request)) {
            log.error(tip, ae);
        } else {
            log.error("{}, caused by: {}", tip, errmsg);
        }

        if (!isBlank(errmsg)) {
            /**
             * {@link LoginAuthenticatorController#errReads()}
             */
            bind(KEY_ERR_SESSION_SAVED, errmsg);
        }
        // Failure redirect
        RedirectInfo redirect = determineFailureRedirect(getRedirectInfo(request), tk, ae, request, response);

        // Handle authentication failure.
        afterAuthenticatingFailure(tk, ae, request, response);

        // Obtain bound parameters.
        Map fullParams = new HashMap();
        fullParams.put(config.getParam().getApplication(), redirect.getFromAppName());
        fullParams.put(config.getParam().getRedirectUrl(), redirect.getRedirectUrl());
        // Merge custom parameters
        fullParams.putAll(getLegalCustomParameters(request));

        // Response JSON message.
        if (isJSONResponse(request)) {
            try {
                RespBase<String> resp = makeFailedResponse(redirect.getRedirectUrl(), request, fullParams, errmsg);
                String failjson = toJSONString(resp);
                log.info("resp: to login - '{}'", failjson);
                writeJson(toHttp(response), failjson);
            } catch (IOException e) {
                log.error("resp:failed to login", e);
            }
        }
        // Redirect the login page directly.
        else {
            try {
                log.info("redirect: to login - {}", redirect);
                issueRedirect(request, response, redirect.getRedirectUrl(), fullParams, true);
            } catch (IOException e1) {
                log.error("redirect:failed to redirect login", e1);
            }
        }

        // Redirection has been responded and no further execution is required.
        return false;
    }

    /**
     * Determine is the JSON interactive strategy, or get it from
     * session(flexible API).
     *
     * @param request
     * @return
     */
    protected boolean isJSONResponse(ServletRequest request) {
        // Using last saved parameters.
        String respTypeValue = extParameterValue(KEY_REQ_AUTH_PARAMS, DEFAULT_RESPTYPE_NAME);
        log.debug("Using last response type:{}", respTypeValue);
        return isRespJSON(respTypeValue, toHttp(request)) || isRespJSON(toHttp(request));
    }

    /**
     * Get redirect information bound from authentication request
     * {@link AuthenticatorAuthenticationFilter#rememberRedirectInfo(ServletRequest, ServletResponse)}
     * </br>
     * {@link AbstractServerIamAuthenticationFilter#createToken(ServletRequest, ServletResponse)}
     *
     * @param request
     *            Whether to get only the redirection information of the binding
     * @return
     */
    protected RedirectInfo getRedirectInfo(ServletRequest request) {
        // Gets request redirect
        String fromAppName = getCleanParam(request, config.getParam().getApplication());
        String redirectUrl = getCleanParam(request, config.getParam().getRedirectUrl());
        // Whether to automatically use the fallback policy (i.e. use the
        // default application redirection URL) when the login is successful,
        // but you do not have permission to access the application represented
        // by the redirection URL.
        String useFallback = getCleanParam(request, config.getParam().getUseFallbackRedirect());
        // When it is not set whether to enable degraded redirection, it will be
        // automatically identified according to the client type (i.e. if it is
        // a pc browser request, it will enable fallback redirection, otherwise
        // it will not be enabled if it is an Android/iOS/WechatApplet, etc.)
        boolean defaultUseFallback = true;
        if (isBlank(useFallback) && !isBrowser(toHttp(request))) {
            defaultUseFallback = false;
        }
        RedirectInfo redirect = new RedirectInfo(fromAppName, redirectUrl, isTrue(useFallback, defaultUseFallback));

        // Fallback from last request bind.
        if (isBlank(redirect.getFromAppName())) {
            RedirectInfo bind = extParameterValue(KEY_REQ_AUTH_PARAMS, KEY_REQ_AUTH_REDIRECT);
            if (nonNull(bind)) {
                redirect = bind;
            }
        }

        // Check & use default redirectUrl.
        if (!isBlank(redirect.getFromAppName())) {
            FastCasClientInfo appInfo = configurer.getFastCasClientInfo(redirect.getFromAppName());
            notNull(appInfo, IllegalRequestException.class, "Invalid redirected application '%s'", redirect.getFromAppName());
            if (isBlank(redirect.getRedirectUrl())) {
                // Use default redirectUrl
                redirect.setRedirectUrl(appInfo.getViewExtranetBaseUri());
            }
            return redirect;
        }

        return new RedirectInfo();
    }

    /**
     * Make logged-in response message.
     *
     * @param token
     * @param subject
     * @param request
     *            Servlet request
     * @param fromAppName
     *            from application name
     * @param grantTicket
     *            logged information model
     * @param callbackUrl
     *            login success redirect URL
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected RespBase<String> makeLoggedResponse(
            AuthenticationToken token,
            Subject subject,
            ServletRequest request,
            ServletResponse response,
            String grantTicket,
            String callbackUrl,
            Map fullParams) throws Exception {
        hasTextOf(callbackUrl, "successCallbackUrl");

        // Redirection URL
        String successRedirectUrl = callbackUrl;
        if (isNotBlank(grantTicket)) {
            successRedirectUrl = applyQueryURL(callbackUrl, singletonMap(config.getParam().getGrantTicket(), grantTicket));
        }

        // Generate absoulte full redirectUrl.
        String fullRedirectUrl = successRedirectUrl.toString();
        if (isRelativeUri(fullRedirectUrl)) { // Relative path?
            fullRedirectUrl = getRFCBaseURI(toHttp(request), true) + successRedirectUrl;
        }

        // Placing it in http.body makes it easier for Android/iOS
        // to get token.
        fullParams.put(config.getParam().getRedirectUrl(), fullRedirectUrl);
        fullParams.put(KEY_SERVICE_ROLE, KEY_SERVICE_ROLE_VALUE_IAMSERVER);
        /**
         * Echo iam-web session info. (Optional) </br>
         * 
         * @see:{@link com.wl4g.devops.iam.web.LoginAuthenticatorEndpoint#handshake()}
         */
        fullParams.put(KEY_SESSIONINFO_NAME, new SessionInfo(config.getParam().getSid(), valueOf(getSessionId(subject))));

        // Handling secret tokens
        postHandleSuccessSecretTokens(token, subject, fullParams, request, response);

        // Make message
        RespBase<String> resp = RespBase.create(SESSION_STATUS_AUTHC);
        resp.withCode(OK).withMessage("Authentication successful");
        resp.forMap().putAll(fullParams);
        return resp;
    }

    /**
     * Make login failed response message.
     *
     * @param failRedirectUrl
     *            failure redirect URL
     * @param request
     *            Servlet request
     * @param params
     *            Redirected query configuration parameters
     * @param thw
     *            Exception object
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected RespBase<String> makeFailedResponse(String failRedirectUrl, ServletRequest request, Map params, String errmsg) {
        errmsg = (isNotBlank(errmsg)) ? errmsg : "Authentication failure";

        // Make message
        RespBase<String> resp = RespBase.create(sessionStatus());
        resp.withCode(UNAUTHC).withMessage(errmsg);
        resp.forMap().putAll(params);
        resp.forMap().put(config.getParam().getRedirectUrl(), failRedirectUrl);
        resp.forMap().put(KEY_SERVICE_ROLE, KEY_SERVICE_ROLE_VALUE_IAMSERVER);
        return resp;
    }

    /**
     * Determine the URL of the login success redirection, default: successURL,
     * can support customization.
     *
     * @param redirect
     *            First get from the binding {@link RedirectInfo}
     * @param token
     * @param subject
     * @param request
     * @param response
     * @return
     */
    protected RedirectInfo determineSuccessRedirect(
            RedirectInfo redirect,
            IamAuthenticationToken token,
            Subject subject,
            ServletRequest request,
            ServletResponse response) {

        // Unvalidity using to default
        if (isBlank(redirect.getFromAppName())) {
            redirect.setFromAppName(config.getSuccessService());
            redirect.setRedirectUrl(getSuccessUrl());
        }

        // after expand determine successUrl.
        String determinedRedirectUrl = configurer.decorateAuthenticateSuccessUrl(redirect.getRedirectUrl(), token, subject,
                request, response);
        // Check redirectUrl
        isTrue(!isRelativeUri(determinedRedirectUrl), IamException.class,
                "Custom success redirectUrl: '%s' is not an absolute URL", determinedRedirectUrl);

        redirect.setRedirectUrl(correctAuthenticaitorURI(URI.create(determinedRedirectUrl).toString())); // Check-symbol
        hasText(redirect.getRedirectUrl(), "Success redirectUrl empty, please check the configure");
        hasText(redirect.getFromAppName(), "Success application empty, please check the configure");
        return redirect;
    }

    /**
     * Determine the URL of the login failure redirection, default: loginURL,
     * can support customization.
     *
     * @param redirect
     *            First get from the binding {@link RedirectInfo}
     * @param token
     * @param ae
     * @param request
     * @param response
     * @return
     */
    protected RedirectInfo determineFailureRedirect(
            RedirectInfo redirect,
            IamAuthenticationToken token,
            AuthenticationException ae,
            ServletRequest request,
            ServletResponse response) {
        if (isBlank(redirect.getRedirectUrl())) {
            redirect.setRedirectUrl(getLoginUrl());
        }

        // Determinte failure loginUrl
        String determinedLoginUrl = configurer.decorateAuthenticateFailureUrl(redirect.getRedirectUrl(), token, ae, request,
                response);
        // Check loginUrl
        isTrue(!isRelativeUri(determinedLoginUrl), IamException.class, "Invalid custom loginUrl: '%s' must is absolute URL",
                determinedLoginUrl);

        redirect.setRedirectUrl(cleanURI(determinedLoginUrl)); // Symbol check
        // hasTextOf(redirect.getFromAppName(), "application"); //Probably-empty
        hasText(redirect.getRedirectUrl(), "Failure redirectUrl is required, please check the configure");
        return redirect;
    }

    /**
     * After handle authentication success.
     * 
     * @param token
     * @param subject
     * @param request
     * @param response
     * @param respParams
     */
    protected void afterAuthenticatingSuccess(
            IamAuthenticationToken token,
            Subject subject,
            HttpServletRequest request,
            HttpServletResponse response,
            Map<String, Object> respParams) {
        // Invoke authentication success.
        coprocessor.postAuthenticatingSuccess(token, subject, request, response, respParams);
    }

    /**
     * After handle authentication failure.
     * 
     * @param token
     * @param ae
     * @param request
     * @param response
     */
    protected void afterAuthenticatingFailure(
            IamAuthenticationToken token,
            AuthenticationException ae,
            ServletRequest request,
            ServletResponse response) {
        // Invoke authentication failure.
        coprocessor.postAuthenticatingFailure(token, ae, request, response);
    }

    /**
     * Post secret and tokens/signature handling.
     * 
     * @param token
     * @param subject
     * @param params
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void postHandleSuccessSecretTokens(
            AuthenticationToken token,
            Subject subject,
            Map params,
            ServletRequest request,
            ServletResponse response) throws Exception {

        // Puts secret tokens to cookies.
        String[] tokens = putSuccessTokensCookieIfNecessary(token, request, response);

        // Sets secret tokens to ressponse.
        params.put(config.getParam().getDataCipherKeyName(), tokens[0]);
        params.put(config.getParam().getAccessTokenName(), tokens[1]);
        // Sets authorization info.
        params.putAll(putAuthzInfoCookiesAndSecurityIfNecessary(token, request, response));

        // Sets refresh xsrf token.
        // params.putAll(putXsrfTokenCookieIfNecessary(token, request,
        // response));
        putXsrfTokenCookieIfNecessary(token, request, response);
    }

    /**
     * Puts secret and tokens/signature to cookies handling.
     * 
     * @param token
     * @param request
     * @param response
     * @return
     * @throws DecoderException
     */
    protected String[] putSuccessTokensCookieIfNecessary(
            AuthenticationToken token,
            ServletRequest request,
            ServletResponse response) throws DecoderException {

        String dataCipherKeyHex = null, accessToken = null, umidToken = null;
        if (token instanceof ClientSecretIamAuthenticationToken) {
            // Sets dataCipherKey(Required)
            if (config.getCipher().isEnableDataCipher()) {
                // Gets SecureCryptService.
                CryptKind kind = ((ClientSecretIamAuthenticationToken) token).getCryptKind();
                SecureCryptService cryptService = cryptAdapter.forOperator(kind);

                // Use clientSecretKey(hexPublicKey) to encrypt the newly
                // generate symmetric dataCipherKey
                String clientSecretKey = ((ClientSecretIamAuthenticationToken) token).getClientSecretKey();
                // Encryption dataCipherKey by clientSecretKey.
                KeySpec pubKeySpec = cryptService.generatePubKeySpec(decodeHex(clientSecretKey.toCharArray()));
                // New generate dataCipherKey.
                String hexDataCipherKey = bind(KEY_DATA_CIPHER_NAME, generateDataCipherKey());
                dataCipherKeyHex = cryptService.encrypt(pubKeySpec, hexDataCipherKey);

                // Set to cookies
                if (isBrowser(toHttp(request))) {
                    Cookie c = new IamCookie(config.getCookie());
                    c.setName(config.getParam().getDataCipherKeyName());
                    c.setValue(dataCipherKeyHex);
                    c.saveTo(toHttp(request), toHttp(response));
                }
            }

            /**
             * Sets accessToken(Required)
             * 
             * @see {@link com.wl4g.devops.iam.common.mgt.IamSubjectFactory#assertRequestAccessTokenValidity}
             */
            if (config.getSession().isEnableAccessTokenValidity()) {
                // Create accessTokenSignKey.
                String accessTokenSignKey = bind(KEY_ACCESSTOKEN_SIGN_NAME, generateAccessTokenSignKey(getSessionId()));
                accessToken = generateAccessToken(getSession(), accessTokenSignKey);
                // Set to cookies
                if (isBrowser(toHttp(request))) {
                    Cookie c = new IamCookie(config.getCookie());
                    c.setName(config.getParam().getAccessTokenName());
                    c.setValue(accessToken);
                    c.saveTo(toHttp(request), toHttp(response));
                }
            }

            /**
             * Sets umidToken.(TODO valid-non-impl)
             * 
             * @see {@link com.wl4g.devops.iam.common.mgt.IamSubjectFactory#assertRequestAccessTokenValidity}
             */
            umidToken = ((ClientSecretIamAuthenticationToken) token).getUmidToken();
            if (isBrowser(toHttp(request))) {
                Cookie c = new IamCookie(config.getCookie());
                c.setName(config.getParam().getUmidTokenName());
                c.setValue(umidToken);
                c.saveTo(toHttp(request), toHttp(response));
            }
        }

        return new String[] { dataCipherKeyHex, accessToken, umidToken };
    }

    /**
     * Saved the latest redirect info, such as response_type, source
     * application, etc.</br>
     * E.G.:</br>
     * </br>
     *
     * <b>Req1：</b>http://localhost:18080/iam-web/view/login.html?service=iam-example&redirect_url=http://localhost:14041/iam-example/index.html</br>
     * <b>Resp1：</b>login.html</br>
     * </br>
     * <b>Req2：(Intercepted by
     * rootFilter)</b>http://localhost:18080/iam-web/favicon.ico</br>
     * <b>Resp2：</b>
     * 302->http://localhost:18080/iam-web/view/login.html?service=iam-example&redirect_url=http://localhost:14041/iam-example/index.html</br>
     * </br>
     * <b>Req3：</b>http://localhost:18080/iam-web/view/login.html</br>
     * </br>
     * <p>
     * No parameters for the second request for login.html ??? This is the
     * problem to be solved by this method.
     *
     * @param redirect
     * @param request
     * @param response
     */
    private void rememberRedirectInfo(RedirectInfo redirect, ServletRequest request, ServletResponse response) {
        notNullOf(redirect, "redirect");
        // Safety encoding for URL fragment.
        redirect.setRedirectUrl(safeEncodeParameterRedirectUrl(redirect.getRedirectUrl()));

        // Response type.
        String respTypeKey = DEFAULT_RESPTYPE_NAME;
        String respType = getCleanParam(request, respTypeKey);

        // Overlay to save the latest parameters.
        bindKVParameters(KEY_REQ_AUTH_PARAMS, respTypeKey, respType, KEY_REQ_AUTH_REDIRECT, redirect);

        log.debug("Binding for respType[{}], redirect[{}]", respType, redirect);
    }

    /**
     * The redirection URI of the secure encoding loop is mainly used to prevent
     * the loss of fragments such as for example "/#/index". </br>
     * e.g.
     *
     * <pre>
     * http://mydomain.com/iam-example/authenticator?redirect_url=http://mydomain.com/#/index
     * => http://mydomain.com/iam-example/authenticator?redirect_url=http%3A%2F%2Fmydomain.com%2F%23%2Findex
     * </pre>
     *
     * @param fullRedirectUrl
     *            Full redirect URL
     * @return
     */
    private String safeEncodeParameterRedirectUrl(String fullRedirectUrl) {
        if (!isBlank(fullRedirectUrl)) {
            // To prevent automatic loss, such as the anchor part of "#".
            URI uri = URI.create(fullRedirectUrl);
            Map<String, String> params = toQueryParams(uri.getQuery() + "#" + uri.getFragment());
            if (!isEmpty(params)) {
                String clientRedirectUrl = params.get(config.getParam().getRedirectUrl());
                if (!isBlank(clientRedirectUrl)) {
                    params.remove(config.getParam().getRedirectUrl());
                    params.put(config.getParam().getRedirectUrl(), safeEncodeURL(clientRedirectUrl));
                    String newRedirectUrl = getBaseURIForDefault(uri.getScheme(), uri.getHost(), uri.getPort()) + uri.getPath();
                    fullRedirectUrl = applyQueryURL(newRedirectUrl, params);
                }
            }
        }
        return fullRedirectUrl;
    }

    /**
     * Cleaning-up.(e.g.Save request parameters)
     *
     * @param token
     * @param subject
     * @param request
     * @param response
     */
    private void cleanup(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) {
        /*
         * Clean See:AuthenticatorAuthenticationFilter#bindRequestParameters()
         */
        unbind(KEY_REQ_AUTH_PARAMS);

        /*
         * Clean error messages. See:i.w.DiabloExtraController#errReads()
         */
        unbind(KEY_ERR_SESSION_SAVED);
    }

    /**
     * Login/Authentication request parameters binding session key
     */
    final public static String KEY_REQ_AUTH_PARAMS = "REQ_AUTH_PARAMS";

    /**
     * Authentication request redirect information key.
     */
    final public static String KEY_REQ_AUTH_REDIRECT = "REQ_AUTH_REDIRECT";

    /**
     * URI login submission base path for processing all SHIRO authentication
     * filters submitted by login
     */
    final public static String URI_BASE_MAPPING = URI_AUTH_BASE;

}