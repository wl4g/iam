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
package com.wl4g.iam.gateway.logging;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.logging.config.LoggingProperties;
import com.wl4g.iam.gateway.trace.config.TraceProperties;
import com.wl4g.infra.common.lang.FastTimeClock;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link LoggingGlobalFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-02 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private @Autowired TraceProperties traceConfig;
    private @Autowired LoggingProperties loggingConfig;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();

        // Check if printing flight logs is enabled.
        // If the mandatory switch is not set, it is determined whether to
        // enable logging according to the preference switch, otherwise it is
        // determined whether to enable logging according to the mandatory
        // switch, the default mandatory switch is empty, the preference switch
        // is enabled.
        if (isNull(loggingConfig.getRequiredPrintFlightLogging())) {
            String value = headers.getFirst(loggingConfig.getPreferredPrintFlightHeaderName());
            if (loggingConfig.isFallbackToGetFromQueryParam()) {
                value = exchange.getRequest().getQueryParams().getFirst(loggingConfig.getPreferredPrintFlightHeaderName());
            }
            // As long as this header is present, is enabled.
            if (isBlank(value)) {
                return chain.filter(exchange);
            }
        } else if (!loggingConfig.getRequiredPrintFlightLogging()) {
            return chain.filter(exchange);
        }

        String traceId = headers.getFirst(traceConfig.getTraceIdRequestHeader());
        StringBuilder requestLog = new StringBuilder(300);
        List<Object> requestLogArgs = new ArrayList<>(16);
        requestLog.append("\n\n---------- IAM Gateway Request Log Begin  ----------\n");
        // Print HTTP URI. (E.g: 997ac7d2-2056-419b-883b-6969aae77e3e :: GET
        // /example/foo/bar)
        requestLog.append("{} :: {} {}\n");
        String requestMethod = exchange.getRequest().getMethodValue();
        String requestUri = exchange.getRequest().getURI().getRawPath();
        requestLogArgs.add(traceId);
        requestLogArgs.add(requestMethod);
        requestLogArgs.add(requestUri);

        // Print request headers.
        headers.forEach((headerName, headerValue) -> {
            requestLog.append("{}: {}\n");
            requestLogArgs.add(headerName);
            requestLogArgs.add(headerValue.toString());
        });
        requestLog.append("---------- IAM Gateway Request Log End ----------\n");
        log.info(requestLog.toString(), requestLogArgs.toArray());

        exchange.getAttributes().put(START_TIME, FastTimeClock.currentTimeMillis());
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();

            Long startTime = exchange.getAttribute(START_TIME);
            long costTime = nonNull(startTime) ? (FastTimeClock.currentTimeMillis() - startTime) : 0L;

            StringBuilder responseLog = new StringBuilder(300);
            List<Object> responseLogArgs = new ArrayList<>(16);
            responseLog.append("\n\n---------- IAM Gateway Response Log Begin  ----------\n");

            // Print HTTP URI. (E.g: 997ac7d2-2056-419b-883b-6969aae77e3e :: 200
            // GET /example/foo/bar)
            responseLog.append("{} :: {} {} {} {}\n");
            responseLogArgs.add(traceId);
            responseLogArgs.add(response.getStatusCode().value());
            responseLogArgs.add(requestMethod);
            responseLogArgs.add(requestUri);
            responseLogArgs.add(costTime + "ms");

            // Print response headers.
            HttpHeaders httpHeaders = response.getHeaders();
            httpHeaders.forEach((headerName, headerValue) -> {
                responseLog.append("{}: {}\n");
                responseLogArgs.add(headerName);
                responseLogArgs.add(headerValue.toString());
            });
            responseLog.append("----------  IAM Gateway Response Log End  ----------\n");
            // Print cost time.
            log.info(responseLog.toString(), responseLogArgs.toArray());
        }));
    }

    private static final String START_TIME = "startTime";

}
