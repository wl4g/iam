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
package com.wl4g.iam.web.oidc.v1;

import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_EXT_AT_HASH;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_EXT_NONCE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_TOKEN_TYPE_BEARER;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_AUTHORIZE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CERTS;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_INTROSPECTION;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_METADATA;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_TOKEN;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_USERINFO;
import static com.wl4g.infra.common.codec.Encodes.urlEncode;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.wl4g.iam.annotation.V1OidcServerController;
import com.wl4g.iam.common.constant.V1OidcIAMConstants;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardClaims;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardDisplay;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardGrantType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardResponseMode;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardResponseType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardScopeType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardSignAlgorithm;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardSubjectType;
import com.wl4g.iam.common.model.oidc.v1.V1AccessToken;
import com.wl4g.iam.common.model.oidc.v1.V1AccessTokenInfo;
import com.wl4g.iam.common.model.oidc.v1.V1AuthorizationCodeInfo;
import com.wl4g.iam.common.model.oidc.v1.V1IntrospectionAccessToken;
import com.wl4g.iam.common.model.oidc.v1.V1MetadataEndpointModel;
import com.wl4g.iam.common.model.oidc.v1.V1OidcUserClaims;
import com.wl4g.iam.handler.oidc.v1.V1OidcAuthenticatingHandler;
import com.wl4g.iam.web.oidc.BasedOidcServerAuthingController;

/**
 * IAM V1-OIDC authentication controller.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-18 v1.0.0
 * @since v3.0.0
 * @see https://openid.net/specs/openid-connect-core-1_0.html#AuthResponseValidation
 */
@V1OidcServerController
public class V1OidcServerAuthingController extends BasedOidcServerAuthingController {

    private final SecureRandom random = new SecureRandom();

    private @Autowired V1OidcAuthenticatingHandler oidcHandler;

    private JWSSigner signer;
    private JWKSet pubJWKSet;
    private JWSHeader jwsHeader;

