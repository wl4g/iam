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
package com.wl4g.iam.config.properties;

import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_LOGIN_THEMEM_BASIC_REALM_DEFAULT;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_LOGIN_THEMEM_IAM;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getFieldValues;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.springframework.util.Assert.isTrue;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.List;

import com.nimbusds.jose.JWSAlgorithm;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.JWSAlgorithmType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.TokenSignAlgorithmType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * IAM V1-OIDC configuration properties
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-17 v1.0.0
 * @since v1.0.0
 * @see https://openid.net/specs/openid-connect-core-1_0.html#AuthResponseValidation
 */
@Getter
@Setter
@ToString
@SuperBuilder
public class V1OidcProperties implements Serializable {
    private static final long serialVersionUID = -2694422471852860689L;

    private String defaultJwksSignAlg; // Default ns(tenant) jwks sign alg
    private String defaultBasicRealmName; // Default ns(tenant) basic realm name
    private String serviceDocumentation; // Provide service documentation URI
    private DefaultProtocolProperties defaultProtocolProperties;

    public V1OidcProperties() {
        this.defaultJwksSignAlg = JWSAlgorithmType.getDefault().name();
        this.defaultBasicRealmName = KEY_IAM_OIDC_LOGIN_THEMEM_BASIC_REALM_DEFAULT;
        this.serviceDocumentation = "https://oidc.iam.wl4g.com/connect/service_documentation.html";
    }

    public void setDefaultJwksAlgName(String defaultJwksSignAlg) {
        List<JWSAlgorithm> algs = safeList(
                getFieldValues(JWSAlgorithm.class, new int[] { Modifier.PRIVATE }, new String[] { "serialVersionUID" }));
        isTrue(algs.stream().map(alg -> alg.getName()).anyMatch(alg -> equalsIgnoreCase(alg, defaultJwksSignAlg)),
                format("Invalid jwks alg is '%s', but supported are: %s", defaultJwksSignAlg, algs.toString()));
        this.defaultJwksSignAlg = defaultJwksSignAlg;
    }

    @Getter
    @Setter
    @ToString
    @SuperBuilder
    public static class DefaultProtocolProperties implements Serializable {
        private static final long serialVersionUID = 4776976112803043619L;

        // Generic OpenID Connect Configuration
        private String loginTheme;
        private boolean standardFlowEnabled;
        private boolean implicitFlowEnabled;
        private boolean directAccessGrantsEnabled;
        private boolean oauth2DeviceCodeEnabled;
        private int deviceCodeExpirationSeconds;

        // Fine Grain OpenID Connect Configuration

        private String accessTokenSignAlg;
        private int accessTokenExpirationSeconds;

        private String idTokenSignAlg;
        private String idTokenEncryptKeyMgtAlg;
        private String idTokenEncryptContentAlg;

        // OpenID Connect Compatibility Modes

        private boolean useRefreshTokenEnabled;
        private int refreshTokenExpirationSeconds;

        /**
         * If this is on, a refresh token willbe created and added to the token
         * response if the client credentials grant is used the Oauth 2.0
         * RFC6749 Section4.4.3 states that a refresh token should not be
         * generated when client credentials grant is used If this is off then
         * no refresh token will be generated and the associateduser session
         * will be removed.
         */
        private boolean useRefreshTokenForClientCredentialsGrantEnabled;
        private boolean mustOpenidScopeEnabled;

        // Advanced Settings

        private int codeChallengeExpirationSeconds;

        // Credentials Information

        private String registrationToken;

        public DefaultProtocolProperties() {
            // Generic OpenID Connect Configuration
            //
            this.loginTheme = KEY_IAM_OIDC_LOGIN_THEMEM_IAM;
            this.standardFlowEnabled = true;
            this.implicitFlowEnabled = false;
            this.directAccessGrantsEnabled = false;
            // E.g:https://docs.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-device-code#device-authorization-request
            this.deviceCodeExpirationSeconds = 15 * 60;

            // Fine Grain OpenID Connect Configuration
            //
            this.accessTokenSignAlg = TokenSignAlgorithmType.getDefault().name();
            this.accessTokenExpirationSeconds = 60 * 60;

            this.idTokenSignAlg = TokenSignAlgorithmType.getDefault().name();
            // TODO
            // this.idTokenEncryptKeyMgtAlg = "";
            // this.idTokenEncryptContentAlg = "";

            // OpenID Connect Compatibility Modes
            //
            this.useRefreshTokenEnabled = false;
            this.useRefreshTokenForClientCredentialsGrantEnabled = false;
            this.refreshTokenExpirationSeconds = 60 * 60 * 24;
            this.mustOpenidScopeEnabled = true;

            // Advanced Settings
            //
            this.codeChallengeExpirationSeconds = 10;

            // Credentials Information
            //
            this.registrationToken = null;
        }

        public void setAccessTokenSignAlg(String accessTokenAlgName) {
            hasTextOf(accessTokenAlgName, "accessTokenSignAlg");
            List<String> definitionAlgNames = asList(TokenSignAlgorithmType.values()).stream().map(d -> d.name()).collect(
                    toList());
            isTrue(definitionAlgNames.stream().anyMatch(d -> equalsIgnoreCase(d, accessTokenAlgName)),
                    format("Invalid access token alg is '%s', but supported are: %s", accessTokenAlgName, definitionAlgNames));
            this.accessTokenSignAlg = accessTokenAlgName;
        }

        public void setAccessTokenExpirationSeconds(int accessTokenExpirationSeconds) {
            isTrue(accessTokenExpirationSeconds > 0, "accessTokenExpirationSeconds must >0");
            this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        }

        public void setRefreshTokenExpirationSeconds(int refreshTokenExpirationSeconds) {
            isTrue(refreshTokenExpirationSeconds > 0, "refreshTokenExpirationSeconds must >0");
            this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
        }

        public void setIdTokenSignAlg(String idTokenSignAlg) {
            hasTextOf(idTokenSignAlg, "idTokenSignAlg");
            List<String> definitionAlgNames = asList(TokenSignAlgorithmType.values()).stream().map(d -> d.name()).collect(
                    toList());
            isTrue(definitionAlgNames.stream().anyMatch(d -> equalsIgnoreCase(d, idTokenSignAlg)),
                    format("Invalid id token sign alg is '%s', but supported are: %s", idTokenSignAlg, definitionAlgNames));
            this.idTokenSignAlg = idTokenSignAlg;
        }

        public void setCodeChallengeExpirationSeconds(int codeChallengeExpirationSeconds) {
            isTrue(codeChallengeExpirationSeconds > 0, "codeChallengeExpirationSeconds must >0");
            this.codeChallengeExpirationSeconds = codeChallengeExpirationSeconds;
        }

    }

}