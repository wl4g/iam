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

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.springframework.http.MediaType.APPLICATION_ATOM_XML;
import static org.springframework.http.MediaType.APPLICATION_CBOR;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_XML;
import static org.springframework.http.MediaType.APPLICATION_RSS_XML;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.MediaType.TEXT_MARKDOWN;
import static org.springframework.http.MediaType.TEXT_PLAIN;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
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
@SuppressWarnings("deprecation")
@Slf4j
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> logGenericHttpHeaders = unmodifiableList(new ArrayList<String>() {
        private static final long serialVersionUID = 1616772712967733180L;
        {
            add("content-type");
            add("cookie");
            add("accept");
            add("accept-encoding");
            add("accept-language");
            add("referer");
            add("user-agent");
            add("access-control-allow-origin");
            add("connection");
            add("upgrade-insecure-requests");
            add("content-md5");
            add("content-encoding");
            add("cache-control:");
            add("location");
            add("server");
            add("date");
        }
    });

    private @Autowired TraceProperties traceConfig;
    private @Autowired LoggingProperties loggingConfig;

    @Override
    public int getOrder() {
        return LOGGING_FILTER_ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(KEY_START_TIME, FastTimeClock.currentTimeMillis());
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        // Check if filtering flight logging is enabled.
        if (!isFilterLogging(request, headers)) {
            return chain.filter(exchange);
        }
        // Flight log level is disabled.
        if (loggingConfig.getPreferredFlightLogPrintVerboseLevel() <= 0) {
            return chain.filter(exchange);
        }

        String traceId = headers.getFirst(traceConfig.getTraceIdRequestHeader());
        String requestMethod = request.getMethodValue();
        String requestUri = request.getURI().getRawPath();

        boolean log1_2 = isFlightLoglevelRange(1, 2);
        boolean log3_10 = isFlightLoglevelRange(3, 10);
        boolean log5_10 = isFlightLoglevelRange(5, 10);
        boolean log7_8 = isFlightLoglevelRange(7, 8);
        boolean log9_10 = isFlightLoglevelRange(9, 10);
        StringBuilder requestLog = new StringBuilder(300);
        List<Object> requestLogArgs = new ArrayList<>(16);
        if (log1_2) {
            requestLog.append("{} {}\n");
            requestLogArgs.add(requestMethod);
            requestLogArgs.add(requestUri);
        } else if (log3_10) {
            requestLog.append(LOG_REQUEST_BEGIN);
            // Print HTTP URI. (E.g: 997ac7d2-2056-419b-883b-6969aae77e3e :: GET
            // /example/foo/bar)
            requestLog.append("{} :: {} {}\n");
            requestLogArgs.add(traceId);
            requestLogArgs.add(requestMethod);
            requestLogArgs.add(requestUri);
        }
        // Print request headers.
        if (log5_10) {
            headers.forEach((headerName, headerValue) -> {
                if (isFlightLoglevelRange(6, 10) || logGenericHttpHeaders.contains(headerName.toLowerCase())) {
                    requestLog.append("{}: {}\n");
                    requestLogArgs.add(headerName);
                    requestLogArgs.add(headerValue.toString());
                }
            });
        }
        // Print request body.
        if (isPrintBody(request.getHeaders().getContentType())) {
            // Print only the first part of the request body data.
            if (log7_8) {
                // Note: In this way, only the first piece of data can be
                // obtained when the data packet is too large.
                // issue:https://www.cnblogs.com/hyf-huangyongfei/p/12849406.html
                request.getBody().subscribe(dataBuffer -> {
                    requestLog.append(LOG_REQUEST_BODY);
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    requestLogArgs.add(new String(bytes, StandardCharsets.UTF_8));
                    // if (log3_10) {
                    requestLog.append(LOG_REQUEST_END);
                    // }
                    log.info(requestLog.toString(), requestLogArgs.toArray());
                });
            }
            // Full print request body.
            else if (log9_10) {
                ServerRequest serverRequest = new DefaultServerRequest(exchange);
                return serverRequest.bodyToMono(String.class).flatMap(body -> {
                    requestLog.append(LOG_REQUEST_BODY);
                    requestLogArgs.add(body);
                    // if (log3_10) {
                    requestLog.append(LOG_REQUEST_END);
                    // }
                    log.info(requestLog.toString(), requestLogArgs.toArray());
                    return doChainResponse(exchange, chain, traceId, requestMethod, requestUri);
                });
            }
        } else if (log3_10) {
            requestLog.append(LOG_REQUEST_END);
            log.info(requestLog.toString(), requestLogArgs.toArray());
        }

        return doChainResponse(exchange, chain, traceId, requestMethod, requestUri);
    }

    private Mono<Void> doChainResponse(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            String traceId,
            String requestMethod,
            String requestUri) {

        boolean log1_2 = isFlightLoglevelRange(1, 2);
        boolean log3_10 = isFlightLoglevelRange(3, 10);
        boolean log6_10 = isFlightLoglevelRange(6, 10);
        boolean log8_9 = isFlightLoglevelRange(8, 9);
        boolean log10_10 = isFlightLoglevelRange(10, 10);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            Long startTime = exchange.getAttribute(KEY_START_TIME);
            long costTime = nonNull(startTime) ? (FastTimeClock.currentTimeMillis() - startTime) : 0L;

            StringBuilder responseLog = new StringBuilder(300);
            List<Object> responseLogArgs = new ArrayList<>(16);
            if (log1_2) {
                responseLog.append("{} {} {} {}\n");
                responseLogArgs.add(response.getStatusCode().value());
                responseLogArgs.add(requestMethod);
                responseLogArgs.add(requestUri);
                responseLogArgs.add(costTime + "ms");
            } else if (log3_10) {
                responseLog.append(LOG_RESPONSE_BEGIN);
                // Print HTTP URI. (E.g: 997ac7d2-2056-419b-883b-6969aae77e3e ::
                // 200 GET /example/foo/bar)
                responseLog.append("{} :: {} {} {} {}\n");
                responseLogArgs.add(traceId);
                responseLogArgs.add(response.getStatusCode().value());
                responseLogArgs.add(requestMethod);
                responseLogArgs.add(requestUri);
                responseLogArgs.add(costTime + "ms");
            }
            // Print response headers.
            if (log6_10) {
                HttpHeaders httpHeaders = response.getHeaders();
                httpHeaders.forEach((headerName, headerValue) -> {
                    if (isFlightLoglevelRange(7, 10) || logGenericHttpHeaders.contains(headerName.toLowerCase())) {
                        responseLog.append("{}: {}\n");
                        responseLogArgs.add(headerName);
                        responseLogArgs.add(headerValue.toString());
                    }
                });
            }
            // Print response body.
            if (isPrintBody(response.getHeaders().getContentType())) {
                if (log8_9) { // part-print-request-body
                    responseLog.append(LOG_RESPONSE_BODY);
                    // TODO
                    responseLogArgs.add("");
                } else if (log10_10) { // full-print-response-body
                    responseLog.append(LOG_RESPONSE_BODY);
                    // TODO
                    responseLogArgs.add("");
                }
            }
            if (log3_10) {
                responseLog.append(LOG_RESPONSE_END);
            }
            // Print cost time.
            log.info(responseLog.toString(), responseLogArgs.toArray());
        }));
    }

    private boolean isFilterLogging(ServerHttpRequest request, HttpHeaders headers) {
        // If the mandatory switch is not set, it is determined whether to
        // enable logging according to the preference switch, otherwise it is
        // determined whether to enable logging according to the mandatory
        // switch, the default mandatory switch is empty, the preference switch
        // is enabled.
        if (isNull(loggingConfig.getRequiredFlightLogPrintEnabled())) {
            String value = headers.getFirst(loggingConfig.getPreferredFlightLogEnableHeaderName());
            if (!isBlank(loggingConfig.getPreferredFlightLogEnableFallbackQueryName())) {
                value = request.getQueryParams().getFirst(loggingConfig.getPreferredFlightLogEnableFallbackQueryName());
            }
            return !isBlank(loggingConfig.getPreferredFlightLogEnableMatchesValue())
                    && equalsIgnoreCase(trimToEmpty(value), loggingConfig.getPreferredFlightLogEnableMatchesValue());
        }
        return loggingConfig.getRequiredFlightLogPrintEnabled();
    }

    private boolean isFlightLoglevelRange(int lower, int upper) {
        return loggingConfig.getPreferredFlightLogPrintVerboseLevel() >= lower
                && loggingConfig.getPreferredFlightLogPrintVerboseLevel() <= upper;
    }

    private boolean isPrintBody(MediaType mediaType) {
        if (isNull(mediaType)) {
            return false;
        }
        return APPLICATION_JSON.isCompatibleWith(mediaType) || APPLICATION_XML.isCompatibleWith(mediaType)
                || APPLICATION_ATOM_XML.isCompatibleWith(mediaType) || APPLICATION_PROBLEM_XML.isCompatibleWith(mediaType)
                || APPLICATION_CBOR.isCompatibleWith(mediaType) || APPLICATION_RSS_XML.isCompatibleWith(mediaType)
                || TEXT_HTML.isCompatibleWith(mediaType) || TEXT_MARKDOWN.isCompatibleWith(mediaType)
                || TEXT_PLAIN.isCompatibleWith(mediaType) || APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType);
    }

    public static final int LOGGING_FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE + 20;
    public static final String KEY_START_TIME = LoggingGlobalFilter.class.getName() + ".startTime";

    public static final String LOG_REQUEST_BEGIN = "\n---------- <IAM Gateway Request Log Begin> ------------\n::: Headers :::\n";
    public static final String LOG_REQUEST_BODY = "::: Body :::\n{}";
    public static final String LOG_REQUEST_END = "\n---------- <IAM Gateway Request Log End> -------------\n";
    public static final String LOG_RESPONSE_BEGIN = "\n---------- <IAM Gateway Response Log Begin> ----------\n::: Headers :::\n";
    public static final String LOG_RESPONSE_BODY = "::: Body :::\n{}";
    public static final String LOG_RESPONSE_END = "\n---------- <IAM Gateway Response Log End> ------------\n";
}
