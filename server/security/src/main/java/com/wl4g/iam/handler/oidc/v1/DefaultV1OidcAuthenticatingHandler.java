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
package com.wl4g.iam.handler.oidc.v1;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.StringUtils2.isTrue;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseArrayString;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wl4g.iam.authc.credential.secure.CredentialsToken;
import com.wl4g.iam.authc.credential.secure.IamCredentialsSecurer;
import com.wl4g.iam.common.bean.User;
import com.wl4g.iam.common.bean.oidc.OidcClient;
import com.wl4g.iam.common.constant.V1OidcIAMConstants;
import com.wl4g.iam.common.model.oidc.v1.V1AccessTokenInfo;
import com.wl4g.iam.common.model.oidc.v1.V1AuthorizationCodeInfo;
import com.wl4g.iam.common.model.oidc.v1.V1OidcUserClaims;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.core.authc.IamAuthenticationInfo;
import com.wl4g.iam.crypto.SecureCryptService.CryptKind;
import com.wl4g.iam.handler.AbstractAuthenticatingHandler;
import com.wl4g.iam.web.oidc.v1.V1OidcClientConfig;
import com.wl4g.infra.common.codec.CodecSource;
import com.wl4g.infra.support.cache.jedis.JedisService;

/**
 * {@link DefaultV1OidcAuthenticatingHandler}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-18 v1.0.0
 * @since v1.0.0
 */
public class DefaultV1OidcAuthenticatingHandler extends AbstractAuthenticatingHandler implements V1OidcAuthenticatingHandler {

    protected final Cache<String, V1OidcClientConfig> clientConfigCache;

    protected @Autowired IamCredentialsSecurer securer;
    protected @Autowired JedisService jedisService;

    public DefaultV1OidcAuthenticatingHandler(IamProperties config) {
        this.clientConfigCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    }

    @Override
    public V1OidcClientConfig loadClientConfig(String clientId) {
        hasTextOf(clientId, "clientId");

        V1OidcClientConfig clientConfig = clientConfigCache.asMap().get(clientId);
        if (isNull(clientConfig)) {
            synchronized (clientId) {
                clientConfig = clientConfigCache.asMap().get(clientId);
                if (isNull(clientConfig)) {
                    // New client config.
                    clientConfig = V1OidcClientConfig.newInstance(clientId, config.getV1Oidc());
                    clientConfigCache.put(clientId, clientConfig);

                    // Load client config from DB.
                    OidcClient client = configurer.loadOidcClient(clientId);

                    // Overwrite merge to client config.
                    // clientConfig.setBasicRealmName("");
                    clientConfig.setRegistrationToken(client.getRegistrationToken());
                    // TODO
                    clientConfig.setJwksAlgName(null);

                    clientConfig.setStandardFlowEnabled(isTrue(valueOf(client.getStandardFlowEnabled())));
                    clientConfig.setImplicitFlowEnabled(isTrue(valueOf(client.getImplicitFlowEnabled())));
                    clientConfig.setDirectAccessGrantsEnabled(isTrue(valueOf(client.getDirectAccessGrantsEnabled())));
                    clientConfig.setOauth2DeviceCodeEnabled(isTrue(valueOf(client.getOauth2DeviceCodeEnabled())));

                    clientConfig.setAccessTokenSignAlg(client.getAccessTokenSignAlg());
                    clientConfig.setAccessTokenExpirationSeconds(client.getAccessTokenExpirationSec());

                    clientConfig.setRefreshTokenExpirationSeconds(client.getRefreshTokenExpirationSec());
                    clientConfig.setUseRefreshTokenEnabled(isTrue(valueOf(client.getUseRefreshTokenEnabled())));
                    clientConfig.setUseRefreshTokenForClientCredentialsGrantEnabled(
                            isTrue(valueOf(client.getUseRefreshTokenForClientCredentialsGrantEnabled())));

                    clientConfig.setIdTokenSignAlg(client.getIdTokenSignAlg());
                    // TODO
                    // clientConfig.setIdTokenAlgSupported(null);

                    clientConfig.setCodeChallengeEnabled(isTrue(valueOf(client.getCodeChallengeEnabled())));
                    clientConfig.setCodeChallengeExpirationSeconds(client.getCodeChallengeExpirationSec());

                    clientConfig.setClientName(client.getClientName());
                    clientConfig.setClientSecrets(parseJSON(client.getClientSecretsJson(), oidcClientSecretTypeRef));
                    clientConfig.setClientType(client.getClientType());
                    clientConfig.setValidRedirectUris(parseArrayString(client.getValidRedirectUrisJson()));
                    clientConfig.setAdminUri(client.getAdminUri());
                    clientConfig.setLogoUri(client.getLogoUri());
                    clientConfig.setPolicyUri(client.getPolicyUri());
                    clientConfig.setTermsUri(client.getTermsUri());
                    clientConfig.setValidWebOriginUris(parseArrayString(client.getValidWebOriginUrisJson()));

                    clientConfig.setBackchannelLogoutEnabled(isTrue(valueOf(client.getBackchannelLogoutEnabled())));
                    clientConfig.setBackchannelLogoutUri(client.getBackchannelLogoutUri());
                }
            }
        }

        return clientConfig;
    }

