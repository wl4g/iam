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

import static com.wl4g.iam.common.constant.OidcIAMConstants.AUTHORIZATION_ENDPOINT;
import static com.wl4g.iam.common.constant.OidcIAMConstants.INTROSPECTION_ENDPOINT;
import static com.wl4g.iam.common.constant.OidcIAMConstants.JWKS_ENDPOINT;
import static com.wl4g.iam.common.constant.OidcIAMConstants.METADATA_ENDPOINT;
import static com.wl4g.iam.common.constant.OidcIAMConstants.TOKEN_ENDPOINT;
import static com.wl4g.iam.common.constant.OidcIAMConstants.USERINFO_ENDPOINT;
import static java.lang.String.format;

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
import java.util.HashMap;
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
import com.wl4g.iam.common.model.oidc.v1.V1AccessTokenInfo;
import com.wl4g.iam.common.model.oidc.v1.V1AuthorizationCodeInfo;
import com.wl4g.iam.common.model.oidc.v1.V1MetadataEndpointModel;
import com.wl4g.iam.common.model.oidc.v1.V1OidcUser;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.core.exception.IamException;
import com.wl4g.iam.web.BaseIamController;
import com.wl4g.infra.common.codec.Encodes;
import com.wl4g.infra.common.resource.StreamResource;
import com.wl4g.infra.common.resource.resolver.ClassPathResourcePatternResolver;

/**
 * IAM OIDC authentication controller implements
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月22日
 * @since
 */
public class V1OidcServerAuthenticatingController extends BaseIamController {

    private @Autowired IamProperties config;

    private JWSSigner signer;
    private JWKSet pubJWKSet;
    private JWSHeader jwsHeader;

    private final Map<String, V1AccessTokenInfo> accessTokens = new HashMap<>();
    private final Map<String, V1AuthorizationCodeInfo> authorizationCodes = new HashMap<>();
    private static final SecureRandom random = new SecureRandom();

