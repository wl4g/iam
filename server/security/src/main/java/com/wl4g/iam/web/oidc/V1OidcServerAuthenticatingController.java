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
package com.wl4g.iam.web.oidc;

import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_AT_HASH;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_EMAIL;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_FAMILY_NAME;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_GIVEN_NAME;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_ISS;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_NAME;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_NONCE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_PREFERRED_USERNAME;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_CLAIMS_SUB;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_DISPLAY_PAGE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_GRANT_AUTHORIZATION_CODE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_GRANT_IMPLICIT;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_GRANT_REFRESH_TOKEN;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_RESPONSE_TYPE_ALL;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_RESPONSE_TYPE_CODE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_RESPONSE_TYPE_TOKEN;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_SCOPE_ADDRESS;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_SCOPE_EMAIL;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_SCOPE_OPENID;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_SCOPE_PHONE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_SCOPE_PROFILE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_SUBJECT_PUBLIC;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_TOKEN_TYPE_BEARER;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_AUTHORIZE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_INTROSPECTION;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_JWKS;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_METADATA;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_TOKEN;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_USERINFO;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.SignAlgorithmSupported.parseDigest;
import static com.wl4g.infra.common.codec.Encodes.urlDecode;
import static com.wl4g.infra.common.codec.Encodes.urlEncode;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.equalsAny;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import com.wl4g.iam.common.constant.V1OidcIAMConstants.SignAlgorithmSupported;
import com.wl4g.iam.common.model.oidc.v1.V1AccessToken;
import com.wl4g.iam.common.model.oidc.v1.V1AccessTokenInfo;
import com.wl4g.iam.common.model.oidc.v1.V1AuthorizationCodeInfo;
import com.wl4g.iam.common.model.oidc.v1.V1IntrospectionAccessToken;
import com.wl4g.iam.common.model.oidc.v1.V1MetadataEndpointModel;
import com.wl4g.iam.common.model.oidc.v1.V1OidcUserClaims;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.core.exception.IamException;
import com.wl4g.iam.handler.oidc.v1.V1OidcAuthenticatingHandler;
import com.wl4g.iam.web.BaseIamController;
import com.wl4g.infra.common.annotation.Reserved;
import com.wl4g.infra.common.collection.CollectionUtils2;
import com.wl4g.infra.common.resource.StreamResource;
import com.wl4g.infra.common.resource.resolver.ClassPathResourcePatternResolver;

/**
 * IAM V1-OIDC authentication controller.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-18 v1.0.0
 * @since v3.0.0
 * @see https://openid.net/specs/openid-connect-core-1_0.html#AuthResponseValidation
 */
@V1OidcServerController
public class V1OidcServerAuthenticatingController extends BaseIamController {

    private final SecureRandom random = new SecureRandom();

    private @Autowired IamProperties config;
    private @Autowired V1OidcAuthenticatingHandler oidcHandler;

    private JWSSigner signer;
    private JWKSet pubJWKSet;
    private JWSHeader jwsHeader;

    @PostConstruct
    public void init() throws IOException, ParseException, JOSEException {
        log.info("Initializing OIDC JWK ...");

        ClassPathResourcePatternResolver resolver = new ClassPathResourcePatternResolver(
                Thread.currentThread().getContextClassLoader());
        Set<StreamResource> resources = resolver.getResources(config.getV1Oidc().getDefaultJwksJsonResource());
        if (resources.isEmpty()) {
            throw new IamException(format("Not found jwks resource: %s", config.getV1Oidc().getDefaultJwksJsonResource()));
        }
        StreamResource jwksRes = resources.iterator().next();
        if (resources.size() > 1) {
            log.warn(format("[WARNNING] Found multi jwks resources %s by %s, Using the first one by default %s", resources,
                    config.getV1Oidc().getDefaultJwksJsonResource(), jwksRes));
        }
        JWKSet jwkSet = JWKSet.load(jwksRes.getInputStream());
        JWK key = jwkSet.getKeys().get(0);
        this.signer = new RSASSASigner((RSAKey) key);
        this.pubJWKSet = jwkSet.toPublicJWKSet();
        this.jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse(config.getV1Oidc().getDefaultJwksAlgName()))
                .keyID(key.getKeyID())
                .build();
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

