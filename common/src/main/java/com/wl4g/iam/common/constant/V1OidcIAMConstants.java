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

    /** V1-OIDC grant definitions. */
    public static final String KEY_IAM_OIDC_GRANT_AUTHORIZATION_CODE = "authorization_code";
    public static final String KEY_IAM_OIDC_GRANT_IMPLICIT = "implicit";
    public static final String KEY_IAM_OIDC_GRANT_REFRESH_TOKEN = "refresh_token";

    /** V1-OIDC subject definitions. */
    public static final String KEY_IAM_OIDC_SUBJECT_PUBLIC = "public";

    /** V1-OIDC response type definitions. */
    public static final String KEY_IAM_OIDC_RESPONSE_TYPE_CODE = "code";
    public static final String KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN = "id_token";
    public static final String KEY_IAM_OIDC_RESPONSE_TYPE_TOKEN = "token";
    public static final String KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN_TOKEN = KEY_IAM_OIDC_RESPONSE_TYPE_IDTOKEN + " "
            + KEY_IAM_OIDC_RESPONSE_TYPE_TOKEN;

    /** V1-OIDC token type definitions. */
    public static final String KEY_IAM_OIDC_TOKEN_TYPE_BEARER = "Bearer";

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

}