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

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.logging.config.LoggingProperties;
import com.wl4g.iam.gateway.trace.config.GrayTraceProperties;
import com.wl4g.infra.common.lang.FastTimeClock;
import com.wl4g.infra.core.web.matcher.ReactiveRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link LoggingGlobalFilter}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-02 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    private final TraceProperties traceConfig;
    private final LoggingProperties loggingConfig;
    private final SpelRequestMatcher requestMatcher;

    public LoggingFilter(TraceProperties traceConfig, LoggingProperties loggingConfig) {
        this.traceConfig = notNullOf(traceConfig, "traceConfig");
        this.loggingConfig = notNullOf(loggingConfig, "loggingConfig");
        this.requestMatcher = new SpelRequestMatcher(loggingConfig.getMatchRuleDefinitions());
    }

    /**
     * @see {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#loadFilters()}
     */
    @Override
    public int getOrder() {
        return ORDER_FILTER;
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
        if (loggingConfig.getVerboseLevel() <= 0) {
            return chain.filter(exchange);
        }
        String traceId = headers.getFirst(traceConfig.getTraceIdRequestHeader());
        String requestMethod = request.getMethodValue();
        String requestUri = request.getURI().getRawPath();

        // TODO
        // return chain.filter(exchange.mutate()
        // .request(logRequest(exchange, chain, headers, traceId, requestMethod,
        // requestUri))
        // .response(logResponse(exchange, chain, headers, traceId,
        // requestMethod, requestUri))
        // .build());

        // TODO
        // Error ???
        //
        return logRequest(exchange, chain, headers, traceId, requestMethod, requestUri).then(Mono.defer(() -> chain.filter(
                exchange.mutate().response(logResponse(exchange, chain, headers, traceId, requestMethod, requestUri)).build())));

        // TODO
        // Can only choose one? ? ?
        //
        // return logRequest(exchange, chain, headers, traceId, requestMethod,
        // requestUri);
        //

        // return chain.filter(
        // exchange.mutate().response(logResponse(exchange, chain, headers,
        // traceId, requestMethod, requestUri)).build());
    }

    /**
     * Wraps mutated response logging filtering.
     * 
     * @param exchange
     * @param chain
     * @param traceId
     * @param requestMethod
     * @param requestUri
     * @return
     */
    private Mono<Void> /* ServerHttpRequest */ logRequest(
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
     * Wraps mutated response logging filtering.
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
        boolean log1_2 = isLoglevelRange(1, 2);
        boolean log3_10 = isLoglevelRange(3, 10);
        boolean log6_10 = isLoglevelRange(6, 10);
        boolean log8_10 = isLoglevelRange(8, 10);
        boolean log9_10 = isLoglevelRange(9, 10);

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
                if (log8_10 || LOG_GENERIC_HEADERS.stream().anyMatch(h -> containsIgnoreCase(h, headerName))) {
                    responseLog.append("{}: {}\n");
                    responseLogArgs.add(headerName);
                    responseLogArgs.add(headerValue.toString());
                }
            });
        }
        // When the response has no body, print the end flag directly.
        boolean isLogResBody = log9_10 && hasBody(response.getHeaders().getContentType());
        if (!isLogResBody) {
            responseLog.append(LOG_RESPONSE_END);
            log.info(responseLog.toString(), responseLogArgs.toArray());
        }

        return decorateResponse(exchange, chain, originalBody -> {
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
    private Mono<Void> /* ServerHttpRequest */ decorateRequest(
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

                // if (hasBody(getHeaders().getContentType())) {
                // return super.getBody().map(ds -> {
                // System.out.println(11111111);
                // System.out.println(java.nio.charset.StandardCharsets.UTF_8.decode(ds.asByteBuffer()));
                // System.out.println(222222);
                // return ds;
                // }).doFinally((s) -> System.out.println("end----111---"));
                // } else {
                // System.out.println("end----222---");
                // return super.getBody();
                // }

                // if (hasBody(getHeaders().getContentType())) {
                // return outputMessage.getBody().map(ds -> {
                // System.out.println(11111111);
                // System.out.println(java.nio.charset.StandardCharsets.UTF_8.decode(ds.asByteBuffer()));
                // System.out.println(222222);
                // return ds;
                // }).doFinally((s) -> System.out.println("end----111---"));
                // } else {
                // System.out.println("end----222---");
                // return outputMessage.getBody();
                // }
            }
        };

        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                .then(Mono.defer(() -> chain.filter(exchange.mutate().request(decorator).build())))
                .onErrorResume((Function<Throwable, Mono<Void>>) ex -> Mono.error(ex));

        // return decorator;
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

    /**
     * Check if filtering is required and logging logs.
     * 
     * @param request
     * @param headers
     * @return
     */
    private boolean isFilterLogging(ServerHttpRequest request, HttpHeaders headers) {
        // If the mandatory switch is not set, it is determined whether to
        // enable logging according to the preference switch, otherwise it is
        // determined whether to enable logging according to the mandatory
        // switch, the default mandatory switch is empty, the preference switch
        // is enabled.
        if (isNull(loggingConfig.getRequiredFlightLogEnabled())) {
            return requestMatcher.matches(new ReactiveRequestExtractor(request), loggingConfig.getMatchExpression());
        }
        return loggingConfig.getRequiredFlightLogEnabled();
    }

    /**
     * Check if the specified flight log level range is met.
     * 
     * @param lower
     * @param upper
     * @return
     */
    private boolean isLoglevelRange(int lower, int upper) {
        return loggingConfig.getVerboseLevel() >= lower && loggingConfig.getVerboseLevel() <= upper;
    }

    /**
     * Check if the media type of the request or response has a body.
     * 
     * @param mediaType
     * @return
     */
    private boolean hasBody(MediaType mediaType) {
        if (isNull(mediaType)) {
            return false;
        }
        for (MediaType media : HAS_BODY_MEDIA_TYPES) {
            if (media.isCompatibleWith(mediaType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logging for generic HTTP headers.
     */
    private static final List<String> LOG_GENERIC_HEADERS = unmodifiableList(new ArrayList<String>() {
        private static final long serialVersionUID = 1616772712967733180L;
        {
            // Standard
            add(HttpHeaders.CONTENT_TYPE);
            add(HttpHeaders.CONTENT_ENCODING);
            add(HttpHeaders.CONTENT_LENGTH);
            add(HttpHeaders.CONTENT_RANGE);
            add(HttpHeaders.CONTENT_DISPOSITION);
            add(HttpHeaders.CONNECTION);
            add(HttpHeaders.CACHE_CONTROL);
            add(HttpHeaders.COOKIE);
            add(HttpHeaders.ACCEPT);
            add(HttpHeaders.ACCEPT_ENCODING);
            add(HttpHeaders.ACCEPT_LANGUAGE);
            add(HttpHeaders.REFERER);
            add(HttpHeaders.USER_AGENT);
            add(HttpHeaders.LOCATION);
            add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
            add(HttpHeaders.SERVER);
            add(HttpHeaders.DATE);
            add(HttpHeaders.UPGRADE);
            // Extension
            add("Content-MD5");
            add("Upgrade-Insecure-Requests");
        }
    });

    /**
     * The content-type definition of the request or corresponding body needs to
     * be recorded.
     */
    private static final List<MediaType> HAS_BODY_MEDIA_TYPES = unmodifiableList(new ArrayList<MediaType>() {
        private static final long serialVersionUID = 1616772712967733180L;
        {
            add(APPLICATION_JSON);
            add(TEXT_HTML);
            add(TEXT_PLAIN);
            add(TEXT_MARKDOWN);
            add(APPLICATION_FORM_URLENCODED);
            add(APPLICATION_XML);
            add(APPLICATION_ATOM_XML);
            add(APPLICATION_PROBLEM_XML);
            add(APPLICATION_CBOR);
            add(APPLICATION_RSS_XML);
        }
    });

    public static final String LOG_REQUEST_BEGIN = "\n---------- <IAM Gateway Request Log Begin> ------------\n::: Headers :::\n";
    public static final String LOG_REQUEST_BODY = "::: Body :::\n{}";
    public static final String LOG_REQUEST_END = "\n---------- <IAM Gateway Request Log End> -------------\n";
    public static final String LOG_RESPONSE_BEGIN = "\n---------- <IAM Gateway Response Log Begin> ----------\n::: Headers :::\n";
    public static final String LOG_RESPONSE_BODY = "::: Body :::\n{}";
    public static final String LOG_RESPONSE_END = "\n---------- <IAM Gateway Response Log End> ------------\n";
    public static final String KEY_START_TIME = LoggingFilter.class.getName() + ".startTime";
    public static final int ORDER_FILTER = Ordered.HIGHEST_PRECEDENCE + 20;
}