    @PostConstruct
    public void init() throws IOException, ParseException, JOSEException {
        log.info("Initializing OIDC JWK ...");

        ClassPathResourcePatternResolver resolver = new ClassPathResourcePatternResolver(
                Thread.currentThread().getContextClassLoader());
        Set<StreamResource> resources = resolver.getResources(config.getOidc().getJwksJsonResource());
        if (resources.isEmpty()) {
            throw new IamException(format("Not found jwks resource by %s", config.getOidc().getJwksJsonResource()));
        }
        StreamResource jwksRes = resources.iterator().next();
        if (resources.size() > 1) {
            log.warn(format("[WARNNING] Found multi jwks resources %s by %s, Using the first one by default %s", resources,
                    config.getOidc().getJwksJsonResource(), jwksRes));
        }
        JWKSet jwkSet = JWKSet.load(jwksRes.getInputStream());
        JWK key = jwkSet.getKeys().get(0);
        this.signer = new RSASSASigner((RSAKey) key);
        this.pubJWKSet = jwkSet.toPublicJWKSet();
        this.jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse(config.getOidc().getJwsAlgorithmName())).keyID(key.getKeyID())
                .build();
    }

    /**
     * Provides OIDC metadata. See the spec at
     * https://openid.net/specs/openid-connect-discovery-1_0.html
     */
    @RequestMapping(value = METADATA_ENDPOINT, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public V1MetadataEndpointModel metadata(UriComponentsBuilder uriBuilder, HttpServletRequest req) {
        log.info("called '{}' from '{}'", METADATA_ENDPOINT, req.getRemoteHost());

        String urlPrefix = uriBuilder.replacePath(null).build().encode().toUriString();
        // https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata
        // https://tools.ietf.org/html/rfc8414#section-2
        return V1MetadataEndpointModel.builder()
                .issuer(urlPrefix.concat("/")) // REQUIRED
                .authorization_endpoint(urlPrefix.concat(AUTHORIZATION_ENDPOINT)) // REQUIRED
                .token_endpoint(urlPrefix.concat(TOKEN_ENDPOINT)) // REQUIRED
                .userinfo_endpoint(urlPrefix.concat(USERINFO_ENDPOINT)) // RECOMMENDED
                .jwks_uri(urlPrefix.concat(JWKS_ENDPOINT)) // REQUIRED
                .introspection_endpoint(urlPrefix.concat(INTROSPECTION_ENDPOINT))
                .scopes_supported(Arrays.asList("openid", "profile", "email")) // REQUIRED
                .response_types_supported(Arrays.asList("id_token token", "code")) // REQUIRED
                .grant_types_supported(Arrays.asList("authorization_code", "implicit")) // OPTIONAL
                .subject_types_supported(Collections.singletonList("public")) // REQUIRED
                .id_token_signing_alg_values_supported(Arrays.asList("RS256", "none")) // REQUIRED
                .claims_supported(Arrays.asList("sub", "iss", "name", "family_name", "given_name", "preferred_username", "email"))
                // PKCE support advertised
                .code_challenge_methods_supported(Arrays.asList("plain", "S256"))
                .build();
    }

    /**
     * Provides JSON Web Key Set containing the public part of the key used to
     * sign ID tokens.
     */
    @RequestMapping(value = JWKS_ENDPOINT, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> jwks(HttpServletRequest req) {
        log.info("called '{}' from '{}'", JWKS_ENDPOINT, req.getRemoteHost());
        return ResponseEntity.ok().body(pubJWKSet.toString());
    }

    /**
     * Provides claims about a user. Requires a valid access token.
     */
    @RequestMapping(value = USERINFO_ENDPOINT, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(allowedHeaders = { "Authorization", "Content-Type" })
    public ResponseEntity<?> userinfo(
            @RequestHeader("Authorization") String auth,
            @RequestParam(required = false) String access_token,
            HttpServletRequest req) {

        log.info("called '{}' from '{}' auth={}, access_token={}", USERINFO_ENDPOINT, req.getRemoteHost(), auth, access_token);

        if (!auth.startsWith("Bearer ")) {
            if (access_token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token");
            }
            auth = access_token;
        } else {
            auth = auth.substring(7);
        }
        V1AccessTokenInfo accessToken = accessTokens.get(auth);
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("access token not found");
        }
        Set<String> scopes = setFromSpaceSeparatedString(accessToken.getScope());
        Map<String, Object> m = new LinkedHashMap<>();
        V1OidcUser user = accessToken.getUser();
        m.put("sub", user.getSub());
        if (scopes.contains("profile")) {
            user = V1OidcUser.builder()
                    .name(user.getName())
                    .family_name(user.getFamily_name())
                    .given_name(user.getGiven_name())
                    .preferred_username(user.getPreferred_username())
                    .build();
        }
        if (scopes.contains("email")) {
            user = V1OidcUser.builder().email(user.getEmail()).build();
        }
        return ResponseEntity.ok().body(user);
    }

    /**
     * Provides information about a supplied access token.
     */
    @RequestMapping(value = INTROSPECTION_ENDPOINT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> introspection(
            @RequestParam String token,
            @RequestHeader("Authorization") String auth,
            HttpServletRequest req) {

        log.info("called '{}' from '{}', token={}, auth={} ", INTROSPECTION_ENDPOINT, req.getRemoteHost(), token, auth);

        Map<String, Object> m = new LinkedHashMap<>();
        V1AccessTokenInfo accessToken = accessTokens.get(token);
        if (accessToken == null) {
            log.error("token not found in memory: {}", token);
            m.put("active", false);
        } else {
            log.info("found token for user {}, releasing scopes: {}", accessToken.getUser().getSub(), accessToken.getScope());
            // https://tools.ietf.org/html/rfc7662#section-2.2 for all claims
            m.put("active", true);
            m.put("scope", accessToken.getScope());
            m.put("client_id", accessToken.getClientId());
            m.put("username", accessToken.getUser().getSub());
            m.put("token_type", "Bearer");
            m.put("exp", accessToken.getExpiration().toInstant().toEpochMilli());
            m.put("sub", accessToken.getUser().getSub());
            m.put("iss", accessToken.getIss());
        }
        return ResponseEntity.ok().body(m);
    }

    /**
     * Provides token endpoint.
     */
    @RequestMapping(value = TOKEN_ENDPOINT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
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

        log.info("called '{}' from '{}', grant_type={} code={} redirect_uri={} client_id={}", TOKEN_ENDPOINT, req.getRemoteHost(),
                grant_type, code, redirect_uri, client_id);

        if (!"authorization_code".equals(grant_type)) {
            return jsonError("unsupported_grant_type", "grant_type is not authorization_code");
        }
        V1AuthorizationCodeInfo codeInfo = authorizationCodes.get(code);
        if (codeInfo == null) {
            return jsonError("invalid_grant", "code not valid");
        }
        if (!redirect_uri.equals(codeInfo.getRedirect_uri())) {
            return jsonError("invalid_request", "redirect_uri not valid");
        }
        if (codeInfo.getCodeChallenge() != null) {
            // check PKCE
            if (code_verifier == null) {
                return jsonError("invalid_request", "code_verifier missing");
            }
            if ("S256".equals(codeInfo.getCodeChallengeMethod())) {
                MessageDigest s256 = MessageDigest.getInstance(config.getOidc().getIdTokenDigestName());
                s256.reset();
                s256.update(code_verifier.getBytes(StandardCharsets.UTF_8));
                String hashedVerifier = Base64URL.encode(s256.digest()).toString();
                if (!codeInfo.getCodeChallenge().equals(hashedVerifier)) {
                    log.warn("code_verifier {} hashed using S256 to {} does not match code_challenge {}", code_verifier,
                            hashedVerifier, codeInfo.getCodeChallenge());
                    return jsonError("invalid_request", "code_verifier not correct");
                }
                log.info("code_verifier OK");
            } else {
                if (!codeInfo.getCodeChallenge().equals(code_verifier)) {
                    log.warn("code_verifier {} does not match code_challenge {}", code_verifier, codeInfo.getCodeChallenge());
                    return jsonError("invalid_request", "code_verifier not correct");
                }
            }
        }
        // return access token
        Map<String, String> map = new LinkedHashMap<>();
        String accessToken = createAccessToken(codeInfo.getIss(), codeInfo.getUser(), codeInfo.getClient_id(),
                codeInfo.getScope());
        map.put("access_token", accessToken);
        map.put("token_type", "Bearer");
        map.put("expires_in", String.valueOf(config.getOidc().getTokenExpirationSeconds()));
        map.put("scope", codeInfo.getScope());
        map.put("id_token",
                createIdToken(codeInfo.getIss(), codeInfo.getUser(), codeInfo.getClient_id(), codeInfo.getNonce(), accessToken));
        return ResponseEntity.ok(map);
    }

    /**
     * Provides authorization endpoint.
     */
    @RequestMapping(value = AUTHORIZATION_ENDPOINT, method = RequestMethod.GET)
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

        log.info("called '{}' from '{}', scope={} response_type={} client_id={} redirect_uri={}", AUTHORIZATION_ENDPOINT,
                req.getRemoteHost(), scope, response_type, client_id, redirect_uri);

        if (auth == null) {
            log.info("User and password not provided. scope={} response_type={} client_id={} redirect_uri={}", scope,
                    response_type, client_id, redirect_uri);
            return response401();
        } else {
            String[] creds = new String(Base64.getDecoder().decode(auth.split(" ")[1])).split(":", 2);
            String login = creds[0];
            String password = creds[1];
            V1OidcUser user = getV1OidcUser();
            if (user.getLogin_name().equals(login) && user.getPassword().equals(password)) {
                log.info("password for user {} is correct", login);
                Set<String> responseType = setFromSpaceSeparatedString(response_type);
                String iss = uriBuilder.replacePath("/").build().encode().toUriString();
                // implicit flow
                if (responseType.contains("token")) {
                    log.info("Using implicit flow. scope={} response_type={} client_id={} redirect_uri={}", scope, response_type,
                            client_id, redirect_uri);
                    String access_token = createAccessToken(iss, user, client_id, scope);
                    String id_token = createIdToken(iss, user, client_id, nonce, access_token);
                    String url = redirect_uri + "#" + "access_token=" + Encodes.urlEncode(access_token) + "&token_type=Bearer"
                            + "&state=" + Encodes.urlEncode(state) + "&expires_in=" + config.getOidc().getTokenExpirationSeconds()
                            + "&id_token=" + Encodes.urlEncode(id_token);
                    return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
                }
                // authorization code flow
                else if (responseType.contains("code")) {
                    log.info("Using authorization code flow {}", code_challenge != null ? "with PKCE" : "");
                    String code = createAuthorizationCode(code_challenge, code_challenge_method, client_id, redirect_uri, user,
                            iss, scope, nonce);
                    String url = redirect_uri + "?" + "code=" + code + "&state=" + Encodes.urlEncode(state);
                    return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
                } else {
                    String url = redirect_uri + "#" + "error=unsupported_response_type";
                    return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
                }
            } else {
                log.info("Wrong user and password combination. scope={} response_type={} client_id={} redirect_uri={}", scope,
                        response_type, client_id, redirect_uri);
                return response401();
            }
        }
    }

    private V1OidcUser getV1OidcUser() {
        // TODO
        return new V1OidcUser();
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
        log.info("issuing code={}", code);
        authorizationCodes.put(code, new V1AuthorizationCodeInfo(code_challenge, code_challenge_method, code, client_id,
                redirect_uri, user, iss, scope, nonce));
        return code;
    }

    private String createAccessToken(String iss, V1OidcUser user, String client_id, String scope) throws JOSEException {
        // create JWT claims
        Date expiration = new Date(System.currentTimeMillis() + config.getOidc().getTokenExpirationSeconds() * 1000L);
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
        accessTokens.put(access_token, new V1AccessTokenInfo(user, access_token, expiration, scope, client_id, iss));
        return access_token;
    }

    private String createIdToken(String iss, V1OidcUser user, String client_id, String nonce, String accessToken)
            throws NoSuchAlgorithmException, JOSEException {
        // compute at_hash
        MessageDigest digest = MessageDigest.getInstance(config.getOidc().getIdTokenDigestName());
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
                .expirationTime(new Date(System.currentTimeMillis() + config.getOidc().getTokenExpirationSeconds() * 1000L))
                .jwtID(UUID.randomUUID().toString())
                .claim("nonce", nonce)
                .claim("at_hash", encodedHash)
                .build();
        // create JWT token
        SignedJWT myToken = new SignedJWT(jwsHeader, jwtClaimsSet);
        // sign the JWT token
        myToken.sign(signer);
        return myToken.serialize();
    }

    private ResponseEntity<String> response401() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_HTML);
        responseHeaders.add("WWW-Authenticate", format("Basic realm=\"{}\"", config.getOidc().getBearerRealmName()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(responseHeaders).body(
                "<html><body><h1>401 Unauthorized</h1>IAM OIDC server</body></html>");
    }

    private Set<String> setFromSpaceSeparatedString(String s) {
        if (s == null || StringUtils.isEmpty(s))
            return Collections.emptySet();
        return new HashSet<>(Arrays.asList(s.split(" ")));
    }

    private ResponseEntity<?> jsonError(String error, String error_description) {
        log.warn("error={} error_description={}", error, error_description);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("error", error);
        map.put("error_description", error_description);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
    }

}
