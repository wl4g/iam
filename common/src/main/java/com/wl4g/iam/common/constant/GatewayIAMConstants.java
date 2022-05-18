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

    //
    // (Static) configuration properties prefix definitions.
    //

    public static final String CONF_PREFIX_IAM_GATEWAY = CONF_PREFIX_IAM + ".gateway";
    public static final String CONF_PREFIX_IAM_GATEWAY_SERVER = CONF_PREFIX_IAM_GATEWAY + ".server";
    public static final String CONF_PREFIX_IAM_GATEWAY_IPFILTER = CONF_PREFIX_IAM_GATEWAY + ".ipfilter";
    public static final String CONF_PREFIX_IAM_GATEWAY_REQUESTSIZE = CONF_PREFIX_IAM_GATEWAY + ".requestsize";
    public static final String CONF_PREFIX_IAM_GATEWAY_FAULT = CONF_PREFIX_IAM_GATEWAY + ".fault";
    public static final String CONF_PREFIX_IAM_GATEWAY_SECURITY = CONF_PREFIX_IAM_GATEWAY + ".security";
    public static final String CONF_PREFIX_IAM_GATEWAY_TRACE = CONF_PREFIX_IAM_GATEWAY + ".trace";
    public static final String CONF_PREFIX_IAM_GATEWAY_LOGGING = CONF_PREFIX_IAM_GATEWAY + ".logging";
    public static final String CONF_PREFIX_IAM_GATEWAY_REQUESTLIMIT = CONF_PREFIX_IAM_GATEWAY + ".requestlimit";
    public static final String CONF_PREFIX_IAM_GATEWAY_ROUTE = CONF_PREFIX_IAM_GATEWAY + ".route";
    public static final String CONF_PREFIX_IAM_GATEWAY_RETRY = CONF_PREFIX_IAM_GATEWAY + ".retry";
    public static final String CONF_PREFIX_IAM_GATEWAY_CIRCUITBREAKER = CONF_PREFIX_IAM_GATEWAY + ".circuitbreaker";
    public static final String CONF_PREFIX_IAM_GATEWAY_LOADBANANER = CONF_PREFIX_IAM_GATEWAY + ".loadbalancer";
    public static final String CONF_PREFIX_IAM_GATEWAY_RESPONSECACHE = CONF_PREFIX_IAM_GATEWAY + ".responsecache";
    public static final String CONF_PREFIX_IAM_GATEWAY_TRAFFIC = CONF_PREFIX_IAM_GATEWAY + ".traffic";

    //
    // (Dynamic) configuration cache prefix definitions.
    //

    public static final String CACHE_PREFIX_IAM_GWTEWAY = CACHE_PREFIX_IAM + ":gateway";

    public static final String CACHE_PREFIX_IAM_GWTEWAY_IPFILTER = CACHE_PREFIX_IAM_GWTEWAY + ":ipfilter";

    public static final String CACHE_PREFIX_IAM_GWTEWAY_ROUTES = CACHE_PREFIX_IAM_GWTEWAY + ":routes";

    public static final String CACHE_PREFIX_IAM_GWTEWAY_AUTH = CACHE_PREFIX_IAM_GWTEWAY + ":auth";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_SECRET = CACHE_PREFIX_IAM_GWTEWAY_AUTH + ":sign:secret";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_REPLAY_BLOOM = CACHE_PREFIX_IAM_GWTEWAY_AUTH
            + ":sign:replay:bloom";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_EVENT_SUCCESS = CACHE_PREFIX_IAM_GWTEWAY_AUTH
            + ":sign:event:success";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_AUTH_SIGN_EVENT_FAILURE = CACHE_PREFIX_IAM_GWTEWAY_AUTH
            + ":sign:event:failure";

    public static final String CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT = CACHE_PREFIX_IAM_GWTEWAY + ":requestlimit";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF_RATE = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT
            + ":config:rate";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF_QUOTA = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT
            + ":config:quota";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_TOKEN_RATE = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT
            + ":token:rate";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_TOKEN_QUOTA = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT
            + ":token:quota";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_EVENT_HITS_RATE = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT
            + ":event:hits:rate";
    public static final String CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_EVENT_HITS_QUOTA = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT
            + ":event:hits:quota";

    public static final String CACHE_SUFFIX_IAM_GATEWAY_RESPONSECACHE = CACHE_PREFIX_IAM_GWTEWAY + ":responsecache:data";

    public static final String CACHE_SUFFIX_IAM_GATEWAY_EVENT_YYMMDD = "yyMMdd";

}