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

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.logging.config.DyeingLoggingProperties;
import com.wl4g.infra.common.lang.FastTimeClock;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link ResponseDyeingLoggingFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class ResponseDyeingLoggingFilter extends AbstractDyeingLoggingFilter {

    public ResponseDyeingLoggingFilter(DyeingLoggingProperties loggingConfig) {
        super(loggingConfig);
    }

    @Override
    protected Mono<Void> doFilterInternal(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            HttpHeaders headers,
            String traceId,
            String requestMethod,
            String requestUri) {
        return chain.filter(
                exchange.mutate().response(logResponse(exchange, chain, headers, traceId, requestMethod, requestUri)).build());
    }

    /**
     * LimitedResult logging filtering.
     * 
     * @param exchange
     * @param chain
     * @param traceId
     * @param requestMethod
     * @param requestUri
     * @return
     */
    private ServerHttpResponse logResponse(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            HttpHeaders headers,
            String traceId,
            String requestMethod,
            String requestUri) {
        boolean log1_2 = isLoglevelRange(exchange, 1, 2);
        boolean log3_10 = isLoglevelRange(exchange, 3, 10);
        boolean log6_10 = isLoglevelRange(exchange, 6, 10);
        boolean log8_10 = isLoglevelRange(exchange, 8, 10);
        boolean log9_10 = isLoglevelRange(exchange, 9, 10);

        // MDC.put("type", "RESPONSE-BODY");
        // log.info(String.format("[%s], %s", requestId,
        // originalBody));
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
            // Print HTTP URI. (E.g:
            // 997ac7d2-2056-419b-883b-6969aae77e3e ::
            // 200 GET /example/foo/bar)
            responseLog.append("{} {} {} :: {} {}\n");
            responseLogArgs.add(response.getStatusCode().value());
            responseLogArgs.add(requestMethod);
            responseLogArgs.add(requestUri);
            responseLogArgs.add(traceId);
            responseLogArgs.add(costTime + "ms");
        }
        // Print response headers.
        if (log6_10) {
            HttpHeaders httpHeaders = response.getHeaders();
            httpHeaders.forEach((headerName, headerValue) -> {
                if (log8_10 || LOG_GENERIC_HEADERS.stream().anyMatch(h -> containsIgnoreCase(h, headerName))) {
                    responseLog.append("{}: {}\n");
                    responseLogArgs.add(headerName);
                    responseLogArgs.add(headerValue.toString());
                }
            });
        }

        return decorateResponse(exchange, chain, originalBody -> {
            // When the response has no body, print the end flag directly.
            boolean isLogResBody = log9_10 && hasBody(response.getHeaders().getContentType());
            // Print response body.
            if (isLogResBody) {
                // Full print response body.
                responseLog.append(LOG_RESPONSE_BODY);
                responseLogArgs.add(originalBody);
            }
            if (log3_10) {
                responseLog.append(LOG_RESPONSE_END);
                log.info(responseLog.toString(), responseLogArgs.toArray());
            }
            return Mono.just(originalBody);
        });

    }

    /**
     * Wraps the HTTP response for body edited. </br>
     * 
     * see: https://www.cnblogs.com/hyf-huangyongfei/p/12849406.html </br>
     * see: https://blog.csdn.net/kk380446/article/details/119537443 </br>
     * 
     * @param exchange
     * @param chain
     * @param transformer
     * @return
     * @see {@link org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory#apply()}
     */
    private ServerHttpResponse decorateResponse(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            Function<? super String, ? extends Mono<? extends String>> transformer) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) { // Mono<NettyDataBuffer>
                Class<String> inClass = String.class;
                Class<String> outClass = String.class;

                String responseContentType = exchange.getAttribute(ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
                HttpHeaders newHeaders = new HttpHeaders();
                newHeaders.add(HttpHeaders.CONTENT_TYPE, responseContentType);

                ClientResponse clientResponse = ClientResponse.create(exchange.getResponse().getStatusCode())
                        .headers(headers -> headers.putAll(newHeaders))
                        .body(Flux.from(body))
                        .build();

                Mono<String> modifiedBody = clientResponse.bodyToMono(inClass).flatMap(transformer);
                BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(modifiedBody,
                        outClass);

                // [FIX]: If the order of this filter is set to be executed
                // before, then the return header here is of type
                // ReadOnlyHttpHeaders, but it will be abnormal when other
                // filters need to modify the header. Is this due to the order
                // of the filters ???
                HttpHeaders editableHeaders = new HttpHeaders(new LinkedMultiValueMap<>(exchange.getResponse().getHeaders()));
                CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, editableHeaders);

                return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
                    Flux<DataBuffer> messageBody = outputMessage.getBody();
                    HttpHeaders headers = getDelegate().getHeaders();
                    if (!headers.containsKey(HttpHeaders.TRANSFER_ENCODING)) {
                        messageBody = messageBody.doOnNext(data -> headers.setContentLength(data.readableByteCount()));
                    }
                    return getDelegate().writeWith(messageBody);
                }));
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(p -> p));
            }
        };
    }

}
