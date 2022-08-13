/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
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

import static com.google.common.base.Charsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.logging.config.LoggingProperties;
import com.wl4g.iam.gateway.logging.model.LogRecord;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link RequestDyeingLoggingFilter}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class RequestDyeingLoggingFilter extends AbstractDyeingLoggingFilter {

    public RequestDyeingLoggingFilter(DyeingLoggingProperties loggingConfig) {
        super(loggingConfig);
    }

    @Override
    protected Mono<Void> doFilterInternal(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            HttpHeaders headers,
            String traceId,
            String requestMethod) {
        return logRequest(exchange, chain, headers, traceId, requestMethod);
    }

    /**
     * Request logging filtering.
     * see:https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-cacherequestbody-gatewayfilter-factory
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
            String requestMethod) {

        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String requestPath = uri.getPath();

        // ADD log record.
        LogRecord record = obtainLogRecord(exchange);

        boolean log1_2 = isLoglevelRange(exchange, 1, 2);
        boolean log3_10 = isLoglevelRange(exchange, 3, 10);
        boolean log5_10 = isLoglevelRange(exchange, 5, 10);
        boolean log6_10 = isLoglevelRange(exchange, 6, 10);
        boolean log8_10 = isLoglevelRange(exchange, 8, 10);

        StringBuilder requestLog = new StringBuilder(300);
        List<Object> requestLogArgs = new ArrayList<>(16);
        if (log1_2) {
            // requestLog.append("{} {}");
            // requestLog.append(LINE_SEPARATOR);
            // requestLogArgs.add(requestMethod);
            // requestLogArgs.add(requestPath);
            // TODO
            record.withRequestScheme(uri.getScheme()).withRequestMethod(request.getMethodValue()).withRequestPath(uri.getPath());
        } else if (log3_10) {
            requestLog.append(LOG_REQUEST_BEGIN);
            // Print HTTP URI. (E.g: 997ac7d2-2056-419b-883b-6969aae77e3e ::
            // GET /example/foo/bar)
            // requestLog.append("{} {} :: {}");
            // requestLog.append(LINE_SEPARATOR);
            // requestLogArgs.add(requestMethod);
            // requestLogArgs.add(requestPath.concat("?").concat(trimToEmpty(uri.getQuery())));
            // requestLogArgs.add(traceId);
            // TODO
            record.withRequestScheme(uri.getScheme())
                    .withRequestMethod(request.getMethodValue())
                    .withRequestPath(uri.getPath())
                    .withRequestQuery(request.getQueryParams().toSingleValueMap());
        }
        // Print request headers.
        if (log5_10) {
            headers.forEach((headerName, headerValue) -> {
                if (log6_10 || LOG_GENERIC_HEADERS.stream().anyMatch(h -> containsIgnoreCase(h, headerName))) {
                    // requestLog.append(LINE_SEPARATOR);
                    // requestLog.append("{}: {}");
                    // requestLogArgs.add(headerName);
                    // requestLogArgs.add(headerValue.toString());
                    // TODO
                    record.getRequestHeaders().put(headerName, headerValue.toString());
                }
            });
        }
        // When the request has no body, print the end flag directly.
        boolean processBodyIfNeed = isCompatibleWithPlainBody(headers.getContentType());
        if (!processBodyIfNeed) {
            // If it is a file upload, direct printing does not display binary.
            if (isUploadStreamMedia(headers.getContentType())) {
                processBodyIfNeed = false;
                // requestLog.append(LOG_REQUEST_BODY);
                // requestLog.append(LOG_REQUEST_END);
                // requestLogArgs.add("[Upload Binary Data] ...");
                // TODO
                record.setRequestBody("[Upload Binary Data] ...");
                // log.info(requestLog.toString(), requestLogArgs.toArray());
                // TODO
                log.info(record.toString());
            } else {
                // requestLog.append(LOG_REQUEST_END);
                // log.info(requestLog.toString(), requestLogArgs.toArray());
                // TODO
                log.info(record.toString());
            }
        }
        final boolean _processBodyIfNeed = processBodyIfNeed;

        // // Print request body.
        // // [problem]:https://www.codercto.com/a/52970.html
        // // [problem]:https://blog.csdn.net/kk380446/article/details/119537443
        // if (logReqBody) {
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

        // Note: The following transform function may be executed multiple
        // times. For the time being, we only print the first segment of data in
        // the request body. We think that printing too much data may be
        // meaningless and waste resources.
        AtomicInteger transformCount = new AtomicInteger(0);
        return decorateRequest(exchange, chain, requestBodySegment -> {
            if (transformCount.incrementAndGet() <= 1) {
                // Add request body.
                if (_processBodyIfNeed) {
                    if (log8_10) {
                        // requestLog.append(LOG_REQUEST_BODY);
                        // requestLog.append(LOG_REQUEST_END);
                        // Note: Only get the first small part of the data of
                        // the request body, which has prevented the amount of
                        // data from being too large.
                        int length = Math.min(requestBodySegment.length, loggingConfig.getMaxPrintRequestBodyLength());
                        String requestBodySegmentString = new String(requestBodySegment, 0, length, UTF_8);
                        // requestLogArgs.add(requestBodySegmentString);
                        // log.info(requestLog.toString(),requestLogArgs.toArray());
                        // TODO
                        record.setRequestBody(requestBodySegmentString);
                        log.info(record.toString());
                    } else if (log3_10) {
                        // requestLog.append(LOG_REQUEST_END);
                        // log.info(requestLog.toString(),
                        // requestLogArgs.toArray());
                        // TODO
                        log.info(record.toString());
                    }
                }
            }
            return Mono.just(requestBodySegment);
        });
    }

    /**
     * The request object decorated as an editable request body to solve the
     * problem that the request body can only be read once.
     * 
     * @param exchange
     * @param chain
     * @param transformer
     * @return
     * @see {@link org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory#apply()}
     * @see https://blog.csdn.net/kk380446/article/details/119537443
     * @see https://www.cnblogs.com/hyf-huangyongfei/p/12849406.html
     */
    private Mono<Void> decorateRequest(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            Function<? super byte[], ? extends Mono<? extends byte[]>> transformer) {

        // Tip: String type can also be used.
        // Class<String> inClass = String.class;
        // Class<String> outClass = String.class;
        Class<byte[]> inClass = byte[].class;
        Class<byte[]> outClass = byte[].class;

        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        // ServerRequest serverRequest = new
        // org.springframework.cloud.gateway.support.DefaultServerRequest(exchange);
        Mono<byte[]> modifiedBody = serverRequest.bodyToMono(inClass).flatMap(transformer);

        BodyInserter<Mono<byte[]>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(modifiedBody, outClass);
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
