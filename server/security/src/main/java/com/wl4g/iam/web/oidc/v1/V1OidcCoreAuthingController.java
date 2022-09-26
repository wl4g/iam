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
package com.wl4g.iam.web.oidc.v1;

import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_EXT_AT_HASH;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_EXT_NONCE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_TOKEN_TYPE_BEARER;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_AUTHORIZE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_CERTS;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_DEVICECODE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_INTROSPECT;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_TOKEN;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_USERINFO;
import static com.wl4g.infra.common.codec.Encodes.urlEncode;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wl4g.iam.annotation.V1OidcCoreController;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.ChallengeAlgorithmType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardGrantType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardPrompt;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardResponseMode;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardResponseType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardScope;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.TokenSignAlgorithmType;
import com.wl4g.iam.common.model.oidc.v1.V1AccessToken;
import com.wl4g.iam.common.model.oidc.v1.V1AccessTokenInfo;
import com.wl4g.iam.common.model.oidc.v1.V1AuthorizationCodeInfo;
import com.wl4g.iam.common.model.oidc.v1.V1DeviceCode;
import com.wl4g.iam.common.model.oidc.v1.V1DeviceCodeInfo;
import com.wl4g.iam.common.model.oidc.v1.V1Introspection;
import com.wl4g.iam.common.model.oidc.v1.V1OidcUserClaims;
import com.wl4g.iam.handler.oidc.v1.V1OidcAuthingHandler;
import com.wl4g.iam.web.oidc.BasedOidcAuthingController;
import com.wl4g.infra.common.lang.FastTimeClock;

/**
 * IAM V1-OIDC authentication controller.
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-03-18 v1.0.0
 * @since v3.0.0
 * @see https://openid.net/specs/openid-connect-core-1_0.html#AuthResponseValidation
 */
@V1OidcCoreController
public class V1OidcCoreAuthingController extends BasedOidcAuthingController {

    private @Autowired V1OidcAuthingHandler oidcAuthingHandler;

