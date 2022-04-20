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
 * IAM for gateway constants.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月13日
 * @since
 */
public abstract class GatewayIAMConstants extends IAMConstants {

    public static final String CONF_PREFIX_IAM_GATEWAY = CONF_PREFIX_IAM + ".gateway";
    public static final String CONF_PREFIX_IAM_GATEWAY_SERVER = CONF_PREFIX_IAM_GATEWAY + ".server";
    public static final String CONF_PREFIX_IAM_GATEWAY_RATELIMIT = CONF_PREFIX_IAM_GATEWAY + ".ratelimit";
    public static final String CONF_PREFIX_IAM_GATEWAY_ROUTE = CONF_PREFIX_IAM_GATEWAY + ".route";
    public static final String CONF_PREFIX_IAM_GATEWAY_CIRCUITBREAKER = CONF_PREFIX_IAM_GATEWAY + ".circuitbreaker";
    public static final String CONF_PREFIX_IAM_GATEWAY_LOADBANANER = CONF_PREFIX_IAM_GATEWAY + ".loadbalancer";
    public static final String CONF_PREFIX_IAM_GATEWAY_AUTHING = CONF_PREFIX_IAM_GATEWAY + ".authing";
    public static final String CONF_PREFIX_IAM_GATEWAY_TRACE = CONF_PREFIX_IAM_GATEWAY + ".trace";
    public static final String CONF_PREFIX_IAM_GATEWAY_LOGGING = CONF_PREFIX_IAM_GATEWAY + ".logging";

    public static final String CACHE_PREFIX_IAM_GWTEWAY = CACHE_PREFIX_IAM + ":gateway";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_ROUTES = CACHE_PREFIX_IAM_GWTEWAY + ":routes";

    public static final String CACHE_PREFIX_IAM_GWTEWAY_AUTH = CACHE_PREFIX_IAM_GWTEWAY + ":auth";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_SECRET = CACHE_PREFIX_IAM_GWTEWAY_AUTH + ":sign:secret";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_REPLAY_BLOOM = CACHE_PREFIX_IAM_GWTEWAY_AUTH
            + ":sign:replay:bloom";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_EVENT_SUCCESS = CACHE_PREFIX_IAM_GWTEWAY_AUTH
            + ":sign:event:success";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_EVENT_FAILURE = CACHE_PREFIX_IAM_GWTEWAY_AUTH
            + ":sign:event:failure";

    public static final String CACHE_PREFIX_IAM_GWTEWAY_RATELIMIT = CACHE_PREFIX_IAM_GWTEWAY + ":ratelimit";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_RATELIMIT_EVENT_HITS = CACHE_PREFIX_IAM_GWTEWAY_RATELIMIT + ":event:hits";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_RATELIMIT_CONF_TOKEN = CACHE_PREFIX_IAM_GWTEWAY_RATELIMIT
            + ":config:token";

    public static final String CACHE_SUFFIX_IAM_GATEWAY_EVENT_YYYYMMDD = "yyyyMMdd";

}