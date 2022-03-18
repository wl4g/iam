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
 * IAM OIDC constants.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月13日
 * @since
 */
public abstract class OidcIAMConstants extends IAMConstants {

    public static final String METADATA_ENDPOINT = "/.well-known/openid-configuration";
    public static final String AUTHORIZATION_ENDPOINT = "/authorize";
    public static final String TOKEN_ENDPOINT = "/token";
    public static final String USERINFO_ENDPOINT = "/userinfo";
    public static final String JWKS_ENDPOINT = "/jwks";
    public static final String INTROSPECTION_ENDPOINT = "/introspect";

    public static final String CACHE_OIDC_PREFIX = CONF_PREFIX_IAM + "oidc:";
    public static final String CACHE_OIDC_ACCESSTOKEN_PREFIX = CACHE_OIDC_PREFIX + "ak:";
    public static final String CACHE_OIDC_AUTHCODE_PREFIX = CACHE_OIDC_PREFIX + "code:";

    /** Based URI with IAM OIDC server controller. */
    public static final String URI_IAM_OIDC_V1_SERVER = "/oidc/v1";

}