    /**
     * Provides JSON Web Key Set containing the public part of the key used to
     * sign ID tokens.
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_CORE_CERTS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> certs(HttpServletRequest req) {
        log.info("called:certs '{}' from '{}'", URI_IAM_OIDC_ENDPOINT_CORE_CERTS, req.getRemoteHost());
        return ResponseEntity.ok().body(loadJWKConfig().getPubJWKSet().toString());
    }

    /**
     * Provides authorization endpoint. </br>
     * Authorization request docs see to:
     * https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest
     * 
     * @param client_id
     *            REQUIRED. Registered client application ID. OAuth 2.0 Client
     *            Identifier valid at the Authorization Server.
     * @param redirect_uri
     *            REQUIRED. Redirection URI to which the response will be sent.
     *            This URI MUST exactly match one of the Redirection URI values
     *            for the Client pre-registered at the OpenID Provider, with the
     *            matching performed as described in Section 6.2.1 of [RFC3986]
     *            (Simple String Comparison). When using this flow, the
     *            Redirection URI SHOULD use the https scheme; however, it MAY
     *            use the http scheme, provided that the Client Type is
     *            confidential, as defined in Section 2.1 of OAuth 2.0, and
     *            provided the OP allows the use of http Redirection URIs in
     *            this case. The Redirection URI MAY use an alternate scheme,
     *            such as one that is intended to identify a callback into a
     *            native application. </br>
     * @param response_type
     *            REQUIRED. OAuth 2.0 Response Type value that determines the
     *            authorization processing flow to be used, including what
     *            parameters are returned from the endpoints used. When using
     *            the Authorization Code Flow, this value is code.. see to:
     *            https://openid.net/specs/openid-connect-core-1_0.html#codeExample
     *            </br>
     * @param scope
     *            REQUIRED. OpenID Connect requests MUST contain the openid
     *            scope value. If the openid scope value is not present, the
     *            behavior is entirely unspecified. Other scope values MAY be
     *            present. Scope values used that are not understood by an
     *            implementation SHOULD be ignored. See Sections 5.4 and 11 for
     *            additional scope values defined by this specification. </br>
     * @param state
     *            RECOMMENDED. Opaque value used to maintain state between the
     *            request and the callback. Typically, Cross-Site Request
     *            Forgery (CSRF, XSRF) mitigation is done by cryptographically
     *            binding the value of this parameter with a browser cookie.
     *            </br>
     * @param nonce
     *            OPTIONAL. String value used to associate a Client session with
     *            an ID Token, and to mitigate replay attacks. The value is
     *            passed through unmodified from the Authentication Request to
     *            the ID Token. Sufficient entropy MUST be present in the nonce
     *            values used to prevent attackers from guessing values. For
     *            implementation notes, see Section 15.5.2. </br>
     * @param display
     *            OPTIONAL. ASCII string value that specifies how the
     *            Authorization Server displays the authentication and consent
     *            user interface pages to the End-User. The defined values are:
     *            <b>page</b> The Authorization Server SHOULD display the
     *            authentication and consent UI consistent with a full User
     *            Agent page view. If the display parameter is not specified,
     *            this is the default display mode. </br>
     *            <b>popup</b> The Authorization Server SHOULD display the
     *            authentication and consent UI consistent with a popup User
     *            Agent window. The popup User Agent window should be of an
     *            appropriate size for a login-focused dialog and should not
     *            obscure the entire window that it is popping up over. </br>
     *            <b>touch</b> The Authorization Server SHOULD display the
     *            authentication and consent UI consistent with a device that
     *            leverages a touch interface. </br>
     *            <b>wap</b> The Authorization Server SHOULD display the
     *            authentication and consent UI consistent with a "feature
     *            phone" type display. </br>
     * @param prompt
     *            OPTIONAL. Space delimited, case sensitive list of ASCII string
     *            values that specifies whether the Authorization Server prompts
     *            the End-User for reauthentication and consent. The defined
     *            values are: </br>
     *            <b>none</b> The Authorization Server MUST NOT display any
     *            authentication or consent user interface pages. An error is
     *            returned if an End-User is not already authenticated or the
     *            Client does not have pre-configured consent for the requested
     *            Claims or does not fulfill other conditions for processing the
     *            request. The error code will typically be login_required,
     *            interaction_required, or another code defined in Section
     *            3.1.2.6. This can be used as a method to check for existing
     *            authentication and/or consent. </br>
     *            <b>login</b> The Authorization Server SHOULD prompt the
     *            End-User for reauthentication. If it cannot reauthenticate the
     *            End-User, it MUST return an error, typically login_required.
     *            </br>
     *            <b>consent The Authorization Server SHOULD prompt the End-User
     *            for consent before returning information to the Client. If it
     *            cannot obtain consent, it MUST return an error, typically
     *            consent_required. </br>
     *            <b>select_account</b> The Authorization Server SHOULD prompt
     *            the End-User to select a user account. This enables an
     *            End-User who has multiple accounts at the Authorization Server
     *            to select amongst the multiple accounts that they might have
     *            current sessions for. If it cannot obtain an account selection
     *            choice made by the End-User, it MUST return an error,
     *            typically account_selection_required. </br>
     * @param max_age
     *            OPTIONAL. Maximum Authentication Age. Specifies the allowable
     *            elapsed time in seconds since the last time the End-User was
     *            actively authenticated by the OP. If the elapsed time is
     *            greater than this value, the OP MUST attempt to actively
     *            re-authenticate the End-User. (The max_age request parameter
     *            corresponds to the OpenID 2.0 PAPE [OpenID.PAPE] max_auth_age
     *            request parameter.) When max_age is used, the ID Token
     *            returned MUST include an auth_time Claim Value. </br>
     * @param ui_locales
     *            OPTIONAL. End-User's preferred languages and scripts for the
     *            user interface, represented as a space-separated list of BCP47
     *            [RFC5646] language tag values, ordered by preference. For
     *            instance, the value "fr-CA fr en" represents a preference for
     *            French as spoken in Canada, then French (without a region
     *            designation), followed by English (without a region
     *            designation). An error SHOULD NOT result if some or all of the
     *            requested locales are not supported by the OpenID Provider.
     *            </br>
     * @param id_token_hint
     *            OPTIONAL. ID Token previously issued by the Authorization
     *            Server being passed as a hint about the End-User's current or
     *            past authenticated session with the Client. If the End-User
     *            identified by the ID Token is logged in or is logged in by the
     *            request, then the Authorization Server returns a positive
     *            response; otherwise, it SHOULD return an error, such as
     *            login_required. When possible, an id_token_hint SHOULD be
     *            present when prompt=none is used and an invalid_request error
     *            MAY be returned if it is not; however, the server SHOULD
     *            respond successfully when possible, even if it is not present.
     *            The Authorization Server need not be listed as an audience of
     *            the ID Token when it is used as an id_token_hint value. If the
     *            ID Token received by the RP from the OP is encrypted, to use
     *            it as an id_token_hint, the Client MUST decrypt the signed ID
     *            Token contained within the encrypted ID Token. The Client MAY
     *            re-encrypt the signed ID token to the Authentication Server
     *            using a key that enables the server to decrypt the ID Token,
     *            and use the re-encrypted ID token as the id_token_hint value.
     *            </br>
     * @param login_hint
     *            OPTIONAL. Hint to the Authorization Server about the login
     *            identifier the End-User might use to log in (if necessary).
     *            This hint can be used by an RP if it first asks the End-User
     *            for their e-mail address (or other identifier) and then wants
     *            to pass that value as a hint to the discovered authorization
     *            service. It is RECOMMENDED that the hint value match the value
     *            used for discovery. This value MAY also be a phone number in
     *            the format specified for the phone_number Claim. The use of
     *            this parameter is left to the OP's discretion. </br>
     * @param acr_values
     *            OPTIONAL. Requested Authentication Context Class Reference
     *            values. Space-separated string that specifies the acr values
     *            that the Authorization Server is being requested to use for
     *            processing this Authentication Request, with the values
     *            appearing in order of preference. The Authentication Context
     *            Class satisfied by the authentication performed is returned as
     *            the acr Claim Value, as specified in Section 2. The acr Claim
     *            is requested as a Voluntary Claim by this parameter. </br>
     * @param response_mode
     *            OPTIONAL. Informs the Authorization Server of the mechanism to
     *            be used for returning parameters from the Authorization
     *            Endpoint. This use of this parameter is NOT RECOMMENDED when
     *            the Response Mode that would be requested is the default mode
     *            specified for the Response Type. see to
     *            https://openid.net/specs/oauth-v2-form-post-response-mode-1_0.html#rfc.references1
     *            </br>
     * @param code_challenge
     *            Used to secure authorization code grants by using Proof Key
     *            for Code Exchange (PKCE). Required if code_challenge_method is
     *            included. For more information, see the PKCE RFC. This
     *            parameter is now recommended for all application types, both
     *            public and confidential clients. see to:
     *            https://blog.csdn.net/weixin_34415923/article/details/89691037
     *            </br>
     * @param code_challenge_method
     *            The method used to encode the code_verifier for the
     *            code_challenge parameter. This SHOULD be S256, but the spec
     *            allows the use of plain if the client can't support SHA256. If
     *            excluded, code_challenge is assumed to be plaintext if
     *            code_challenge is included. The Microsoft identity platform
     *            supports both plain and S256. </br>
     * @param auth
     * @param uriBuilder
     * @param req
     * @return
     * @throws JOSEException
     * @throws NoSuchAlgorithmException
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_CORE_AUTHORIZE, method = RequestMethod.GET)
    public ResponseEntity<?> authorize(
            @RequestParam String client_id,
            @RequestParam String redirect_uri,
            @RequestParam String response_type,
            @RequestParam String scope,
            @RequestParam String state,
            @RequestParam(required = false) String nonce,
            @RequestParam(required = false) String display,
            @RequestParam(required = false) String prompt,
            @RequestParam(required = false) String max_age,
            @RequestParam(required = false) String ui_locales,
            @RequestParam(required = false) String id_token_hint,
            @RequestParam(required = false) String login_hint,
            @RequestParam(required = false) String acr_values,
            @RequestParam(required = false) String response_mode,
            @RequestParam(required = false) String code_challenge,
            @RequestParam(required = false, defaultValue = "plain") String code_challenge_method,
            @RequestHeader(name = "Authorization", required = false) String auth,
            UriComponentsBuilder uriBuilder,
            HttpServletRequest req) throws Exception {

        log.info("called:authorize '{}' from '{}', scope={} response_type={} client_id={} redirect_uri={}",
                URI_IAM_OIDC_ENDPOINT_CORE_AUTHORIZE, req.getRemoteHost(), scope, response_type, client_id, redirect_uri);

        if (isBlank(auth)) {
            log.info("User and password not provided. scope={} response_type={} client_id={} redirect_uri={}", scope,
                    response_type, client_id, redirect_uri);
            return wrapUnauthentication();
        }

        // Check response_type valid.
        if (!StandardResponseType.isValid(response_type)) {
            String url = format("%s#error=unsupported_response_type", redirect_uri);
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
        }

        // Load configuration.
        V1OidcClientConfig clientConfig = oidcAuthingHandler.loadClientConfig(client_id);
        if (isNull(clientConfig)) {
            return wrapErrorRfc6749("invalid_client", "The client_id is no vaild");
        }

        // Check the if openid existing. (oauth2 does not required)
        if (clientConfig.isMustOpenidScopeEnabled() && !StandardScope.openid.containsIn(scope)) {
            return wrapErrorRfc6749("invalid_scope", "The oidc v1 specs requires that scope must contain openid");
        }
        if (isNull(StandardScope.isValid(scope))) {
            return wrapErrorRfc6749("invalid_scope", "The scope is no vaild");
        }

        // Check redirect_uri valid.
        boolean matched = safeList(clientConfig.getValidWebOriginUris()).stream()
                .anyMatch(u -> getMatcher().matchStart(u, redirect_uri));
        if (!matched) {
            String url = format("%s#error=unsupported_redirect_uri", redirect_uri);
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
        }

        StandardPrompt _prompt = StandardPrompt.safeOf(prompt);
        // Always prompt the user for authentication
        if (StandardPrompt.login == _prompt) {
            return wrapUnauthentication();
        }

        // Verify credentials.
        String[] creds = new String(Base64.decodeBase64(auth.split(" ")[1])).split(":", 2);
        String username = creds[0];
        String password = creds[1];
        V1OidcUserClaims user = oidcAuthingHandler.getV1OidcUserClaimsByUser(username);
        // if (!username.equals("root")) { // for test
        boolean verified = oidcAuthingHandler.validate(user, password);
        log.info("Verificating password {} for user {} is {}", password, username, verified);

        // Render authorization page based on authentication status.
        // see:https://developer.okta.com/docs/reference/api/oidc/#request-parameters
        if (StandardPrompt.none == _prompt) {
            if (!verified) {
                log.info("Wrong user and password combination. scope={} response_type={} client_id={} redirect_uri={}", scope,
                        response_type, client_id, redirect_uri);
                return wrapUnauthentication();
            }
        } else if (StandardPrompt.consent == _prompt) {
            if (!verified) {
                // TODO
            }
        } else if (StandardPrompt.select_account == _prompt) {
            // TODO
        }

        return doAuthorizeWithSuccess(clientConfig, user, client_id, redirect_uri, response_type, scope, state, nonce, display,
                prompt, max_age, ui_locales, id_token_hint, login_hint, acr_values, response_mode, code_challenge,
                code_challenge_method, auth, uriBuilder, req);
    }

    /**
     * Provides device code endpoint. e.g to
     * see:https://docs.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-device-code
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_CORE_DEVICECODE, method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> deviceCode(
            @RequestParam String client_id,
            @RequestParam String scope,
            UriComponentsBuilder uriBuilder,
            HttpServletRequest req) {
        log.info("called:deviceCode '{}' from '{}', client_id={}, scope={} ", URI_IAM_OIDC_ENDPOINT_CORE_DEVICECODE,
                req.getRemoteHost(), client_id, scope);

        // Check the scope.
        if (isNull(StandardScope.isValid(scope))) {
            return wrapErrorRfc6749("invalid_scope", "The scope is no vaild");
        }

        // load configuration.
        V1OidcClientConfig clientConfig = oidcAuthingHandler.loadClientConfig(client_id);
        if (isNull(clientConfig)) {
            return wrapErrorRfc6749("invalid_client", "The client_id is no vaild");
        }

        V1DeviceCode deviceCode = V1DeviceCode.builder()
                .device_code("")
                .user_code("")
                .verification_uri("")
                .interval(5)
                .expires_in(clientConfig.getDeviceCodeExpirationSeconds())
                .message("ok")
                .build();
        V1DeviceCodeInfo codeInfo = V1DeviceCodeInfo.builder()
                .deviceCode(deviceCode)
                .clientId(client_id)
                .scope(scope)
                .iss(getIss(uriBuilder))
                .build();
        oidcAuthingHandler.putDeviceCode(deviceCode.getDevice_code(), codeInfo);

        log.info("issuing device code={}, codeInfo={}", deviceCode, codeInfo);
        return ResponseEntity.ok(deviceCode);
    }

    /**
     * Provides token endpoint. </br>
     * <p>
     * https://openid.net/specs/openid-connect-core-1_0.html#TokenRequest</br>
     * 
     * Request:
     * 
     * <pre>
     *  POST /token HTTP/1.1
     *  Host: server.example.com
     *  Content-Type: application/x-www-form-urlencoded
     *  Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
     *  
     *  grant_type=authorization_code&code=SplxlOBeZQQYbYS6WxSbIA
     *  &redirect_uri=https%3A%2F%2Fclient.example.org%2Fcb
     * </pre>
     * 
     * Response:
     * 
     * <pre>
     *  HTTP/1.1 200 OK
     *  Content-Type: application/json
     *  Cache-Control: no-store
     *  Pragma: no-cache
     *  
     *  {
     *  "access_token": "SlAV32hkKG",
     *  "token_type": "Bearer",
     *  "refresh_token": "8xLOxBtZp8",
     *  "expires_in": 3600,
     *  "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ.ewogImlzc
     *   yI6ICJodHRwOi8vc2VydmVyLmV4YW1wbGUuY29tIiwKICJzdWIiOiAiMjQ4Mjg5
     *   NzYxMDAxIiwKICJhdWQiOiAiczZCaGRSa3F0MyIsCiAibm9uY2UiOiAibi0wUzZ
     *   fV3pBMk1qIiwKICJleHAiOiAxMzExMjgxOTcwLAogImlhdCI6IDEzMTEyODA5Nz
     *   AKfQ.ggW8hZ1EuVLuxNuuIJKX_V8a_OMXzR0EHR9R6jgdqrOOF4daGU96Sr_P6q
     *   Jp6IcmD3HP99Obi1PRs-cwh3LO-p146waJ8IhehcwL7F09JdijmBqkvPeB2T9CJ
     *   NqeGpe-gccMg4vfKjkM8FcGvnzZUN4_KSP0aAp1tOJ1zZwgjxqGByKHiOtX7Tpd
     *   QyHE5lcMiKPXfEIQILVq0pc_E2DzL7emopWoaoZTF_m0_N0YzFC6g6EJbOEoRoS
     *   K5hoDalrcvRYLSrQAZZKflyuVCyixEoV9GfNQC3_osjzw2PAithfubEEBLuVVk4
     *   XUVrWOLrLl0nx7RkKU8NXNHq-rvKMzqg"
     *  }
     * </pre>
     * </p>
     * 
     * </hr>
     * </br>
     * 
     * <p>
     * https://openid.net/specs/openid-connect-core-1_0.html#RefreshingAccessToken</br>
     * 
     * Request:
     * 
     * <pre>
     *  POST /token HTTP/1.1
     *  Host: server.example.com
     *  Content-Type: application/x-www-form-urlencoded
     *  
     *  client_id=s6BhdRkqt3
     *  &client_secret=some_secret12345
     *  &grant_type=refresh_token
     *  &refresh_token=8xLOxBtZp8
     *  &scope=openid%20profile
     * </pre>
     * 
     * Response:
     * 
     * <pre>
     *  HTTP/1.1 200 OK
     *  Content-Type: application/json
     *  Cache-Control: no-store
     *  Pragma: no-cache
     *  
     *  {
     *  "access_token": "TlBN45jURg",
     *  "token_type": "Bearer",
     *  "refresh_token": "9yNOxJtZa5",
     *  "expires_in": 3600
     *  }
     * </pre>
     * </p>
     * 
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_CORE_TOKEN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> token(
            @RequestParam String grant_type,
            @RequestParam String code,
            @RequestParam String redirect_uri,
            @RequestParam(required = false) String client_id,
            @RequestParam(required = false) String client_secret,
            @RequestParam(required = false) String refresh_token,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String device_code,
            @RequestParam(required = false) String code_verifier,
            @RequestHeader(name = "Authorization", required = false) String auth,
            UriComponentsBuilder uriBuilder,
            HttpServletRequest req) throws Exception {

        log.info("called:token '{}' from '{}', grant_type={} code={} redirect_uri={} client_id={}",
                URI_IAM_OIDC_ENDPOINT_CORE_TOKEN, req.getRemoteHost(), grant_type, code, redirect_uri, client_id);

        StandardGrantType grantType = StandardGrantType.safeOf(grant_type);
        if (isNull(grantType)) {
            return wrapErrorRfc6749("unsupported_grant_type",
                    format("The grant_type is not valid, must be one of '%s'", StandardGrantType.getNames()));
        }
        String iss = getIss(uriBuilder);

        // load configuration.
        V1OidcClientConfig clientConfig = oidcAuthingHandler.loadClientConfig(client_id);
        if (isNull(clientConfig)) {
            return wrapErrorRfc6749("invalid_request", "The client_id is no vaild");
        }

        // response tokens
        switch (grantType) {
        case authorization_code: // oauth2+v1-oidc
            return doTokenWithAuthorizationCode(clientConfig, client_id, redirect_uri, code, code_verifier);
        // case implicit: // oauth2+v1-oidc, see:[#mark1,#mark2]
        case refresh_token: // oauth2
            return doTokenWithRefreshToken(clientConfig, client_id, redirect_uri, refresh_token);
        case password: // oauth2
            return doTokenWithPassword(clientConfig, client_id, client_secret, redirect_uri, scope, auth, iss);
        case client_credentials: // oauth2
            return doTokenWithClientCredentials(clientConfig, client_id, client_secret, redirect_uri, scope, iss, uriBuilder);
        case device_code: // oauth2
            return doTokenWithDeviceCode(clientConfig, grant_type, client_id, client_secret, device_code, iss);
        default:
            return wrapErrorRfc6749("invalid_request", "grant_type not valid");
        }
    }

    /**
     * Provides information about a supplied access token. </br>
     * </br>
     * E.g: https://developer.okta.com/docs/reference/api/oidc/#introspect
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_CORE_INTROSPECT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> introspect(
            @RequestHeader("Authorization") String auth,
            @RequestParam String token,
            HttpServletRequest req) {

        log.info("called:introspect '{}' from '{}', token={}, auth={} ", URI_IAM_OIDC_ENDPOINT_CORE_INTROSPECT,
                req.getRemoteHost(), token, auth);

        String access_token = toDetermineAccessToken(auth, token);
        V1AccessTokenInfo accessTokenInfo = oidcAuthingHandler.loadAccessToken(access_token);
        if (isNull(accessTokenInfo)) {
            log.error("No found accessToken info by '{}'", token);
            return ResponseEntity.ok().body(V1Introspection.builder().active(false).build());
        } else {
            log.info("Found token for sub='{}', scope='{}'", accessTokenInfo.getUser().getSub(), accessTokenInfo.getScope());
            // https://tools.ietf.org/html/rfc7662#section-2.2
            V1Introspection introspect = V1Introspection.builder()
                    .iss(accessTokenInfo.getIss())
                    .sub(accessTokenInfo.getUser().getSub())
                    .active(true)
                    .scope(accessTokenInfo.getScope())
                    .client_id(accessTokenInfo.getClientId())
                    .username(accessTokenInfo.getUser().getSub())
                    .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                    .exp(MILLISECONDS.toSeconds(accessTokenInfo.getExpirationAt().getTime()))
                    .iat(MILLISECONDS.toSeconds(accessTokenInfo.getCreateAt()))
                    // Subtract clock skew by default.
                    // see:https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.5
                    .nbf(MILLISECONDS.toSeconds(accessTokenInfo.getExpirationAt().getTime() - 60_000))
                    .build();
            return ResponseEntity.ok().body(introspect);
        }
    }

    /**
     * Provides claims about a user. Requires a valid access token.
     * https://openid.net/specs/openid-connect-core-1_0.html#UserInfoResponseValidation</br>
     * 
     * Request:
     * 
     * <pre>
     *  GET /userinfo HTTP/1.1
     *  Host: server.example.com
     *  Authorization: Bearer SlAV32hkKG
     * </pre>
     * 
     * Response:
     * 
     * <pre>
     *  HTTP/1.1 200 OK
     *  Content-Type: application/json
     *  
     *  {
     *  "sub": "248289761001",
     *  "name": "Jane Doe",
     *  "given_name": "Jane",
     *  "family_name": "Doe",
     *  "preferred_username": "j.doe",
     *  "email": "janedoe@example.com",
     *  "picture": "http://example.com/janedoe/me.jpg"
     *  }
     * </pre>
     * </p>
     * 
     * </hr>
     * </br>
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_CORE_USERINFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(allowedHeaders = { "Authorization", "Content-Type" })
    public ResponseEntity<?> userinfo(
            @RequestHeader("Authorization") String auth,
            @RequestParam(required = false) String access_token,
            HttpServletRequest req) {

        log.info("called:userinfo '{}' from '{}' bearerToken={}, access_token={}", URI_IAM_OIDC_ENDPOINT_CORE_USERINFO,
                req.getRemoteHost(), auth, access_token);

        String accessToken = toDetermineAccessToken(auth, access_token);
        V1AccessTokenInfo accessTokenInfo = oidcAuthingHandler.loadAccessToken(accessToken);
        if (isNull(accessTokenInfo)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("access_token is not valid");
        }
        V1OidcUserClaims user = accessTokenInfo.getUser();
        V1OidcUserClaims oidcUser = V1OidcUserClaims.builder().sub(user.getSub()).build();
        if (StandardScope.profile.containsIn(accessTokenInfo.getScope())) {
            oidcUser = oidcUser.withName(user.getName())
                    .withGiven_name(user.getGiven_name())
                    .withFamily_name(user.getFamily_name())
                    .withMiddleName(user.getMiddleName())
                    .withNickname(user.getNickname())
                    .withPreferred_username(user.getPreferred_username())
                    .withGender(user.getGender())
                    .withLocale(user.getLocale())
                    .withBirthdate(user.getBirthdate())
                    .withPicture(user.getPicture())
                    .withZoneinfo(user.getZoneinfo())
                    .withUpdated_at(user.getUpdated_at());
        }
        if (StandardScope.email.containsIn(accessTokenInfo.getScope())) {
            oidcUser = oidcUser.withEmail(user.getEmail()).withEmail_verified(user.getEmail_verified());
        }
        if (StandardScope.address.containsIn(accessTokenInfo.getScope())) {
            oidcUser = oidcUser.withAddress(user.getAddress());
        }
        if (StandardScope.phone.containsIn(accessTokenInfo.getScope())) {
            oidcUser = oidcUser.withPhone_number(user.getPhone_number())
                    .withPhone_number_verified(user.getPhone_number_verified());
        }
        return ResponseEntity.ok().body(oidcUser);
    }

    private ResponseEntity<?> doAuthorizeWithSuccess(
            V1OidcClientConfig clientConfig,
            V1OidcUserClaims user,
            String client_id,
            String redirect_uri,
            String response_type,
            String scope,
            String state,
            String nonce,
            String display,
            String prompt,
            String max_age,
            String ui_locales,
            String id_token_hint,
            String login_hint,
            String acr_values,
            String response_mode,
            String code_challenge,
            String code_challenge_method,
            String auth,
            UriComponentsBuilder uriBuilder,
            HttpServletRequest req) throws Exception {

        // Apply challenge default.
        if (!isBlank(code_challenge) && isBlank(code_challenge_method)) {
            code_challenge_method = ChallengeAlgorithmType.getDefault().name();
        }

        // Gets issue.
        String iss = getIss(uriBuilder);

        // see:https://openid.net/specs/openid-connect-core-1_0.html#HybridFlowAuth
        MultiValueMap<String, String> redirectParams = new LinkedMultiValueMap<>(8);
        redirectParams.add("token_type", KEY_IAM_OIDC_TOKEN_TYPE_BEARER);
        redirectParams.add("state", urlEncode(state));

        // Authorization code flow
        // see:https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth
        if (StandardResponseType.code.containsIn(response_type)) {
            // see:https://openid.net/specs/openid-connect-core-1_0.html#codeExample
            // Check challenge method supported.
            if (!isBlank(code_challenge_method) && isNull(ChallengeAlgorithmType.safeOf(code_challenge_method))) {
                return wrapErrorRfc6749("unsupported_code_challenge_method",
                        format("code_challenge_method must contains is '%s'", ChallengeAlgorithmType.getNames()));
            }
            String code = createAuthorizationCode(client_id, redirect_uri, user, iss, scope, code_challenge,
                    code_challenge_method, nonce);
            redirectParams.add("code", code);
        }
        // Implicit flow, [#mark1]
        // see:https://openid.net/specs/openid-connect-core-1_0.html#ImplicitFlowAuth
        if (StandardResponseType.id_token.containsIn(response_type)) {
            // see:https://openid.net/specs/openid-connect-core-1_0.html#id_tokenExample
            String id_token = createIdToken(clientConfig, iss, user, client_id, nonce);
            redirectParams.add("id_token", id_token);
        }
        if (StandardResponseType.token.containsIn(response_type)) {
            // see:https://openid.net/specs/openid-connect-core-1_0.html#code-tokenExample
            V1AccessTokenInfo accessTokenInfo = createAccessTokenInfo(clientConfig, iss, user, client_id, redirect_uri, scope);
            redirectParams.add("access_token", accessTokenInfo.getAccessToken());
        }

        // OPTIONAL. Informs the Authorization Server of the mechanism
        // to be used for returning parameters from the Authorization
        // Endpoint. This use of this parameter is NOT RECOMMENDED when
        // the Response Mode that would be requested is the default mode
        // specified for the Response Type.
        // e.g to
        // see:https://docs.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-auth-code-flow#request-an-authorization-code
        StandardResponseType responseType = StandardResponseType.of(response_type);
        StandardResponseMode responseMode = StandardResponseMode.safeOf(response_mode);
        if (responseMode == StandardResponseMode.form_post && StandardResponseType.isAuthorizationCodeFlow(responseType)) {
            return wrapResponseFromPost(redirect_uri, state, redirectParams.getFirst("code"), redirectParams.getFirst("id_token"),
                    redirectParams.getFirst("access_token"));
        } else {
            String paramUri = UriComponentsBuilder.newInstance().queryParams(redirectParams).build().toUriString().substring(1);
            if (StandardResponseType.isImplicitFlow(responseType)) { // [#mark2]
                // Implicit flow are not allowed to use query.
                responseMode = (responseMode == StandardResponseMode.query) ? StandardResponseMode.fragment : responseMode;
                // The default for implicit flow is fragment.
                responseMode = isNull(responseMode) ? StandardResponseMode.fragment : responseMode;
            }
            if (responseMode == StandardResponseMode.fragment) {
                // The protocol specifications stipulates that the response
                // redirection parameters are spliced into the fragment
                // parts, which can ensure maximum security, because the
                // parameters of the fragment are not sent to the client
                // application.
                String location = redirect_uri.concat("#").concat(paramUri);
                return ResponseEntity.status(HttpStatus.FOUND).header("Location", location).build();
            }
            // others are according handled to query.
            String location = redirect_uri.concat("?").concat(paramUri);
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", location).build();
        }
    }

    private ResponseEntity<?> doTokenWithAuthorizationCode(
            V1OidcClientConfig clientConfig,
            String client_id,
            String redirect_uri,
            String code,
            String code_verifier) throws Exception {
        if (!clientConfig.isStandardFlowEnabled()) {
            return wrapErrorRfc6749("invalid_request", "disabled standard authorization code grant");
        }

        V1AuthorizationCodeInfo codeInfo = oidcAuthingHandler.loadAuthorizationCode(code);
        if (isNull(codeInfo)) {
            return wrapErrorRfc6749("invalid_grant", "The code not valid");
        }
        if (!StringUtils.equals(redirect_uri, codeInfo.getRedirectUri())) {
            return wrapErrorRfc6749("invalid_request", "The redirect_uri not valid");
        }

        // (oauth2)Verify code challenge
        if (!isBlank(codeInfo.getCodeChallenge())) {
            // check PKCE
            if (isBlank(code_verifier)) {
                return wrapErrorRfc6749("invalid_request", "The code_verifier missing");
            }
            if (ChallengeAlgorithmType.plain.name().equals(codeInfo.getCodeChallengeMethod())) {
                if (!codeInfo.getCodeChallenge().equals(code_verifier)) {
                    log.warn("code_verifier {} does not match code_challenge {}", code_verifier, codeInfo.getCodeChallenge());
                    return wrapErrorRfc6749("invalid_request", "The code_verifier not correct");
                }
            } else {
                String hashedVerifier = Base64URL
                        .encode(doDigestHash(ChallengeAlgorithmType.of(codeInfo.getCodeChallengeMethod()), code_verifier))
                        .toString();
                if (!codeInfo.getCodeChallenge().equals(hashedVerifier)) {
                    log.warn("code_verifier {} hashed using S256 to {} does not match code_challenge {}", code_verifier,
                            hashedVerifier, codeInfo.getCodeChallenge());
                    return wrapErrorRfc6749("invalid_request", "The code_verifier not correct");
                }
                log.debug("code_verifier OK");
            }
        }

        V1AccessTokenInfo accessTokenInfo = createAccessTokenInfo(clientConfig, codeInfo.getIss(), codeInfo.getUser(),
                codeInfo.getClientId(), codeInfo.getRedirectUri(), codeInfo.getScope());
        String id_token = createIdToken(clientConfig, codeInfo.getIss(), codeInfo.getUser(), codeInfo.getClientId(),
                codeInfo.getNonce());
        V1AccessToken accessToken = V1AccessToken.builder()
                .access_token(accessTokenInfo.getAccessToken())
                .refresh_token(accessTokenInfo.getRefreshToken())
                .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                .scope(codeInfo.getScope())
                .expires_in(oidcAuthingHandler.loadClientConfig(client_id).getAccessTokenExpirationSeconds())
                .id_token(id_token)
                .build();
        return ResponseEntity.ok(accessToken);
    }

    private ResponseEntity<?> doTokenWithRefreshToken(
            V1OidcClientConfig clientConfig,
            String client_id,
            String redirect_uri,
            String refresh_token) throws Exception {

        if (!clientConfig.isUseRefreshTokenEnabled()) {
            return wrapErrorRfc6749("invalid_request", "The refresh token grant disabled.");
        }
        if (isBlank(refresh_token)) {
            return wrapErrorRfc6749("invalid_request", "The refresh_token missing");
        }
        // Check refresh_token valid
        V1AccessTokenInfo lastAccessTokenInfo = oidcAuthingHandler.loadRefreshToken(refresh_token, true);
        if (isNull(lastAccessTokenInfo)) {
            return wrapErrorRfc6749("invalid_request", "The refresh_token not valid");
        }
        // Check redirect_uri valid
        if (!StringUtils.equals(redirect_uri, lastAccessTokenInfo.getRedirectUri())) {
            return wrapErrorRfc6749("invalid_request", "The redirect_uri not valid");
        }

        // New access_token
        V1AccessTokenInfo accessTokenInfo = createAccessTokenInfo(clientConfig, lastAccessTokenInfo.getIss(),
                lastAccessTokenInfo.getUser(), lastAccessTokenInfo.getClientId(), redirect_uri, lastAccessTokenInfo.getScope());
        V1AccessToken accessToken = V1AccessToken.builder()
                .access_token(accessTokenInfo.getAccessToken())
                .refresh_token(accessTokenInfo.getRefreshToken())
                .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                .expires_in(oidcAuthingHandler.loadClientConfig(client_id).getAccessTokenExpirationSeconds())
                .scope(lastAccessTokenInfo.getScope())
                .build();
        return ResponseEntity.ok(accessToken);
    }

    /**
     * e.g to
     * see:https://docs.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth-ropc
     */
    private ResponseEntity<?> doTokenWithPassword(
            V1OidcClientConfig clientConfig,
            String client_id,
            String client_secret,
            String redirect_uri,
            String scope,
            String auth,
            String iss) throws Exception {
        if (!clientConfig.isDirectAccessGrantsEnabled()) {
            return wrapErrorRfc6749("invalid_request", "The password credential grant has been disabled");
        }
        String[] creds = new String(Base64.decodeBase64(auth.split(" ")[1])).split(":", 2);
        String username = creds[0];
        String password = creds[1];
        V1OidcUserClaims user = oidcAuthingHandler.getV1OidcUserClaimsByUser(username);
        if (!oidcAuthingHandler.validate(user, password)) {
            return wrapUnauthentication();
        }

        // New access_token
        V1AccessTokenInfo accessTokenInfo = createAccessTokenInfo(clientConfig, iss, user, client_id, redirect_uri, scope);
        V1AccessToken accessToken = V1AccessToken.builder()
                .access_token(accessTokenInfo.getAccessToken())
                .refresh_token(accessTokenInfo.getRefreshToken())
                .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                .expires_in(oidcAuthingHandler.loadClientConfig(client_id).getAccessTokenExpirationSeconds())
                .scope(scope)
                .build();
        return ResponseEntity.ok(accessToken);
    }

