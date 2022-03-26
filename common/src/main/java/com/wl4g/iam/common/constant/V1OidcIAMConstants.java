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
package com.wl4g.iam.common.constant;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.annotations.Beta;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IAM V1-OIDC constants.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月13日
 * @since
 */
public abstract class V1OidcIAMConstants extends IAMConstants {

    /** V1-OIDC endpoint URIs definitions. */
    public static final String URI_IAM_OIDC_ENDPOINT = "/oidc/v1";
    // https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig
    public static final String URI_IAM_OIDC_ENDPOINT_METADATA = "/.well-known/openid-configuration";
    public static final String URI_IAM_OIDC_ENDPOINT_JWKS = "/jwks";
    public static final String URI_IAM_OIDC_ENDPOINT_TOKEN = "/token";
    public static final String URI_IAM_OIDC_ENDPOINT_INTROSPECTION = "/introspect";
    public static final String URI_IAM_OIDC_ENDPOINT_AUTHORIZE = "/authorize";
    public static final String URI_IAM_OIDC_ENDPOINT_USERINFO = "/userinfo";

    /** V1-OIDC cache key definitions. */
    public static final String CACHE_OIDC_PREFIX = CONF_PREFIX_IAM + "oidc:";
    public static final String CACHE_OIDC_ACCESSTOKEN_PREFIX = CACHE_OIDC_PREFIX + "ak:";
    public static final String CACHE_OIDC_REFRESHTOKEN_PREFIX = CACHE_OIDC_PREFIX + "rk:";
    public static final String CACHE_OIDC_AUTHCODE_PREFIX = CACHE_OIDC_PREFIX + "code:";

    /** V1-OIDC scope definitions. */
    public static final String KEY_IAM_OIDC_SCOPE_OPENID = "openid";
    public static final String KEY_IAM_OIDC_SCOPE_PROFILE = "profile";
    public static final String KEY_IAM_OIDC_SCOPE_EMAIL = "email";
    public static final String KEY_IAM_OIDC_SCOPE_ADDRESS = "address";
    public static final String KEY_IAM_OIDC_SCOPE_PHONE = "phone";

    /** V1-OIDC grant definitions. */
    public static final String KEY_IAM_OIDC_GRANT_AUTHORIZATION_CODE = "authorization_code";
    public static final String KEY_IAM_OIDC_GRANT_IMPLICIT = "implicit";
    public static final String KEY_IAM_OIDC_GRANT_REFRESH_TOKEN = "refresh_token";
    @Deprecated
    public static final String KEY_IAM_OIDC_GRANT_PASSWORD = "password";
    public static final String KEY_IAM_OIDC_GRANT_CLIENT_CREDENTIALS = "client_credentials";
    @Beta
    public static final String KEY_IAM_OIDC_GRANT_DEVICE_CODE = "device_code";

    /** V1-OIDC subject definitions. */
    public static final String KEY_IAM_OIDC_SUBJECT_PUBLIC = "public";
    public static final String KEY_IAM_OIDC_SUBJECT_PAIRWISE = "pairwise";

    /** V1-OIDC response type definitions. */
    public static final String KEY_IAM_OIDC_RESPONSE_TYPE_CODE = "code";
    public static final String KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN = "id_token";
    public static final String KEY_IAM_OIDC_RESPONSE_TYPE_TOKEN = "token";
    public static final String KEY_IAM_OIDC_RESPONSE_TYPE_CODE_IDTOKEN = KEY_IAM_OIDC_RESPONSE_TYPE_CODE + " "
            + KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN;
    public static final String KEY_IAM_OIDC_RESPONSE_TYPE_CODE_TOKEN = KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN + " "
            + KEY_IAM_OIDC_RESPONSE_TYPE_TOKEN;
    public static final String KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN_TOKEN = KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN + " "
            + KEY_IAM_OIDC_RESPONSE_TYPE_TOKEN;

    public static final String KEY_IAM_OIDC_RESPONSE_TYPE_CODE_IDTOKEN_TOKEN = KEY_IAM_OIDC_RESPONSE_TYPE_CODE + " "
            + KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN + " " + KEY_IAM_OIDC_RESPONSE_TYPE_TOKEN;

    /**
     * @see https://openid.net/specs/openid-connect-core-1_0.html#AuthorizationExamples
     */
    public static final List<String> KEY_IAM_OIDC_RESPONSE_TYPE_ALL = asList(KEY_IAM_OIDC_RESPONSE_TYPE_CODE,
            KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN, KEY_IAM_OIDC_RESPONSE_TYPE_CODE_IDTOKEN, KEY_IAM_OIDC_RESPONSE_TYPE_CODE_TOKEN,
            KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN_TOKEN, KEY_IAM_OIDC_RESPONSE_TYPE_CODE_IDTOKEN_TOKEN);

    /** V1-OIDC token type definitions. */
    public static final String KEY_IAM_OIDC_TOKEN_TYPE_BEARER = "Bearer";

    /** V1-OIDC token signing algorithm definitions. */
    @Getter
    @AllArgsConstructor
    public static enum SignAlgorithmSupported {
        PLAIN("none"), S256("SHA-256"), S384("SHA-384"), S512("SHA-512");
        private final String digestAlgName;

        public static String parseDigest(String name) {
            for (SignAlgorithmSupported m : values()) {
                if (StringUtils.equals(m.name(), name)) {
                    return m.getDigestAlgName();
                }
            }
            throw new IllegalArgumentException(format("Invalid digest alg alias name for '%s'", name));
        }
    }

    /** V1-OIDC display definitions. */
    public static final String KEY_IAM_OIDC_DISPLAY_PAGE = "page";

    /** V1-OIDC claims definitions. */
    public static final String KEY_IAM_OIDC_CLAIMS_SUB = "sub";
    public static final String KEY_IAM_OIDC_CLAIMS_ISS = "iss";
    public static final String KEY_IAM_OIDC_CLAIMS_NAME = "name";
    public static final String KEY_IAM_OIDC_CLAIMS_FAMILY_NAME = "family_name";
    public static final String KEY_IAM_OIDC_CLAIMS_GIVEN_NAME = "given_name";
    public static final String KEY_IAM_OIDC_CLAIMS_PREFERRED_USERNAME = "preferred_username";
    public static final String KEY_IAM_OIDC_CLAIMS_EMAIL = "email";
    public static final String KEY_IAM_OIDC_CLAIMS_NONCE = "nonce";
    public static final String KEY_IAM_OIDC_CLAIMS_AT_HASH = "at_hash";

    /** Default JWK configuration resources. */
    public static final String URI_IAM_OIDC_JWK_DEFAULT_RESOURCE = "classpath*:/credentials/oidc/jwks.json";
}