    @PostConstruct
    public void init() throws IOException, ParseException, JOSEException {
        log.info("Initializing OIDC JWK ...");
        // TODO remove, use from DB config
        JWK jwk = V1OidcClientConfig.DEFAULT_JWKSET.getKeys().get(0);
        this.signer = new RSASSASigner((RSAKey) jwk);
        this.pubJWKSet = V1OidcClientConfig.DEFAULT_JWKSET.toPublicJWKSet();
        this.jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse("S256")).keyID(jwk.getKeyID()).build();
    }

    /**
     * Provides OIDC metadata. See the spec at
     * 
     * @see https://openid.net/specs/openid-connect-core-1_0.html#SelfIssuedDiscovery
     * @see https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfigurationResponse
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_METADATA, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> metadata(UriComponentsBuilder uriBuilder, HttpServletRequest req) {
        log.info("called:metadata '{}' from '{}'", URI_IAM_OIDC_ENDPOINT_METADATA, req.getRemoteHost());

        String prefix = uriBuilder.replacePath(null).build().encode().toUriString();
        // https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata
        // https://tools.ietf.org/html/rfc8414#section-2
        V1MetadataEndpointModel metadata = V1MetadataEndpointModel.builder()
                .issuer(prefix.concat("/")) // REQUIRED
                // REQUIRED
                .authorization_endpoint(prefix.concat(URI_IAM_OIDC_ENDPOINT_AUTHORIZE))
                // REQUIRED
                .token_endpoint(prefix.concat(URI_IAM_OIDC_ENDPOINT_TOKEN))
                // RECOMMENDED
                .userinfo_endpoint(prefix.concat(URI_IAM_OIDC_ENDPOINT_USERINFO))
                // REQUIRED
                .jwks_uri(prefix.concat(URI_IAM_OIDC_ENDPOINT_CERTS))
                .introspection_endpoint(prefix.concat(URI_IAM_OIDC_ENDPOINT_INTROSPECTION))
                // REQUIRED
                .scopes_supported(StandardScopeType.getNames())
                // REQUIRED
                .response_types_supported(StandardResponseType.getNames())
                // OPTIONAL
                .grant_types_supported(StandardGrantType.getNames())
                // REQUIRED
                .subject_types_supported(StandardSubjectType.getNames())
                // REQUIRED
                .id_token_signing_alg_values_supported(null)
                .claims_supported(StandardClaims.getNames())
                .display_values_supported(StandardDisplay.getNames())
                .service_documentation(config.getV1Oidc().getServiceDocumentation())
                // PKCE support advertised
                .code_challenge_methods_supported(null)
                .build();
        return ResponseEntity.ok().body(metadata);
    }

    /**
     * Provides JSON Web Key Set containing the public part of the key used to
     * sign ID tokens.
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_CERTS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> certs(HttpServletRequest req) {
        log.info("called:certs '{}' from '{}'", URI_IAM_OIDC_ENDPOINT_CERTS, req.getRemoteHost());
        return ResponseEntity.ok().body(pubJWKSet.toString());
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
     *            Oauth2 security challenge code docs see to:
     *            https://blog.csdn.net/weixin_34415923/article/details/89691037
     *            </br>
     * @param code_challenge_method
     *            Challenge code algorithm. </br>
     * @param auth
     * @param uriBuilder
     * @param req
     * @return
     * @throws JOSEException
     * @throws NoSuchAlgorithmException
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_AUTHORIZE, method = RequestMethod.GET)
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
            @RequestParam(required = false) String code_challenge_method,
            @RequestHeader(name = "Authorization", required = false) String auth,
            UriComponentsBuilder uriBuilder,
            HttpServletRequest req) throws JOSEException, NoSuchAlgorithmException {

        log.info("called:authorize '{}' from '{}', scope={} response_type={} client_id={} redirect_uri={}",
                URI_IAM_OIDC_ENDPOINT_AUTHORIZE, req.getRemoteHost(), scope, response_type, client_id, redirect_uri);

        if (isBlank(auth)) {
            log.info("User and password not provided. scope={} response_type={} client_id={} redirect_uri={}", scope,
                    response_type, client_id, redirect_uri);
            return wrapUnauthentication();
        } else {
            String[] creds = new String(Base64.getDecoder().decode(auth.split(" ")[1])).split(":", 2);
            String username = creds[0];
            String password = creds[1];
            V1OidcUserClaims user = oidcHandler.getV1OidcUser(username);
            if (username.equals("root")) { // for test
                // if (oidcHandler.validate(user, password)) {
                log.info("Password {} for user {} is correct.", password, username);

                String iss = uriBuilder.replacePath("/").build().encode().toUriString();

                // Check response_type valid
                if (!StandardResponseType.isValid(response_type)) {
                    String url = format("%s#error=unsupported_response_type", redirect_uri);
                    return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
                }

                // see:https://openid.net/specs/openid-connect-core-1_0.html#HybridFlowAuth
                Map<String, String> redirectParams = new HashMap<>(8);
                // fragmentUri.queryParam("token_type",KEY_IAM_OIDC_TOKEN_TYPE_BEARER);
                // fragmentUri.queryParam("state", urlEncode(state));
                redirectParams.put("token_type", KEY_IAM_OIDC_TOKEN_TYPE_BEARER);
                redirectParams.put("state", urlEncode(state));

                // Authorization code flow
                // see:https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth
                if (StandardResponseType.containtsInSingle(response_type, StandardResponseType.code)) {
                    // see:https://openid.net/specs/openid-connect-core-1_0.html#codeExample
                    // Check challenge method supported.
                    if (!isBlank(code_challenge_method)
                            && !oidcHandler.loadClientConfig(client_id).getCodeChallengeMethodsSupported().contains(
                                    code_challenge_method)) {
                        return wrapErrorRFC6749("unsupported_code_challenge_method",
                                format("code_challenge_method must contains is '%s'",
                                        oidcHandler.loadClientConfig(client_id).getCodeChallengeMethodsSupported()));
                    }
                    String code = createAuthorizationCode(code_challenge, code_challenge_method, client_id, redirect_uri, user,
                            iss, scope, nonce);
                    // fragmentUri.queryParam("code", code);
                    redirectParams.put("code", code);
                }
                // Implicit flow
                // see:https://openid.net/specs/openid-connect-core-1_0.html#ImplicitFlowAuth
                if (StandardResponseType.containtsInSingle(response_type, StandardResponseType.token)) {
                    // see:https://openid.net/specs/openid-connect-core-1_0.html#code-tokenExample
                    V1AccessTokenInfo accessTokenInfo = createAccessTokenInfo(iss, user, client_id, scope);
                    // fragmentUri.queryParam("access_token",accessTokenInfo.getAccessToken());
                    redirectParams.put("access_token", accessTokenInfo.getAccessToken());
                }
                if (StandardResponseType.containtsInSingle(response_type, StandardResponseType.id_token)) {
                    // see:https://openid.net/specs/openid-connect-core-1_0.html#id_tokenExample
                    String id_token = createIdToken(iss, user, client_id, nonce);
                    // fragmentUri.queryParam("id_token", id_token);
                    redirectParams.put("id_token", id_token);
                }

                // OPTIONAL. Informs the Authorization Server of the mechanism
                // to be used for returning parameters from the Authorization
                // Endpoint. This use of this parameter is NOT RECOMMENDED when
                // the Response Mode that would be requested is the default mode
                // specified for the Response Type.
                if (StandardResponseMode.safeOf(response_mode) == StandardResponseMode.form_post) {
                    return wrapResponseFromPost("", state, redirectParams.get("code"), redirectParams.get("id_token"),
                            redirectParams.get("access_token"));
                } else {
                    // The protocol specifications stipulates that the response
                    // redirection parameters are spliced into the fragment
                    // parts, which can ensure maximum security, because the
                    // parameters of the fragment are not sent to the client
                    // application.
                    UriComponentsBuilder fragmentUri = UriComponentsBuilder.newInstance();
                    redirectParams.forEach((key, value) -> fragmentUri.queryParam(key, value));
                    String location = redirect_uri.concat("#").concat(fragmentUri.build().toString().substring(1));
                    return ResponseEntity.status(HttpStatus.FOUND).header("Location", location).build();
                }
            } else {
                log.info("Wrong user and password combination. scope={} response_type={} client_id={} redirect_uri={}", scope,
                        response_type, client_id, redirect_uri);
                return wrapUnauthentication();
            }
        }
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
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_TOKEN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> token(
            @RequestParam String grant_type,
            @RequestParam String code,
            @RequestParam String redirect_uri,
            @RequestParam(required = false) String refresh_token,
            @RequestParam(required = false) String client_id,
            @RequestParam(required = false) String client_secret,
            @RequestParam(required = false) String code_verifier,
            @RequestHeader(name = "Authorization", required = false) String auth,
            UriComponentsBuilder uriBuilder,
            HttpServletRequest req) throws Exception {

        log.info("called:token '{}' from '{}', grant_type={} code={} redirect_uri={} client_id={}", URI_IAM_OIDC_ENDPOINT_TOKEN,
                req.getRemoteHost(), grant_type, code, redirect_uri, client_id);

        StandardGrantType grantType = V1OidcIAMConstants.StandardGrantType.safeOf(grant_type);
        if (isNull(grantType)) {
            return wrapErrorRFC6749("unsupported_grant_type",
                    format("grant_type must be one of '%s'", V1OidcIAMConstants.StandardGrantType.getNames()));
        }
        V1AuthorizationCodeInfo codeInfo = oidcHandler.loadAuthorizationCode(code);
        if (isNull(codeInfo)) {
            return wrapErrorRFC6749("invalid_grant", "code not valid");
        }
        if (!StringUtils.equals(redirect_uri, codeInfo.getRedirect_uri())) {
            return wrapErrorRFC6749("invalid_request", "redirect_uri not valid");
        }

        // load OIDC client configuration.
        V1OidcClientConfig clientConfig = oidcHandler.loadClientConfig(client_id);

        // (oauth2)Verify code challenge
        if (!isBlank(codeInfo.getCodeChallenge())) {
            // check PKCE
            if (isBlank(code_verifier)) {
                return wrapErrorRFC6749("invalid_request", "code_verifier missing");
            }
            if (StandardSignAlgorithm.PLAIN.name().equals(codeInfo.getCodeChallengeMethod())) {
                if (!codeInfo.getCodeChallenge().equals(code_verifier)) {
                    log.warn("code_verifier {} does not match code_challenge {}", code_verifier, codeInfo.getCodeChallenge());
                    return wrapErrorRFC6749("invalid_request", "code_verifier not correct");
                }
            } else {
                String hashedVerifier = Base64URL
                        .encode(doDigestHash(StandardSignAlgorithm.of(codeInfo.getCodeChallengeMethod()), code_verifier))
                        .toString();
                if (!codeInfo.getCodeChallenge().equals(hashedVerifier)) {
                    log.warn("code_verifier {} hashed using S256 to {} does not match code_challenge {}", code_verifier,
                            hashedVerifier, codeInfo.getCodeChallenge());
                    return wrapErrorRFC6749("invalid_request", "code_verifier not correct");
                }
                log.debug("code_verifier OK");
            }
        }

        // response tokens
        switch (grantType) {
        case authorization_code: // oauth2+v1oidc
            return doTokenWithAuthorizationCode(client_id, clientConfig, codeInfo);
        case implicit: // oauth2+v1oidc
            return doTokenWithImplicit(clientConfig);
        case refresh_token: // oauth2
            return doTokenWithRefreshToken(client_id, refresh_token, clientConfig, codeInfo);
        case password: // oauth2
            return doTokenWithPassword(clientConfig);
        case client_credentials: // oauth2
            return doTokenWithClientCredentials(clientConfig);
        case device_code: // oauth2
            return doTokenWithDeviceCode(clientConfig);
        default:
            return wrapErrorRFC6749("invalid_request", "grant_type not valid");
        }
    }

    /**
     * Provides information about a supplied access token.
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_INTROSPECTION, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> introspection(
            @RequestHeader("Authorization") String auth,
            @RequestParam String token,
            HttpServletRequest req) {

        log.info("called:introspection '{}' from '{}', token={}, auth={} ", URI_IAM_OIDC_ENDPOINT_INTROSPECTION,
                req.getRemoteHost(), token, auth);

        String access_token = toDetermineAccessToken(auth, token);
        V1AccessTokenInfo accessTokenInfo = oidcHandler.loadAccessToken(access_token);
        if (isNull(accessTokenInfo)) {
            log.error("No found accessToken info by '{}'", token);
            return ResponseEntity.ok().body(V1IntrospectionAccessToken.builder().active(true).build());
        } else {
            log.info("Found token for user {}, releasing scopes: {}", accessTokenInfo.getUser().getSub(),
                    accessTokenInfo.getScope());
            // https://tools.ietf.org/html/rfc7662#section-2.2 for all claims
            V1IntrospectionAccessToken accessToken = V1IntrospectionAccessToken.builder()
                    .active(true)
                    .scope(accessTokenInfo.getScope())
                    .client_id(accessTokenInfo.getClientId())
                    .username(accessTokenInfo.getUser().getSub())
                    .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                    .exp(accessTokenInfo.getExpiration().toInstant().toEpochMilli())
                    .sub(accessTokenInfo.getUser().getSub())
                    .iss(accessTokenInfo.getIss())
                    .build();
            return ResponseEntity.ok().body(accessToken);
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
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_USERINFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(allowedHeaders = { "Authorization", "Content-Type" })
    public ResponseEntity<?> userinfo(
            @RequestHeader("Authorization") String auth,
            @RequestParam(required = false) String access_token,
            HttpServletRequest req) {

        log.info("called:userinfo '{}' from '{}' bearerToken={}, access_token={}", URI_IAM_OIDC_ENDPOINT_USERINFO,
                req.getRemoteHost(), auth, access_token);

        String accessToken = toDetermineAccessToken(auth, access_token);
        V1AccessTokenInfo accessTokenInfo = oidcHandler.loadAccessToken(accessToken);
        if (isNull(accessTokenInfo)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("access_token is not valid");
        }
        Set<String> scopes = toSpaceSeparatedParams(accessTokenInfo.getScope());
        V1OidcUserClaims user = accessTokenInfo.getUser();
        V1OidcUserClaims oidcUser = V1OidcUserClaims.builder().sub(user.getSub()).build();
        if (scopes.contains(StandardScopeType.profile.name())) {
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
        if (scopes.contains(StandardScopeType.email.name())) {
            oidcUser = oidcUser.withEmail(user.getEmail()).withEmail_verified(user.getEmail_verified());
        }
        if (scopes.contains(StandardScopeType.address.name())) {
            oidcUser = oidcUser.withAddress(user.getAddress());
        }
        if (scopes.contains(StandardScopeType.phone.name())) {
            oidcUser = oidcUser.withPhone_number(user.getPhone_number())
                    .withPhone_number_verified(user.getPhone_number_verified());
        }
        return ResponseEntity.ok().body(oidcUser);
    }

    private ResponseEntity<?> doTokenWithAuthorizationCode(
            String client_id,
            V1OidcClientConfig clientConfig,
            V1AuthorizationCodeInfo codeInfo) throws Exception {
        if (!clientConfig.isStandardFlowEnabled()) {
            return wrapErrorRFC6749("invalid_request", "disabled standard authorization code grant");
        }
        V1AccessTokenInfo accessTokenInfo = createAccessTokenInfo(codeInfo.getIss(), codeInfo.getUser(), codeInfo.getClient_id(),
                codeInfo.getScope());
        String id_token = createIdToken(codeInfo.getIss(), codeInfo.getUser(), codeInfo.getClient_id(), codeInfo.getNonce());
        V1AccessToken accessToken = V1AccessToken.builder()
                .access_token(accessTokenInfo.getAccessToken())
                .refresh_token(accessTokenInfo.getRefreshToken())
                .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                .scope(codeInfo.getScope())
                .expires_in(oidcHandler.loadClientConfig(client_id).getAccessTokenExpirationSeconds())
                .id_token(id_token)
                .build();
        return ResponseEntity.ok(accessToken);
    }

    private ResponseEntity<?> doTokenWithImplicit(V1OidcClientConfig clientConfig) throws Exception {
        if (!clientConfig.isImplicitFlowEnabled()) {
            return wrapErrorRFC6749("invalid_request", "disabled implicit credentials grant");
        }
        return wrapErrorRFC6749("invalid_request", "Not yet implemented");
    }

    private ResponseEntity<?> doTokenWithRefreshToken(
            String client_id,
            String refresh_token,
            V1OidcClientConfig clientConfig,
            V1AuthorizationCodeInfo codeInfo) throws Exception {
        if (!clientConfig.isUseRefreshTokenEnabled()) {
            return wrapErrorRFC6749("invalid_request", "disabled refresh token grant");
        }
        if (isBlank(refresh_token)) {
            return wrapErrorRFC6749("invalid_request", "refresh_token not missing");
        }
        // Check refresh_token valid?
        String lastAccessToken = oidcHandler.loadRefreshToken(refresh_token);
        if (isBlank(lastAccessToken)) {
            return wrapErrorRFC6749("invalid_request", "refresh_token not invalid");
        }
        // New generate access_token
        V1AccessTokenInfo accessTokenInfo = createAccessTokenInfo(codeInfo.getIss(), codeInfo.getUser(), codeInfo.getClient_id(),
                codeInfo.getScope());
        V1AccessToken accessToken = V1AccessToken.builder()
                .access_token(accessTokenInfo.getAccessToken())
                .refresh_token(accessTokenInfo.getRefreshToken())
                .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                .expires_in(oidcHandler.loadClientConfig(client_id).getAccessTokenExpirationSeconds())
                .scope(codeInfo.getScope())
                .build();
        return ResponseEntity.ok(accessToken);
    }

    private ResponseEntity<?> doTokenWithPassword(V1OidcClientConfig clientConfig) throws Exception {
        if (!clientConfig.isDirectAccessGrantsEnabled()) {
            return wrapErrorRFC6749("invalid_request", "disabled password credentials grant");
        }
        return wrapErrorRFC6749("invalid_request", "Not yet implemented");
    }

    private ResponseEntity<?> doTokenWithClientCredentials(V1OidcClientConfig clientConfig) throws Exception {
        // TODO
        return wrapErrorRFC6749("invalid_request", "Not yet implemented");
    }

    private ResponseEntity<?> doTokenWithDeviceCode(V1OidcClientConfig clientConfig) throws Exception {
        // TODO
        return wrapErrorRFC6749("invalid_request", "Not yet implemented");
    }

    private String createAuthorizationCode(
            String code_challenge,
            String code_challenge_method,
            String client_id,
            String redirect_uri,
            V1OidcUserClaims user,
            String iss,
            String scope,
            String nonce) {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        String code = Base64URL.encode(bytes).toString();

        V1AuthorizationCodeInfo codeInfo = new V1AuthorizationCodeInfo(code_challenge, code_challenge_method, code, client_id,
                redirect_uri, user, iss, scope, nonce);
        oidcHandler.putAuthorizationCode(code, codeInfo);

        log.info("Issuing authorization code={}, code info={}", code, codeInfo);
        return code;
    }

    private V1AccessTokenInfo createAccessTokenInfo(String iss, V1OidcUserClaims user, String client_id, String scope)
            throws JOSEException {
        // load config for client.
        V1OidcClientConfig clientConfig = oidcHandler.loadClientConfig(client_id);

        // Create JWT claims
        Date expiration = new Date(System.currentTimeMillis() + clientConfig.getAccessTokenExpirationSeconds() * 1000L);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getSub())
                .issuer(iss)
                .subject(user.getSub())
                .audience(client_id)
                .issueTime(new Date())
                .expirationTime(expiration)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", scope)
                .build();
        // Create JWT token
        SignedJWT jwt = new SignedJWT(jwsHeader, jwtClaimsSet);
        // Sign the JWT token
        jwt.sign(signer);
        String access_token = jwt.serialize();

        // Generate refresh token.
        String refresh_token = createRefreshToken(access_token, iss, user, client_id, scope);

        V1AccessTokenInfo accessTokenInfo = V1AccessTokenInfo.builder()
                .user(user)
                .accessToken(access_token)
                .refreshToken(refresh_token)
                .expiration(expiration)
                .scope(scope)
                .clientId(client_id)
                .iss(iss)
                .build();

        oidcHandler.putAccessToken(access_token, accessTokenInfo);
        oidcHandler.putRefreshToken(refresh_token, accessTokenInfo);
        return accessTokenInfo;
    }

    private String createRefreshToken(String access_token, String iss, V1OidcUserClaims user, String client_id, String scope)
            throws JOSEException {
        // load config for client.
        V1OidcClientConfig clientConfig = oidcHandler.loadClientConfig(client_id);

        // create JWT claims
        Date expiration = new Date(System.currentTimeMillis() + clientConfig.getAccessTokenExpirationSeconds() * 1000L);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getSub())
                .issuer(iss)
                .subject(user.getSub())
                .audience(client_id)
                .issueTime(new Date())
                .expirationTime(expiration)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", scope)
                .claim("access_token", access_token)
                .build();
        // create JWT token
        SignedJWT jwt = new SignedJWT(jwsHeader, jwtClaimsSet);
        // sign the JWT token
        jwt.sign(signer);
        return jwt.serialize();
    }

    private String createIdToken(String iss, V1OidcUserClaims user, String client_id, String nonce)
            throws NoSuchAlgorithmException, JOSEException {
        // load config for client.
        V1OidcClientConfig clientConfig = oidcHandler.loadClientConfig(client_id);

        // compute at_hash
        byte[] hashBytes = doDigestHash(StandardSignAlgorithm.of(clientConfig.getIdTokenSignAlg()), user.getSub());
        byte[] hashBytesLeftHalf = Arrays.copyOf(hashBytes, hashBytes.length / 2);
        Base64URL encodedHash = Base64URL.encode(hashBytesLeftHalf);
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
        SignedJWT jwt = new SignedJWT(jwsHeader, jwtClaimsSet);
        // sign the JWT token
        jwt.sign(signer);
        return jwt.serialize();
    }

}
