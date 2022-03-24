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

import static java.util.Objects.isNull;

import javax.validation.constraints.NotNull;

import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.iam.authc.credential.secure.CredentialsToken;
import com.wl4g.iam.authc.credential.secure.IamCredentialsSecurer;
import com.wl4g.iam.common.constant.V1OidcIAMConstants;
import com.wl4g.iam.common.model.oidc.v1.V1AccessTokenInfo;
import com.wl4g.iam.common.model.oidc.v1.V1AuthorizationCodeInfo;
import com.wl4g.iam.common.model.oidc.v1.V1OidcUserClaims;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.common.subject.IamPrincipal.SimpleParameter;
import com.wl4g.iam.core.authc.IamAuthenticationInfo;
import com.wl4g.iam.crypto.SecureCryptService.CryptKind;
import com.wl4g.iam.handler.AbstractAuthenticatingHandler;
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

    private @Autowired IamCredentialsSecurer securer;
    private @Autowired JedisService jedisService;

    @Override
    public void putAccessToken(String accessToken, V1AccessTokenInfo accessTokenInfo) {
        jedisService.setObjectAsJson(buildAccessTokenKey(accessToken), accessTokenInfo,
                config.getV1Oidc().getDefaultAccessTokenExpirationSeconds());
    }

    @Override
    public V1AccessTokenInfo loadAccessToken(String accessToken) {
        return jedisService.getObjectAsJson(buildAccessTokenKey(accessToken), V1AccessTokenInfo.class);
    }

    @Override
    public void putRefreshToken(String refreshToken, String accessTokenInfo) {
        jedisService.set(buildRefreshTokenKey(refreshToken), accessTokenInfo,
                config.getV1Oidc().getDefaultRefreshTokenExpirationSeconds());
    }

    @Override
    public String loadRefreshToken(String accessToken) {
        return jedisService.get(buildRefreshTokenKey(accessToken));
    }

    @Override
    public void putAuthorizationCode(String authorizationCode, V1AuthorizationCodeInfo authorizationCodeInfo) {
        jedisService.setObjectAsJson(buildAuthorizationCodeKey(authorizationCode), authorizationCodeInfo,
                config.getV1Oidc().getDefaultCodeExpirationSeconds());
    }

    @Override
    public V1OidcUserClaims getV1OidcUser(String loginName) {
        // for test
        IamPrincipal principal = new com.wl4g.iam.common.subject.SimpleIamPrincipal("1", "root",
                "cd446a729ea1d31d712be2ff9c1401d87beb14a811ceb7a61b3a66a4d34177f8", "a3e0b320c73020aa81ebf87bd8611bf1", "", "",
                null);
        // IamPrincipal principal = configurer.getIamUserDetail(new
        // SimpleParameter(loginName));
        return V1OidcUserClaims.builder()
                .iamPrincipal(principal)
                .sub(principal.principalId())
                // TODO
                .name(principal.getName())
                .given_name(principal.getName())
                .family_name(principal.getName())
                .nickname(principal.getName())
                .preferred_username(principal.getName())
                // TODO
                .picture("")
                .locale("")
                .updated_at("")
                .email("")
                .email_verified(false)
                .phone_number("")
                .phone_number_verified(false)
                .build();
    }

    @Override
    public boolean validate(V1OidcUserClaims user, String password) {
        if (isNull(user)) {
            return false;
        }
        CredentialsToken crToken = new CredentialsToken(user.getIamPrincipal().principal(), password, CryptKind.NONE, true);
        return securer.validate(crToken, new IamAuthenticationInfo() {
            private static final long serialVersionUID = 1L;

            @Override
            public PrincipalCollection getPrincipals() {
                // Ignore
                return null;
            }

            @Override
            public Object getCredentials() {
                return user.getIamPrincipal().getStoredCredentials();
            }

            @Override
            public @NotNull IamPrincipal getIamPrincipal() {
                return user.getIamPrincipal();
            }

            @Override
            public @NotNull CodecSource getPublicSalt() {
                return CodecSource.fromHex(user.getIamPrincipal().getPublicSalt());
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

}