    /**
     * e.g to
     * see:https://docs.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow
     */
    private ResponseEntity<?> doTokenWithClientCredentials(
            V1OidcClientConfig clientConfig,
            String client_id,
            String client_secret,
            String redirect_uri,
            String scope,
            String iss,
            UriComponentsBuilder uriBuilder) throws Exception {

        // Check request scope.
        // The value passed for the scope parameter in this request should be
        // the resource identifier (application ID URI) of the resource you
        // want, affixed with the .default suffix. For the IAM example, the
        // value is https://iam.example.com/.default
        if (!StringUtils.equals(scope, getDefaultClientCredentialsScope(uriBuilder))) {
            return wrapErrorRfc6749("invalid_scope", "The scope is not valid");
        }
        // Check client secrets.
        if (isEmpty(clientConfig.getClientSecrets())) {
            return wrapErrorRfc6749("invalid_client", "No client credentials found");
        }

        // Verify client credentials.
        // TODO use hashing match?
        boolean certificated = safeList(clientConfig.getClientSecrets()).stream()
                .anyMatch(s -> StringUtils.equals(s.getSecret(), client_secret));
        if (!certificated) {
            return wrapErrorRfc6749("invalid_client", "The client_secret is not valid");
        }

        // Load user claims by client_id.
        V1OidcUserClaims user = oidcAuthingHandler.getV1OidcUserClaimsByClientId(client_id);

        // New access token.
        V1AccessTokenInfo accessTokenInfo = createAccessTokenInfo(clientConfig, iss, user, client_id, redirect_uri, scope);
        V1AccessToken accessToken = V1AccessToken.builder()
                .access_token(accessTokenInfo.getAccessToken())
                .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                .expires_in(clientConfig.getAccessTokenExpirationSeconds())
                .scope(scope)
                .build();
        if (clientConfig.isUseRefreshTokenForClientCredentialsGrantEnabled()) {
            accessToken.setRefresh_token(accessTokenInfo.getRefreshToken());
        }
        return ResponseEntity.ok(accessToken);
    }

