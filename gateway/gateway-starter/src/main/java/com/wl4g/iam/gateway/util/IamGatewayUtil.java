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
package com.wl4g.iam.gateway.util;

import java.security.Principal;

import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;

/**
 * {@link IamGatewayUtil}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-05 v3.0.0
 * @since v3.0.0
 */
public interface IamGatewayUtil {

    public static final Principal UNKNOWN_PRINCIPAL = new Principal() {
        @Override
        public String getName() {
            return "unknown";
        }
    };

    /**
     * <p>
     * Spring Cloud Gateway VS Zuul filter orders and Pit records:
     * https://blogs.wl4g.com/archives/3401
     * </p>
     * 
     * <p>
     * https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.1.1.RELEASE/single/spring-cloud-gateway.html#_global_filters_2
     * </p>
     */
    public static interface SafeDefaultFilterOrdered {

        int ORDER_IPFILTER = -100;

        int ORDER_REQUEST_SIZE = -90;

        int ORDER_FAULT_FILTER = -80;

        int ORDER_SIMPLE_SIGN_FILTER = -70;

        /**
         * Depends: principal(request limit by principal)
         */
        int ORDER_REQUEST_LIMITER = -60;

        int ORDER_TRACE_FILTER = -50;

        /**
         * Depends: traceId, principal(optional, The dyeing print according to
         * principal log)
         */
        int ORDER_LOGGING_FILTER = -40;

        int ORDER_TRAFFIC_REPLICATION_FILTER = -30;

        int ORDER_CACHE_FILTER = -20;

        int ORDER_RETRY_FILTER = -10;

        //
        // The order of the default built-in filters is here (When order is not
        // specified, route filters are incremented from 1 in the order in which
        // they are configured), for example: RewritePath, RemoveRequestHeader,
        // RemoveResponseHeader, SetResponseHeader, RedirectTo, SecureHeaders,
        // SetStatus ...
        //

        int ORDER_CIRCUITBREAKER = 1000;

        /**
         * see:{@link org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter#LOAD_BALANCER_CLIENT_FILTER_ORDER}
         * see:{@link org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter#ROUTE_TO_URL_FILTER_ORDER}
         */
        int ORDER_CANARY_LOADBALANCER = RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 149;

        int ORDER_HTTPS_TO_HTTP_FILTER = ORDER_CANARY_LOADBALANCER + 10;

    }

}
