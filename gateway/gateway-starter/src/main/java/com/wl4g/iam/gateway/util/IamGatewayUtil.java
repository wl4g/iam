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

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Objects.nonNull;

import java.security.Principal;

import javax.annotation.Nullable;

import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.infra.core.constant.BaseConstants;

/**
 * {@link IamGatewayUtil}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-05 v3.0.0
 * @since v3.0.0
 */
public abstract class IamGatewayUtil {

    public static final Principal UNKNOWN_PRINCIPAL = new Principal() {
        @Override
        public String getName() {
            return "unknown";
        }
    };

    @Nullable
    public static String getRouteId(ServerWebExchange exchange) {
        notNullOf(exchange, "exchange");
        Object route = exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (nonNull(route)) {
            return ((Route) route).getId();
        }
        return null;
    }

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
    public static abstract class SafeFilterOrdered extends BaseConstants {

        public static final int ORDER_IPFILTER = getIntegerProperty("ISCG_ORDER_IPFILTER", -100);

        public static final int ORDER_REQUEST_SIZE = getIntegerProperty("ISCG_ORDER_REQUEST_SIZE", -90);

        public static final int ORDER_FAULT = getIntegerProperty("ISCG_ORDER_FAULT", -80);

        public static final int ORDER_SIMPLE_SIGN = getIntegerProperty("ISCG_ORDER_SIMPLE_SIGN", -70);

        public static final int ORDER_TRACE = getIntegerProperty("ISCG_ORDER_TRACE", -60);

        /**
         * Depends: traceId, principal(optional, The dyeing print according to
         * principal log)
         */
        public static final int ORDER_LOGGING = getIntegerProperty("ISCG_ORDER_LOGGING", -50);

        /**
         * Depends: principal(request limit by principal)
         */
        public static final int ORDER_REQUEST_LIMITER = getIntegerProperty("ISCG_ORDER_REQUEST_LIMITER", -40);

        public static final int ORDER_TRAFFIC_REPLICATION = getIntegerProperty("ISCG_ORDER_TRAFFIC_REPLICATION", -30);

        public static final int ORDER_REQUEST_CACHE = getIntegerProperty("ISCG_ORDER_REQUEST_CACHE", -20);

        public static final int ORDER_RETRY = getIntegerProperty("ISCG_ORDER_RETRY", -10);

        //
        // The order of the default built-in filters is here (When order is not
        // specified, route filters are incremented from 1 in the order in which
        // they are configured), for example: RewritePath, RemoveRequestHeader,
        // RemoveResponseHeader, SetResponseHeader, RedirectTo, SecureHeaders,
        // SetStatus ...
        //

        public static final int ORDER_CIRCUITBREAKER = getIntegerProperty("ISCG_ORDER_CIRCUITBREAKER", 1000);

        /**
         * see:{@link org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter#LOAD_BALANCER_CLIENT_FILTER_ORDER}
         * see:{@link org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter#ROUTE_TO_URL_FILTER_ORDER}
         */
        public static final int ORDER_CANARY_LOADBALANCER = getIntegerProperty("ISCG_ORDER_CANARY_LOADBALANCER",
                RouteToRequestUrlFilter.ROUTE_TO_URL_FILTER_ORDER + 149);

        public static final int ORDER_HTTPS_TO_HTTP = getIntegerProperty("ISCG_ORDER_HTTPS_TO_HTTP",
                ORDER_CANARY_LOADBALANCER + 10);

    }

}