    /**
     * Like the scan login polling implementation. </br>
     * </br>
     * for example: </br>
     * </br>
     * 
     * https://developers.google.com/identity/protocols/oauth2/limited-input-device
     * </br>
     * </br>
     * https://docs.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-device-code#device-authorization-response
     * </br>
     * </br>
     * https://www.oauth.com/playground/device-code.html </br>
     * 
     * @since oauth2.1
     */
    private ResponseEntity<?> doTokenWithDeviceCode(
            V1OidcClientConfig clientConfig,
            String grant_type,
            String client_id,
            String client_secret,
            String device_code,
            String iss) throws Exception {

        V1DeviceCodeInfo codeInfo = oidcAuthingHandler.loadDeviceCode(device_code, true);
        if (isNull(codeInfo)) {
            return wrapErrorRfc6749("invalid_request", "The device_code is not valid or expired");
        }

        // New access_token
        V1AccessTokenInfo accessTokenInfo = createAccessTokenInfo(clientConfig, iss, null, client_id, "", codeInfo.getScope());
        V1AccessToken accessToken = V1AccessToken.builder()
                .access_token(accessTokenInfo.getAccessToken())
                .refresh_token(accessTokenInfo.getRefreshToken())
                .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                .expires_in(oidcAuthingHandler.loadClientConfig(client_id).getAccessTokenExpirationSeconds())
                .scope(codeInfo.getScope())
                .build();

        return ResponseEntity.ok(accessToken);
    }

