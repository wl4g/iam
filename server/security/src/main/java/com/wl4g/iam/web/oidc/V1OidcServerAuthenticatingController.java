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
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_GRANT_AUTHORIZATION_CODE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_GRANT_IMPLICIT;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_RESPONSE_TYPE_CODE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN_TOKEN;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_RESPONSE_TYPE_TOKEN;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_SCOPE_EMAIL;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_SCOPE_OPENID;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_SCOPE_PROFILE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_SUBJECT_PUBLIC;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_TOKEN_TYPE_BEARER;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_AUTHORIZE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_INTROSPECTION;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_JWKS;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_METADATA;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_TOKEN;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_USERINFO;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
import com.wl4g.iam.common.model.oidc.v1.V1AccessToken;
import com.wl4g.iam.common.model.oidc.v1.V1AccessTokenInfo;
import com.wl4g.iam.common.model.oidc.v1.V1AuthorizationCodeInfo;
import com.wl4g.iam.common.model.oidc.v1.V1IntrospectionAccessToken;
import com.wl4g.iam.common.model.oidc.v1.V1MetadataEndpointModel;
import com.wl4g.iam.common.model.oidc.v1.V1OidcUser;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.core.exception.IamException;
import com.wl4g.iam.handler.oidc.V1OidcAuthenticatingHandler;
import com.wl4g.iam.web.BaseIamController;
import com.wl4g.infra.common.codec.Encodes;
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
        Set<StreamResource> resources = resolver.getResources(config.getV1Oidc().getJwksJsonResource());
        if (resources.isEmpty()) {
            throw new IamException(format("Not found jwks resource: %s", config.getV1Oidc().getJwksJsonResource()));
        }
        StreamResource jwksRes = resources.iterator().next();
        if (resources.size() > 1) {
            log.warn(format("[WARNNING] Found multi jwks resources %s by %s, Using the first one by default %s", resources,
                    config.getV1Oidc().getJwksJsonResource(), jwksRes));
        }
        JWKSet jwkSet = JWKSet.load(jwksRes.getInputStream());
        JWK key = jwkSet.getKeys().get(0);
        this.signer = new RSASSASigner((RSAKey) key);
        this.pubJWKSet = jwkSet.toPublicJWKSet();
        this.jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse(config.getV1Oidc().getJwsAlgorithmName())).keyID(key.getKeyID())
                .build();
    }

    /**
     * Provides OIDC metadata. See the spec at
     * https://openid.net/specs/openid-connect-discovery-1_0.html
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_METADATA, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public V1MetadataEndpointModel metadata(UriComponentsBuilder uriBuilder, HttpServletRequest req) {
        log.info("called:metadata '{}' from '{}'", URI_IAM_OIDC_ENDPOINT_METADATA, req.getRemoteHost());

        String urlPrefix = uriBuilder.replacePath(null).build().encode().toUriString();
        // https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata
        // https://tools.ietf.org/html/rfc8414#section-2
        return V1MetadataEndpointModel.builder()
                .issuer(urlPrefix.concat("/")) // REQUIRED
                .authorization_endpoint(urlPrefix.concat(URI_IAM_OIDC_ENDPOINT_AUTHORIZE)) // REQUIRED
                .token_endpoint(urlPrefix.concat(URI_IAM_OIDC_ENDPOINT_TOKEN)) // REQUIRED
                .userinfo_endpoint(urlPrefix.concat(URI_IAM_OIDC_ENDPOINT_USERINFO)) // RECOMMENDED
                .jwks_uri(urlPrefix.concat(URI_IAM_OIDC_ENDPOINT_JWKS)) // REQUIRED
                .introspection_endpoint(urlPrefix.concat(URI_IAM_OIDC_ENDPOINT_INTROSPECTION))
                .scopes_supported(asList(KEY_IAM_OIDC_SCOPE_OPENID, KEY_IAM_OIDC_SCOPE_PROFILE, KEY_IAM_OIDC_SCOPE_EMAIL)) // REQUIRED
                .response_types_supported(asList(KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN_TOKEN, KEY_IAM_OIDC_RESPONSE_TYPE_CODE)) // REQUIRED
                .grant_types_supported(asList(KEY_IAM_OIDC_GRANT_AUTHORIZATION_CODE, KEY_IAM_OIDC_GRANT_IMPLICIT)) // OPTIONAL
                .subject_types_supported(singletonList(KEY_IAM_OIDC_SUBJECT_PUBLIC)) // REQUIRED
                .id_token_signing_alg_values_supported(asList("RS256", "none")) // REQUIRED
                .claims_supported(asList(KEY_IAM_OIDC_CLAIMS_SUB, KEY_IAM_OIDC_CLAIMS_ISS, KEY_IAM_OIDC_CLAIMS_NAME,
                        KEY_IAM_OIDC_CLAIMS_FAMILY_NAME, KEY_IAM_OIDC_CLAIMS_GIVEN_NAME, KEY_IAM_OIDC_CLAIMS_PREFERRED_USERNAME,
                        KEY_IAM_OIDC_CLAIMS_EMAIL))
                // PKCE support advertised
                .code_challenge_methods_supported(config.getV1Oidc().getCodeChallengeMethodsSupported())
                .build();
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
     * Provides claims about a user. Requires a valid access token.
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_USERINFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(allowedHeaders = { "Authorization", "Content-Type" })
    public ResponseEntity<?> userinfo(
            @RequestHeader("Authorization") String auth,
            @RequestParam(required = false) String access_token,
            HttpServletRequest req) {

        log.info("called:userinfo '{}' from '{}' bearerToken={}, access_token={}", URI_IAM_OIDC_ENDPOINT_USERINFO,
                req.getRemoteHost(), auth, access_token);

        String accessToken = getAccessTokenFrom(auth, access_token);
        V1AccessTokenInfo accessTokenInfo = oidcHandler.loadAccessToken(accessToken);
        if (accessTokenInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid is access token.");
        }
        Set<String> scopes = fromSpaceSeparatedString(accessTokenInfo.getScope());
        Map<String, Object> m = new LinkedHashMap<>();
        V1OidcUser user = accessTokenInfo.getUser();
        m.put("sub", user.getSub());
        if (scopes.contains(KEY_IAM_OIDC_SCOPE_PROFILE)) {
            user = V1OidcUser.builder()
                    .name(user.getName())
                    .family_name(user.getFamily_name())
                    .given_name(user.getGiven_name())
                    .preferred_username(user.getPreferred_username())
                    .build();
        }
        if (scopes.contains(KEY_IAM_OIDC_SCOPE_EMAIL)) {
            user = V1OidcUser.builder().email(user.getEmail()).build();
        }
        return ResponseEntity.ok().body(user);
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

        // String accessToken = getAccessTokenFrom(auth, token);
        V1AccessTokenInfo accessTokenInfo = oidcHandler.loadAccessToken(auth);
        if (accessTokenInfo == null) {
            log.error("Not found {}", token);
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
     * Provides token endpoint.
     * 
     * @see https://openid.net/specs/openid-connect-core-1_0.html#TokenRequestValidation
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_TOKEN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> token(
            @RequestParam String grant_type,
            @RequestParam String code,
            @RequestParam String redirect_uri,
            @RequestParam(required = false) String client_id,
            @RequestParam(required = false) String code_verifier,
            @RequestHeader(name = "Authorization", required = false) String auth,
            UriComponentsBuilder uriBuilder,
            HttpServletRequest req) throws NoSuchAlgorithmException, JOSEException {

        log.info("called:token '{}' from '{}', grant_type={} code={} redirect_uri={} client_id={}", URI_IAM_OIDC_ENDPOINT_TOKEN,
                req.getRemoteHost(), grant_type, code, redirect_uri, client_id);

        if (!"authorization_code".equals(grant_type)) {
            return wrapErrorJson("unsupported_grant_type", "grant_type is not authorization_code");
        }
        V1AuthorizationCodeInfo codeInfo = oidcHandler.loadAuthorizationCode(code);
        if (codeInfo == null) {
            return wrapErrorJson("invalid_grant", "code not valid");
        }
        if (!redirect_uri.equals(codeInfo.getRedirect_uri())) {
            return wrapErrorJson("invalid_request", "redirect_uri not valid");
        }
        if (codeInfo.getCodeChallenge() != null) {
            // check PKCE
            if (code_verifier == null) {
                return wrapErrorJson("invalid_request", "code_verifier missing");
            }
            if ("S256".equals(codeInfo.getCodeChallengeMethod())) {
                MessageDigest s256 = MessageDigest.getInstance(config.getV1Oidc().getIdTokenDigestName());
                s256.reset();
                s256.update(code_verifier.getBytes(StandardCharsets.UTF_8));
                String hashedVerifier = Base64URL.encode(s256.digest()).toString();
                if (!codeInfo.getCodeChallenge().equals(hashedVerifier)) {
                    log.warn("code_verifier {} hashed using S256 to {} does not match code_challenge {}", code_verifier,
                            hashedVerifier, codeInfo.getCodeChallenge());
                    return wrapErrorJson("invalid_request", "code_verifier not correct");
                }
                log.info("code_verifier OK");
            } else {
                if (!codeInfo.getCodeChallenge().equals(code_verifier)) {
                    log.warn("code_verifier {} does not match code_challenge {}", code_verifier, codeInfo.getCodeChallenge());
                    return wrapErrorJson("invalid_request", "code_verifier not correct");
                }
            }
        }

        // response access token
        String access_token = createAccessToken(codeInfo.getIss(), codeInfo.getUser(), codeInfo.getClient_id(),
                codeInfo.getScope());
        String id_token = createIdToken(codeInfo.getIss(), codeInfo.getUser(), codeInfo.getClient_id(), codeInfo.getNonce(),
                access_token);
        V1AccessToken accessToken = V1AccessToken.builder()
                .access_token(access_token)
                .token_type(KEY_IAM_OIDC_TOKEN_TYPE_BEARER)
                .scope(codeInfo.getScope())
                .expires_in(config.getV1Oidc().getTokenExpirationSeconds())
                .id_token(id_token)
                .build();
        return ResponseEntity.ok(accessToken);
    }

    /**
     * Provides authorization endpoint.
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
            @RequestParam(required = false) String response_mode,
            @RequestHeader(name = "Authorization", required = false) String auth,
            UriComponentsBuilder uriBuilder,
            HttpServletRequest req) throws JOSEException, NoSuchAlgorithmException {

        log.info("called:authorize '{}' from '{}', scope={} response_type={} client_id={} redirect_uri={}",
                URI_IAM_OIDC_ENDPOINT_AUTHORIZE, req.getRemoteHost(), scope, response_type, client_id, redirect_uri);

        if (auth == null) {
            log.info("User and password not provided. scope={} response_type={} client_id={} redirect_uri={}", scope,
                    response_type, client_id, redirect_uri);
            return wrapUnauthentication();
        } else {
            String[] creds = new String(Base64.getDecoder().decode(auth.split(" ")[1])).split(":", 2);
            String username = creds[0];
            String password = creds[1];
            V1OidcUser user = oidcHandler.getV1OidcUser(username, password);
            if (user.getLogin_name().equals(username) && user.getPassword().equals(password)) {
                log.info("Password {} for user {} is correct.", password, username);

                Set<String> responseType = fromSpaceSeparatedString(response_type);
                String iss = uriBuilder.replacePath("/").build().encode().toUriString();

                // Implicit flow
                // see:https://openid.net/specs/openid-connect-core-1_0.html#ImplicitFlowAuth
                if (responseType.contains(KEY_IAM_OIDC_RESPONSE_TYPE_TOKEN)) {
                    log.info("Using implicit flow. scope={} response_type={} client_id={} redirect_uri={}", scope, response_type,
                            client_id, redirect_uri);

                    String access_token = createAccessToken(iss, user, client_id, scope);
                    String id_token = createIdToken(iss, user, client_id, nonce, access_token);
                    String url = redirect_uri + "#" + "access_token=" + Encodes.urlEncode(access_token) + "&token_type="
                            + KEY_IAM_OIDC_TOKEN_TYPE_BEARER + "&state=" + Encodes.urlEncode(state) + "&expires_in="
                            + config.getV1Oidc().getTokenExpirationSeconds() + "&id_token=" + Encodes.urlEncode(id_token);
                    return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
                }
                // Authorization code flow
                // see:https://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth
                else if (responseType.contains(KEY_IAM_OIDC_RESPONSE_TYPE_CODE)) {
                    log.info("Using authorization code flow {}", code_challenge != null ? "with PKCE" : "");
                    String code = createAuthorizationCode(code_challenge, code_challenge_method, client_id, redirect_uri, user,
                            iss, scope, nonce);
                    String url = redirect_uri.concat("?").concat("code=").concat(code).concat("&state=").concat(
                            Encodes.urlEncode(state));
                    return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
                }
                // Not implements to Hybrid flow
                // see:https://openid.net/specs/openid-connect-core-1_0.html#HybridFlowAuth
                else {
                    String url = redirect_uri + "#" + "error=unsupported_response_type";
                    return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
                }
            } else {
                log.info("Wrong user and password combination. scope={} response_type={} client_id={} redirect_uri={}", scope,
                        response_type, client_id, redirect_uri);
                return wrapUnauthentication();
            }
        }
    }

    private String createAuthorizationCode(
            String code_challenge,
            String code_challenge_method,
            String client_id,
            String redirect_uri,
            V1OidcUser user,
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

    private String createAccessToken(String iss, V1OidcUser user, String client_id, String scope) throws JOSEException {
        // create JWT claims
        Date expiration = new Date(System.currentTimeMillis() + config.getV1Oidc().getTokenExpirationSeconds() * 1000L);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getSub())
                .issuer(iss)
                .audience(client_id)
                .issueTime(new Date())
                .expirationTime(expiration)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", scope)
                .build();
        // create JWT token
        SignedJWT jwt = new SignedJWT(jwsHeader, jwtClaimsSet);
        // sign the JWT token
        jwt.sign(signer);
        String access_token = jwt.serialize();

        oidcHandler.putAccessToken(access_token, new V1AccessTokenInfo(user, access_token, expiration, scope, client_id, iss));
        return access_token;
    }

    private String createIdToken(String iss, V1OidcUser user, String client_id, String nonce, String accessToken)
            throws NoSuchAlgorithmException, JOSEException {
        // compute at_hash
        MessageDigest digest = MessageDigest.getInstance(config.getV1Oidc().getIdTokenDigestName());
        digest.reset();
        digest.update(accessToken.getBytes(StandardCharsets.UTF_8));
        byte[] hashBytes = digest.digest();
        byte[] hashBytesLeftHalf = Arrays.copyOf(hashBytes, hashBytes.length / 2);
        Base64URL encodedHash = Base64URL.encode(hashBytesLeftHalf);
        // create JWT claims
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getSub())
                .issuer(iss)
                .audience(client_id)
                .issueTime(new Date())
                .expirationTime(new Date(currentTimeMillis() + config.getV1Oidc().getTokenExpirationSeconds() * 1000L))
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

    private String getAccessTokenFrom(String authorizationHeader, String accessTokenParam) {
        if (!authorizationHeader.startsWith(KEY_IAM_OIDC_TOKEN_TYPE_BEARER.concat(" "))) {
            if (isBlank(accessTokenParam)) {
                return null;
            }
            return accessTokenParam;
        } else {
            return authorizationHeader.substring(7);
        }
    }

    private Set<String> fromSpaceSeparatedString(String s) {
        if (s == null || StringUtils.isEmpty(s))
            return Collections.emptySet();
        return new HashSet<>(asList(s.split(" ")));
    }

    private ResponseEntity<String> wrapUnauthentication() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_HTML);
        responseHeaders.add("WWW-Authenticate", format("Basic realm=\"{}\"", config.getV1Oidc().getWwwRealmName()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(responseHeaders).body(
                "<html><body><h1>401 Unauthorized</h1>IAM V1 OIDC server</body></html>");
    }

    private ResponseEntity<?> wrapErrorJson(String error, String error_description) {
        log.warn("error={} error_description={}", error, error_description);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("error", error);
        map.put("error_description", error_description);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
    }

}
