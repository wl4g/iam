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
package com.wl4g.iam.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.filter.reactive.HiddenHttpMethodFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.wl4g.iam.gateway.auth.config.AuthingAutoConfiguration;
import com.wl4g.iam.gateway.circuitbreaker.config.CustomCircuitBreakerAutoConfiguration;
//import com.wl4g.iam.gateway.auth.TokenRelayRefreshGatewayFilterFactory;
import com.wl4g.iam.gateway.console.ConsoleAutoConfiguration;
import com.wl4g.iam.gateway.loadbalance.config.CanaryLoadbalanceAutoConfiguration;
import com.wl4g.iam.gateway.logging.config.DyeingLoggingAutoConfiguration;
import com.wl4g.iam.gateway.metrics.config.CustomMetricsAutoConfiguration;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterAutoConfiguration;
import com.wl4g.iam.gateway.route.config.RouteAutoConfiguration;
import com.wl4g.iam.gateway.server.config.GatewayWebServerAutoConfiguration;
import com.wl4g.iam.gateway.trace.config.TraceAutoConfiguration;

import reactor.core.publisher.Mono;

/**
 * IAM gateway auto configuration.
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2018年9月16日
 * @since
 * @see {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration}
 */
@Configuration
@Import({ GatewayWebServerAutoConfiguration.class, IamRequestLimiterAutoConfiguration.class,
        CustomCircuitBreakerAutoConfiguration.class, RouteAutoConfiguration.class, CanaryLoadbalanceAutoConfiguration.class,
        AuthingAutoConfiguration.class, TraceAutoConfiguration.class, DyeingLoggingAutoConfiguration.class,
        ConsoleAutoConfiguration.class, CustomMetricsAutoConfiguration.class })
public class IamGatewayAutoConfiguration {

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                return chain.filter(exchange);
            }
        };
    }

    // @Bean
    // public WebFilter corsWebFilter() {
    // return (ServerWebExchange ctx, WebFilterChain chain) -> {
    // ServerHttpRequest request = ctx.getRequest();
    // if (!CorsUtils.isCorsRequest(request)) {
    // return chain.filter(ctx);
    // }
    //
    // HttpHeaders requestHeaders = request.getHeaders();
    // ServerHttpResponse response = ctx.getResponse();
    // HttpMethod requestMethod =
    // requestHeaders.getAccessControlRequestMethod();
    // HttpHeaders headers = response.getHeaders();
    // headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
    // requestHeaders.getOrigin());
    // headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
    // requestHeaders.getAccessControlRequestHeaders());
    // if (requestMethod != null) {
    // headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
    // requestMethod.name());
    // }
    // headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    // headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
    // headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "18000L");
    // if (request.getMethod() == HttpMethod.OPTIONS) {
    // response.setStatusCode(HttpStatus.OK);
    // return Mono.empty();
    // }
    // return chain.filter(ctx);
    // };
    // }

}