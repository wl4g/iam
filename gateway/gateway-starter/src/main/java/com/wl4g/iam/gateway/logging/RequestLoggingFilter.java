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

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.logging.config.LoggingProperties;
import com.wl4g.iam.gateway.trace.config.TraceProperties;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link RequestLoggingFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-02 v3.0.0
 * @since v3.0.0
 */
public class RequestLoggingFilter extends AbstractLoggingFilter {

    public RequestLoggingFilter(TraceProperties traceConfig, LoggingProperties loggingConfig) {
        super(traceConfig, loggingConfig);
    }

    @Override
    protected Mono<Void> doFilterInternal(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            HttpHeaders headers,
            String traceId,
            String requestMethod,
            String requestUri) {
        return logRequest(exchange, chain, headers, traceId, requestMethod, requestUri);
    }

    /**
     * Request logging filtering.
     * 
     * @param exchange
     * @param chain
     * @param traceId
     * @param requestMethod
     * @param requestUri
     * @return
     */
    private Mono<Void> logRequest(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            HttpHeaders headers,
            String traceId,
            String requestMethod,
            String requestUri) {
        boolean log1_2 = isLoglevelRange(1, 2);
        boolean log3_10 = isLoglevelRange(3, 10);
        boolean log5_10 = isLoglevelRange(5, 10);
        boolean log6_10 = isLoglevelRange(6, 10);
        boolean log8_10 = isLoglevelRange(8, 10);

        StringBuilder requestLog = new StringBuilder(300);
        List<Object> requestLogArgs = new ArrayList<>(16);
        if (log1_2) {
            requestLog.append("{} {}\n");
            requestLogArgs.add(requestMethod);
            requestLogArgs.add(requestUri);
        } else if (log3_10) {
            requestLog.append(LOG_REQUEST_BEGIN);
            // Print HTTP URI. (E.g: 997ac7d2-2056-419b-883b-6969aae77e3e ::
            // GET /example/foo/bar)
            requestLog.append("{} :: {} {}\n");
            requestLogArgs.add(traceId);
            requestLogArgs.add(requestMethod);
            requestLogArgs.add(requestUri);
        }
        // Print request headers.
        if (log5_10) {
            headers.forEach((headerName, headerValue) -> {
                if (log6_10 || LOG_GENERIC_HEADERS.stream().anyMatch(h -> containsIgnoreCase(h, headerName))) {
                    requestLog.append("{}: {}\n");
                    requestLogArgs.add(headerName);
                    requestLogArgs.add(headerValue.toString());
                }
            });
        }
        // When the request has no body, print the end flag directly.
        boolean logReqBody = hasBody(headers.getContentType());
        if (!logReqBody) {
            requestLog.append(LOG_REQUEST_END);
            log.info(requestLog.toString(), requestLogArgs.toArray());
        }

        // Print request body.
        // if (logReqBody) {
        // // [issue-see]:https://www.codercto.com/a/52970.html
        // //
        // //[issue-see]:https://blog.csdn.net/kk380446/article/details/119537443
        // // Note: In this way, only the first piece of data can be
        // // obtained when the data packet is too large.
        // exchange.getRequest().getBody().subscribe(dataBuffer -> {
        // if (log8_10) {
        // requestLog.append(LOG_REQUEST_BODY);
        // byte[] bytes = new byte[dataBuffer.readableByteCount()];
        // dataBuffer.read(bytes);
        // org.springframework.core.io.buffer.DataBufferUtils.release(dataBuffer);
        // requestLogArgs.add(new String(bytes,
        // java.nio.charset.StandardCharsets.UTF_8));
        // // if (log3_10) {
        // requestLog.append(LOG_REQUEST_END);
        // // }
        // log.info(requestLog.toString(), requestLogArgs.toArray());
        // }
        // });
        // }
        // return chain.filter(exchange);

        return decorateRequest(exchange, chain, body -> {
            // Print request body.
            if (logReqBody) {
                // Full print request body.
                if (log8_10) {
                    requestLog.append(LOG_REQUEST_BODY);
                    // if (log3_10) {
                    requestLog.append(LOG_REQUEST_END);
                    // }
                    requestLogArgs.add(body);
                    log.info(requestLog.toString(), requestLogArgs.toArray());
                }
            } else if (log3_10) {
                requestLog.append(LOG_REQUEST_END);
                log.info(requestLog.toString(), requestLogArgs.toArray());
            }
            return Mono.just(body);
        });
    }

    /**
     * Wraps the HTTP request for body edited. </br>
     * see: https://www.cnblogs.com/hyf-huangyongfei/p/12849406.html </br>
     * see: https://blog.csdn.net/kk380446/article/details/119537443 </br>
     * 
     * @param exchange
     * @param chain
     * @param transformer
     * @return
     * @see {@link org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory#apply()}
     */
    private Mono<Void> decorateRequest(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            Function<? super String, ? extends Mono<? extends String>> transformer) {

        Class<String> inClass = String.class;
        Class<String> outClass = String.class;
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        // ServerRequest serverRequest = new
        // org.springframework.cloud.gateway.support.DefaultServerRequest(exchange);
        Mono<String> modifiedBody = serverRequest.bodyToMono(inClass).flatMap(transformer);

        BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(modifiedBody, outClass);
        HttpHeaders newHeaders = new HttpHeaders();
        newHeaders.putAll(exchange.getRequest().getHeaders());
        newHeaders.remove(HttpHeaders.CONTENT_LENGTH);

        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, newHeaders);
        ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = newHeaders.getContentLength();
                HttpHeaders _newHeaders = new HttpHeaders();
                _newHeaders.putAll(newHeaders);
                if (contentLength > 0) {
                    _newHeaders.setContentLength(contentLength);
                } else {
                    _newHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }
                return _newHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };

        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                .then(Mono.defer(() -> chain.filter(exchange.mutate().request(decorator).build())))
                .onErrorResume((Function<Throwable, Mono<Void>>) ex -> Mono.error(ex));
    }

}