    private String createAuthorizationCode(
            String client_id,
            String redirect_uri,
            V1OidcUserClaims user,
            String iss,
            String scope,
            String code_challenge,
            String code_challenge_method,
            String nonce) {
        byte[] bytes = new byte[16];
        getRandom().nextBytes(bytes);
        String code = Base64URL.encode(bytes).toString();

        V1AuthorizationCodeInfo codeInfo = V1AuthorizationCodeInfo.builder()
                .user(user)
                .code(code)
                .clientId(client_id)
                .redirectUri(redirect_uri)
                .iss(iss)
                .scope(scope)
                .nonce(nonce)
                .codeChallenge(code_challenge)
                .codeChallengeMethod(code_challenge_method)
                .build();
        oidcAuthingHandler.putAuthorizationCode(code, codeInfo);

        log.info("issuing authorization code={}, codeInfo={}", code, codeInfo);
        return code;
    }

    private V1AccessTokenInfo createAccessTokenInfo(
            V1OidcClientConfig clientConfig,
            String iss,
            V1OidcUserClaims user,
            String client_id,
            String redirect_uri,
            String scope) throws JOSEException {

        // Create JWT claims
        long now = FastTimeClock.currentTimeMillis();
        Date expiration = new Date(now + clientConfig.getAccessTokenExpirationSeconds() * 1000L);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getSub())
                .issuer(iss)
                .audience(client_id)
                .issueTime(new Date())
                .expirationTime(expiration)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", scope)
                .build();
        // Create JWT token
        SignedJWT jwt = new SignedJWT(loadJWKConfig().getJwsHeader(), jwtClaimsSet);
        // Sign the JWT token
        jwt.sign(loadJWKConfig().getSigner());
        String access_token = jwt.serialize();

