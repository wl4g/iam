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

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.security.sign.SimpleSignAuthingFilterFactory;
import com.wl4g.iam.gateway.security.sign.SimpleSignAuthingFilterFactory.AppIdExtractor;
import com.wl4g.iam.gateway.security.sign.SimpleSignAuthingFilterFactory.SignAlgorithm;
import com.wl4g.iam.gateway.security.sign.SimpleSignAuthingFilterFactory.SignHashingMode;
import com.wl4g.iam.gateway.logging.config.LoggingProperties;
import com.wl4g.iam.gateway.trace.config.GrayTraceProperties;
import com.wl4g.infra.common.lang.FastTimeClock;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link LoggingFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-02 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> implements Ordered {
    private static final Set<String> LOGGABLE_CONTENT_TYPES = new HashSet<>(
            Arrays.asList(MediaType.APPLICATION_JSON_VALUE.toLowerCase(), MediaType.APPLICATION_JSON_UTF8_VALUE.toLowerCase(),
                    MediaType.TEXT_PLAIN_VALUE, MediaType.TEXT_XML_VALUE));

    // private @Autowired TraceProperties traceConfig;
    // private @Autowired LoggingProperties loggingConfig;

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public int getOrder() {
        return LOGGING_FILTER_ORDER;
    }

    @Override
    public String name() {
        return LOGGING_FILTER;
    }

    @Override
    public GatewayFilter apply(LoggingFilter.Config config) {
        return (exchange, chain) -> {
            // First we get here request in exchange
            ServerHttpRequest requestMutated = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public Flux<DataBuffer> getBody() {
                    Logger requestLogger = new Logger(getDelegate());
                    if (LOGGABLE_CONTENT_TYPES.contains(String.valueOf(getHeaders().getContentType()).toLowerCase())) {
                        return super.getBody().map(ds -> {
                            requestLogger.appendBody(ds.asByteBuffer());
                            return ds;
                        }).doFinally((s) -> requestLogger.log());
                    } else {
                        requestLogger.log();
                        return super.getBody();
                    }
                }
            };
            ServerHttpResponse responseMutated = new ServerHttpResponseDecorator(exchange.getResponse()) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    Logger responseLogger = new Logger(getDelegate());
                    if (LOGGABLE_CONTENT_TYPES.contains(String.valueOf(getHeaders().getContentType()).toLowerCase())) {
                        return join(body).flatMap(db -> {
                            responseLogger.appendBody(db.asByteBuffer());
                            responseLogger.log();
                            return getDelegate().writeWith(Mono.just(db));
                        });
                    } else {
                        responseLogger.log();
                        return getDelegate().writeWith(body);
                    }
                }
            };
            return chain.filter(exchange.mutate().request(requestMutated).response(responseMutated).build());
        };
    }

    private Mono<? extends DataBuffer> join(Publisher<? extends DataBuffer> dataBuffers) {
        notNullOf(dataBuffers, "dataBuffers");
        return Flux.from(dataBuffers)
                .collectList()
                .filter((list) -> !list.isEmpty())
                .map((list) -> list.get(0).factory().join(list))
                .doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release);
    }

    private static class Logger {
        private StringBuilder sb = new StringBuilder();

        Logger(ServerHttpResponse response) {
            sb.append("\n");
            sb.append("---- Response -----").append("\n");
            sb.append("Headers      :").append(response.getHeaders().toSingleValueMap()).append("\n");
            sb.append("Status code  :").append(response.getStatusCode()).append("\n");
        }

        Logger(ServerHttpRequest request) {
            sb.append("\n");
            sb.append("---- Request -----").append("\n");
            sb.append("Headers      :").append(request.getHeaders().toSingleValueMap()).append("\n");
            sb.append("Method       :").append(request.getMethod()).append("\n");
            sb.append("Client       :").append(request.getRemoteAddress()).append("\n");
        }

        void appendBody(ByteBuffer byteBuffer) {
            sb.append("Body         :").append(StandardCharsets.UTF_8.decode(byteBuffer)).append("\n");
        }

        void log() {
            sb.append("-------------------").append("\n");
            log.info(sb.toString());
        }

    }

    @Getter
    @Setter
    @ToString
    public static class Config {
    }

    public static final int LOGGING_FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE + 20;
    public static final String LOGGING_FILTER = "Logging";
    public static final String KEY_START_TIME = LoggingFilter.class.getName() + ".startTime";

    public static final String LOG_REQUEST_BEGIN = "\n---------- <IAM Gateway Request Log Begin> ------------\n::: Headers :::\n";
    public static final String LOG_REQUEST_BODY = "::: Body :::\n{}";
    public static final String LOG_REQUEST_END = "\n---------- <IAM Gateway Request Log End> -------------\n";
    public static final String LOG_RESPONSE_BEGIN = "\n---------- <IAM Gateway Response Log Begin> ----------\n::: Headers :::\n";
    public static final String LOG_RESPONSE_BODY = "::: Body :::\n{}";
    public static final String LOG_RESPONSE_END = "\n---------- <IAM Gateway Response Log End> ------------\n";

}