    @Override
    public void putAccessToken(String accessToken, V1AccessTokenInfo accessTokenInfo) {
        jedisService.setObjectAsJson(buildAccessTokenKey(accessToken), accessTokenInfo,
                loadClientConfig(accessTokenInfo.getClientId()).getAccessTokenExpirationSeconds());
    }

    @Override
    public V1AccessTokenInfo loadAccessToken(String accessToken) {
        return jedisService.getObjectAsJson(buildAccessTokenKey(accessToken), V1AccessTokenInfo.class);
    }

    @Override
    public void putRefreshToken(String refreshToken, V1AccessTokenInfo accessTokenInfo) {
        jedisService.set(buildRefreshTokenKey(refreshToken), accessTokenInfo.getAccessToken(),
                loadClientConfig(accessTokenInfo.getClientId()).getRefreshTokenExpirationSeconds());
    }

    @Override
    public String loadRefreshToken(String accessToken) {
        return jedisService.get(buildRefreshTokenKey(accessToken));
    }

    @Override
    public void putAuthorizationCode(String authorizationCode, V1AuthorizationCodeInfo authorizationCodeInfo) {
        jedisService.setObjectAsJson(buildAuthorizationCodeKey(authorizationCode), authorizationCodeInfo,
                loadClientConfig(authorizationCodeInfo.getClient_id()).getCodeChallengeExpirationSeconds());
    }

    @Override
    public V1OidcUserClaims getV1OidcUser(String loginName) {
        IamPrincipal principal = configurer.getIamUserDetail(new IamPrincipal.SimpleParameter(loginName));
        User user = principal.attributes().getUser();
        return V1OidcUserClaims.builder()
                .principal(principal)
                .sub(user.getSubject())
                .name(user.getName())
                .given_name(user.getGiven_name())
                .family_name(user.getFamily_name())
                .nickname(user.getNickname())
                .preferred_username(user.getPreferred_username())
                .locale(user.getLocale())
                .picture(user.getPicture())
                .zoneinfo(user.getZoneinfo())
                .updated_at(user.getUpdateDate())
                .email(user.getEmail())
                .email_verified(false)
                .phone_number(user.getPhone())
                .phone_number_verified(false)
                .build();
    }

    @Override
    public boolean validate(V1OidcUserClaims claims, String password) {
        if (isNull(claims)) {
            return false;
        }
        CredentialsToken crToken = new CredentialsToken(claims.getPrincipal().principal(), password, CryptKind.NONE, true);
        return securer.validate(crToken, new IamAuthenticationInfo() {
            private static final long serialVersionUID = 1L;

            @Override
            public PrincipalCollection getPrincipals() {
                // Ignore
                return null;
            }

            @Override
            public Object getCredentials() {
                return claims.getPrincipal().getStoredCredentials();
            }

            @Override
            public @NotNull IamPrincipal getIamPrincipal() {
                return claims.getPrincipal();
            }

            @Override
            public @NotNull CodecSource getPublicSalt() {
                return CodecSource.fromHex(claims.getPrincipal().getPublicSalt());
            }
        });
    }

    @Override
    public V1AuthorizationCodeInfo loadAuthorizationCode(String authorizationCode) {
        return jedisService.getObjectAsJson(buildAuthorizationCodeKey(authorizationCode), V1AuthorizationCodeInfo.class);
    }

    private String buildAccessTokenKey(String accessToken) {
        return V1OidcIAMConstants.CACHE_OIDC_ACCESSTOKEN_PREFIX.concat(new CodecSource(accessToken).toHex());
    }

    private String buildRefreshTokenKey(String refreshToken) {
        return V1OidcIAMConstants.CACHE_OIDC_REFRESHTOKEN_PREFIX.concat(new CodecSource(refreshToken).toHex());
    }

    private String buildAuthorizationCodeKey(String authorizationCode) {
        return V1OidcIAMConstants.CACHE_OIDC_AUTHCODE_PREFIX.concat(new CodecSource(authorizationCode).toHex());
    }

    private static final TypeReference<List<OidcClient.ClientSecretInfo>> oidcClientSecretTypeRef = new TypeReference<List<OidcClient.ClientSecretInfo>>() {
    };

}