        // Generate refresh token.
        String refresh_token = createRefreshToken(clientConfig, access_token, iss, user, client_id, scope);

        V1AccessTokenInfo accessTokenInfo = V1AccessTokenInfo.builder()
                .user(user)
                .iss(iss)
                .clientId(client_id)
                .redirectUri(redirect_uri)
                .scope(scope)
                .accessToken(access_token)
                .refreshToken(refresh_token)
                .createAt(now)
                .expirationAt(expiration)
                .build();

        oidcAuthingHandler.putAccessToken(access_token, accessTokenInfo);
        oidcAuthingHandler.putRefreshToken(refresh_token, accessTokenInfo);
        return accessTokenInfo;
    }

    private String createRefreshToken(
            V1OidcClientConfig clientConfig,
            String access_token,
            String iss,
            V1OidcUserClaims user,
            String client_id,
            String scope) throws JOSEException {

        // create JWT claims
        long now = FastTimeClock.currentTimeMillis();
        Date expiration = new Date(now + clientConfig.getRefreshTokenExpirationSeconds() * 1000L);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getSub())
                .issuer(iss)
                .audience(client_id)
                .issueTime(new Date())
                .expirationTime(expiration)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", scope)
                .claim("access_token", access_token)
                .build();
        // create JWT token
        SignedJWT jwt = new SignedJWT(loadJWKConfig().getJwsHeader(), jwtClaimsSet);
        // sign the JWT token
        jwt.sign(loadJWKConfig().getSigner());
        return jwt.serialize();
    }

    private String createIdToken(
            V1OidcClientConfig clientConfig,
            String iss,
            V1OidcUserClaims user,
            String client_id,
            String nonce) throws NoSuchAlgorithmException, JOSEException {

        // compute at_hash
        byte[] hashed = doDigestHash(TokenSignAlgorithmType.of(clientConfig.getIdTokenSignAlg()), user.getSub());
        byte[] hashedLeftHalf = Arrays.copyOf(hashed, hashed.length / 2);
        Base64URL encodedHash = Base64URL.encode(hashedLeftHalf);
        // create JWT claims
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getSub())
                .issuer(iss)
                .audience(client_id)
                .issueTime(new Date())
                .expirationTime(new Date(currentTimeMillis() + clientConfig.getAccessTokenExpirationSeconds() * 1000L))
                .jwtID(UUID.randomUUID().toString())
                .claim(KEY_IAM_OIDC_CLAIMS_EXT_NONCE, nonce)
                .claim(KEY_IAM_OIDC_CLAIMS_EXT_AT_HASH, encodedHash)
                .build();
        // create JWT token
        SignedJWT jwt = new SignedJWT(loadJWKConfig().getJwsHeader(), jwtClaimsSet);
        // sign the JWT token
        jwt.sign(loadJWKConfig().getSigner());
        return jwt.serialize();
    }

    private String getIss(UriComponentsBuilder uriBuilder) {
        return uriBuilder.replacePath("/").build().encode().toUriString();
    }

    private String getDefaultClientCredentialsScope(UriComponentsBuilder uriBuilder) {
        return getIss(uriBuilder).concat(".default");
    }

    private V1OidcClientConfig.JWKConfig loadJWKConfig() {
        return oidcAuthingHandler.loadJWKConfig(getCurrentNamespaceLocal().get());
    }

}