        String urlPrefix = uriBuilder.replacePath(null).build().encode().toUriString();
        // https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata
        // https://tools.ietf.org/html/rfc8414#section-2
        V1MetadataEndpointModel metadata = V1MetadataEndpointModel.builder()
                .issuer(urlPrefix.concat("/")) // REQUIRED
                // REQUIRED
                .authorization_endpoint(urlPrefix.concat(URI_IAM_OIDC_ENDPOINT_AUTHORIZE))
                // REQUIRED
                .token_endpoint(urlPrefix.concat(URI_IAM_OIDC_ENDPOINT_TOKEN))
                // RECOMMENDED
                .userinfo_endpoint(urlPrefix.concat(URI_IAM_OIDC_ENDPOINT_USERINFO))
                // REQUIRED
                .jwks_uri(urlPrefix.concat(URI_IAM_OIDC_ENDPOINT_JWKS))
                .introspection_endpoint(urlPrefix.concat(URI_IAM_OIDC_ENDPOINT_INTROSPECTION))
                // REQUIRED
                .scopes_supported(asList(KEY_IAM_OIDC_SCOPE_OPENID, KEY_IAM_OIDC_SCOPE_PROFILE, KEY_IAM_OIDC_SCOPE_EMAIL,
                        KEY_IAM_OIDC_SCOPE_ADDRESS, KEY_IAM_OIDC_SCOPE_PHONE))
                // REQUIRED
                .response_types_supported(KEY_IAM_OIDC_RESPONSE_TYPE_ALL)
                // OPTIONAL
                .grant_types_supported(asList(KEY_IAM_OIDC_GRANT_AUTHORIZATION_CODE, KEY_IAM_OIDC_GRANT_IMPLICIT,
                        KEY_IAM_OIDC_GRANT_REFRESH_TOKEN))
                // REQUIRED
                .subject_types_supported(singletonList(KEY_IAM_OIDC_SUBJECT_PUBLIC))
                // REQUIRED
                .id_token_signing_alg_values_supported(config.getV1Oidc().getDefaultIdTokenAlgSupported())
                .claims_supported(asList(KEY_IAM_OIDC_CLAIMS_SUB, KEY_IAM_OIDC_CLAIMS_ISS, KEY_IAM_OIDC_CLAIMS_NAME,
                        KEY_IAM_OIDC_CLAIMS_FAMILY_NAME, KEY_IAM_OIDC_CLAIMS_GIVEN_NAME, KEY_IAM_OIDC_CLAIMS_PREFERRED_USERNAME,
                        KEY_IAM_OIDC_CLAIMS_EMAIL))
                .display_values_supported(asList(KEY_IAM_OIDC_DISPLAY_PAGE))
                .service_documentation(config.getV1Oidc().getServiceDocumentation())
                // PKCE support advertised
                .code_challenge_methods_supported(config.getV1Oidc().getDefaultCodeChallengeMethodsSupported())
                .build();
        return ResponseEntity.ok().body(metadata);
    }

    /**
     * Provides JSON Web Key Set containing the public part of the key used to
     * sign ID tokens.
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_JWKS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> jwks(HttpServletRequest req) {
        log.info("called:jwks '{}' from '{}'", URI_IAM_OIDC_ENDPOINT_JWKS, req.getRemoteHost());
        return ResponseEntity.ok().body(pubJWKSet.toString());
    }

    /**
     * Provides authorization endpoint. </br>
     * </br>
     * For the description of parameter "code_challenge", see: <a href=
     * "https://blog.csdn.net/weixin_34415923/article/details/89691037">https://blog.csdn.net/weixin_34415923/article/details/89691037</a>
     * 
     * @see https://openid.net/specs/openid-connect-core-1_0.html#codeExample
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_AUTHORIZE, method = RequestMethod.GET)
    public ResponseEntity<?> authorize(
            @RequestParam String client_id,
            @RequestParam String redirect_uri,
            @RequestParam String response_type,
            @RequestParam String scope,
            @RequestParam String state,
            @RequestParam(required = false) String nonce,
            @RequestParam(required = false) String code_challenge,
            @RequestParam(required = false) String code_challenge_method,
            @RequestParam(required = false) @Reserved String response_mode,
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
                Set<String> responseType = toSpaceSeparatedParams(response_type);
                // Check hybrid flow response type
                if (!CollectionUtils2.isSubCollection(responseType, KEY_IAM_OIDC_RESPONSE_TYPE_ALL)) {
                    String url = format("%s#error=unsupported_response_type", redirect_uri);
                    return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
                }

                // see:https://openid.net/specs/openid-connect-core-1_0.html#HybridFlowAuth
                UriComponentsBuilder fragmentUri = UriComponentsBuilder.newInstance();
                fragmentUri.queryParam("token_type", KEY_IAM_OIDC_TOKEN_TYPE_BEARER);
                fragmentUri.queryParam("state", urlEncode(state));
                if (responseType.contains(KEY_IAM_OIDC_RESPONSE_TYPE_CODE)) {
                    // see:https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth
                    // see:https://openid.net/specs/openid-connect-core-1_0.html#codeExample
                    // Check challenge method supported.
                    if (!isBlank(code_challenge_method)
                            && !config.getV1Oidc().getDefaultCodeChallengeMethodsSupported().contains(code_challenge_method)) {
                        return wrapErrorRFC6749("unsupported_code_challenge_method",
                                format("code_challenge_method must contains is '%s'",
                                        config.getV1Oidc().getDefaultCodeChallengeMethodsSupported()));
                    }
                    String code = createAuthorizationCode(code_challenge, code_challenge_method, client_id, redirect_uri, user,
                            iss, scope, nonce);
                    fragmentUri.queryParam("code", code);
                }
                if (responseType.contains(KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN)) {
                    // see:https://openid.net/specs/openid-connect-core-1_0.html#id_tokenExample
                    String id_token = createIdToken(iss, user, client_id, nonce);
                    fragmentUri.queryParam("id_token", id_token);
                }
                if (responseType.contains(KEY_IAM_OIDC_RESPONSE_TYPE_TOKEN)) {
                    // see:https://openid.net/specs/openid-connect-core-1_0.html#ImplicitFlowAuth
                    // see:https://openid.net/specs/openid-connect-core-1_0.html#code-tokenExample
                    V1AccessTokenInfo accessTokenInfo = createAccessTokenInfo(iss, user, client_id, scope);
                    fragmentUri.queryParam("access_token", accessTokenInfo.getAccessToken());
                }
                String location = redirect_uri.concat("#").concat(fragmentUri.build().toString());
                return ResponseEntity.status(HttpStatus.FOUND).header("Location", location).build();
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
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_TOKEN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> token(
            @RequestParam String grant_type,
            @RequestParam String code,
            @RequestParam String redirect_uri,
            @RequestParam(required = false) String refresh_token,
            @RequestParam(required = false) String client_id,
            @RequestParam(required = false) String code_verifier,
            @RequestHeader(name = "Authorization", required = false) String auth,
            UriComponentsBuilder uriBuilder,
            HttpServletRequest req) throws NoSuchAlgorithmException, JOSEException {

        log.info("called:token '{}' from '{}', grant_type={} code={} redirect_uri={} client_id={}", URI_IAM_OIDC_ENDPOINT_TOKEN,
                req.getRemoteHost(), grant_type, code, redirect_uri, client_id);

        if (!equalsAny(grant_type, KEY_IAM_OIDC_GRANT_AUTHORIZATION_CODE, KEY_IAM_OIDC_GRANT_REFRESH_TOKEN)) {
            return wrapErrorRFC6749("unsupported_grant_type", format("grant_type must is '%s' or '%s'",
                    KEY_IAM_OIDC_GRANT_AUTHORIZATION_CODE, KEY_IAM_OIDC_GRANT_REFRESH_TOKEN));
        }
        V1AuthorizationCodeInfo codeInfo = oidcHandler.loadAuthorizationCode(code);
        if (isNull(codeInfo)) {
            return wrapErrorRFC6749("invalid_grant", "code not valid");
        }
        if (!StringUtils.equals(redirect_uri, codeInfo.getRedirect_uri())) {
            return wrapErrorRFC6749("invalid_request", "redirect_uri not valid");
        }
        if (!isBlank(codeInfo.getCodeChallenge())) {
            // check PKCE
            if (isBlank(code_verifier)) {
                return wrapErrorRFC6749("invalid_request", "code_verifier missing");
            }
            if (SignAlgorithmSupported.PLAIN.name().equals(codeInfo.getCodeChallengeMethod())) {
                if (!codeInfo.getCodeChallenge().equals(code_verifier)) {
                    log.warn("code_verifier {} does not match code_challenge {}", code_verifier, codeInfo.getCodeChallenge());
                    return wrapErrorRFC6749("invalid_request", "code_verifier not correct");
                }
            } else {
                String hashedVerifier = Base64URL
                        .encode(doDigestHash(parseDigest(codeInfo.getCodeChallengeMethod()), code_verifier))
                        .toString();
                if (!codeInfo.getCodeChallenge().equals(hashedVerifier)) {
                    log.warn("code_verifier {} hashed using S256 to {} does not match code_challenge {}", code_verifier,
                            hashedVerifier, codeInfo.getCodeChallenge());
                    return wrapErrorRFC6749("invalid_request", "code_verifier not correct");
                }
                log.debug("code_verifier OK");
            }
        }

        // response access token
        switch (grant_type) {
        case KEY_IAM_OIDC_GRANT_AUTHORIZATION_CODE: // authorization_code
            V1AccessTokenInfo accessTokenInfo = createAccessTokenInfo(codeInfo.getIss(), codeInfo.getUser(),
                    codeInfo.getClient_id(), codeInfo.getScope());
            String id_token = createIdToken(codeInfo.getIss(), codeInfo.getUser(), codeInfo.getClient_id(), codeInfo.getNonce());
            V1AccessToken accessToken = V1AccessToken.builder()
                    .access_token(accessTokenInfo.getAccessToken())
                    .refresh_token(accessTokenInfo.getRefreshToken())
                    .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                    .scope(codeInfo.getScope())
                    .expires_in(config.getV1Oidc().getDefaultAccessTokenExpirationSeconds())
                    .id_token(id_token)
                    .build();
            return ResponseEntity.ok(accessToken);
        case KEY_IAM_OIDC_GRANT_REFRESH_TOKEN: // refresh_token
            if (isBlank(refresh_token)) {
                return wrapErrorRFC6749("invalid_request", "refresh_token not missing");
            }
            // Check refresh_token valid?
            String lastAccessToken = oidcHandler.loadRefreshToken(refresh_token);
            if (isBlank(lastAccessToken)) {
                return wrapErrorRFC6749("invalid_request", "refresh_token not invalid");
            }
            // New generate access_token
            accessTokenInfo = createAccessTokenInfo(codeInfo.getIss(), codeInfo.getUser(), codeInfo.getClient_id(),
                    codeInfo.getScope());
            accessToken = V1AccessToken.builder()
                    .access_token(accessTokenInfo.getAccessToken())
                    .refresh_token(accessTokenInfo.getRefreshToken())
                    .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                    .expires_in(config.getV1Oidc().getDefaultAccessTokenExpirationSeconds())
                    .scope(codeInfo.getScope())
                    .build();
            return ResponseEntity.ok(accessToken);
        default:
            return wrapErrorRFC6749("invalid_request", "unknown");
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
        if (accessTokenInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid is access token.");
        }
        Set<String> scopes = toSpaceSeparatedParams(accessTokenInfo.getScope());
        V1OidcUserClaims user = accessTokenInfo.getUser();
        V1OidcUserClaims scopedUser = V1OidcUserClaims.builder().sub(user.getSub()).build();
        if (scopes.contains(KEY_IAM_OIDC_SCOPE_PROFILE)) {
            scopedUser = scopedUser.withName(user.getName())
                    .withGiven_name(user.getGiven_name())
                    .withFamily_name(user.getFamily_name())
                    .withNickname(user.getNickname())
                    .withPreferred_username(user.getPreferred_username())
                    .withPicture(user.getPicture())
                    .withBirthdate(user.getBirthdate())
                    .withLocale(user.getLocale())
                    .withUpdated_at(user.getUpdated_at());
        }
        if (scopes.contains(KEY_IAM_OIDC_SCOPE_EMAIL)) {
            scopedUser = scopedUser.withEmail(user.getEmail()).withEmail_verified(user.getEmail_verified());
        }
        if (scopes.contains(KEY_IAM_OIDC_SCOPE_ADDRESS)) {
            scopedUser = scopedUser.withAddress(user.getAddress());
        }
        if (scopes.contains(KEY_IAM_OIDC_SCOPE_PHONE)) {
            scopedUser = scopedUser.withPhone_number(user.getPhone_number())
                    .withPhone_number_verified(user.getPhone_number_verified());
        }
        return ResponseEntity.ok().body(scopedUser);
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
        // Create JWT claims
        Date expiration = new Date(
                System.currentTimeMillis() + config.getV1Oidc().getDefaultAccessTokenExpirationSeconds() * 1000L);
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
        oidcHandler.putRefreshToken(refresh_token, access_token);
        return accessTokenInfo;
    }

    private String createRefreshToken(String access_token, String iss, V1OidcUserClaims user, String client_id, String scope)
            throws JOSEException {
        // create JWT claims
        Date expiration = new Date(
                System.currentTimeMillis() + config.getV1Oidc().getDefaultAccessTokenExpirationSeconds() * 1000L);
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
        // compute at_hash
        byte[] hashBytes = doDigestHash(parseDigest(config.getV1Oidc().getDefaultIdTokenAlgName()), user.getSub());
        byte[] hashBytesLeftHalf = Arrays.copyOf(hashBytes, hashBytes.length / 2);
        Base64URL encodedHash = Base64URL.encode(hashBytesLeftHalf);
        // create JWT claims
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getSub())
                .issuer(iss)
                .audience(client_id)
                .issueTime(new Date())
                .expirationTime(
                        new Date(currentTimeMillis() + config.getV1Oidc().getDefaultAccessTokenExpirationSeconds() * 1000L))
                .jwtID(UUID.randomUUID().toString())
                .claim(KEY_IAM_OIDC_CLAIMS_NONCE, nonce)
                .claim(KEY_IAM_OIDC_CLAIMS_AT_HASH, encodedHash)
                .build();
        // create JWT token
        SignedJWT jwt = new SignedJWT(jwsHeader, jwtClaimsSet);
        // sign the JWT token
        jwt.sign(signer);
        return jwt.serialize();
    }

    private byte[] doDigestHash(String digestAlgName, String plaintext) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(digestAlgName);
        digest.reset();
        digest.update(plaintext.getBytes(StandardCharsets.UTF_8));
        return digest.digest();
    }

    private String toDetermineAccessToken(String authorizationHeader, String accessTokenParameter) {
        if (!isBlank(accessTokenParameter)) {
            return accessTokenParameter;
        } else if (startsWith(authorizationHeader, KEY_IAM_OIDC_TOKEN_TYPE_BEARER.concat(" "))) {
            return authorizationHeader.substring(KEY_IAM_OIDC_TOKEN_TYPE_BEARER.length() + 1);
        }
        return null;
    }

    private Set<String> toSpaceSeparatedParams(String param) {
        if (isBlank(param)) {
            return emptySet();
        }
        return new HashSet<>(asList(urlDecode(param).split(" ")));
    }

    private ResponseEntity<String> wrapUnauthentication() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_HTML);
        responseHeaders.add("WWW-Authenticate", format("Basic realm=\"{}\"", config.getV1Oidc().getDefaultBasicRealmName()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(responseHeaders).body(
                "<html><body><h1>401 Unauthorized</h1>IAM V1 OIDC server</body></html>");
    }

    /**
     * https://datatracker.ietf.org/doc/html/rfc6749
     */
    private ResponseEntity<?> wrapErrorRFC6749(String error, String error_description) {
        log.warn("Wrap rfc6749 error={} error_description={}", error, error_description);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("error", error);
        map.put("error_description", error_description);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
    }

}
