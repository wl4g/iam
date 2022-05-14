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
package com.wl4g.iam.gateway.responsecache;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.valueOf;
import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.hash.Hashing;
import com.wl4g.iam.gateway.config.ReactiveByteArrayRedisTemplate;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsTag;
import com.wl4g.iam.gateway.responsecache.cache.EhCacheResponseCacheTests;
import com.wl4g.iam.gateway.responsecache.cache.RedisResponseCache;
import com.wl4g.iam.gateway.responsecache.cache.ResponseCache;
import com.wl4g.iam.gateway.responsecache.cache.SimpleResponseCache;
import com.wl4g.iam.gateway.responsecache.config.ResponseCacheProperties;
import com.wl4g.iam.gateway.responsecache.config.ResponseCacheProperties.CachedProperties;
import com.wl4g.iam.gateway.util.IamGatewayUtil;
import com.wl4g.iam.gateway.util.IamGatewayUtil.SafeFilterOrdered;
import com.wl4g.infra.common.bean.ConfigBeanUtils;
import com.wl4g.infra.core.utils.web.ReactiveRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link ResponseCacheFilterFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-11 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class ResponseCacheFilterFactory extends AbstractGatewayFilterFactory<ResponseCacheFilterFactory.Config> {

    private final ResponseCacheProperties responseCacheConfig;
    private final ReactiveByteArrayRedisTemplate redisTemplate;
    private final IamGatewayMetricsFacade metricsFacade;
    private final SpelRequestMatcher requestMatcher;
    private final ConcurrentMap<String, ResponseCache> responseCaches = new ConcurrentHashMap<>(4);

    public ResponseCacheFilterFactory(ResponseCacheProperties responseCacheConfig, ReactiveByteArrayRedisTemplate redisTemplate,
            IamGatewayMetricsFacade metricsFacade) {
        super(ResponseCacheFilterFactory.Config.class);
        this.responseCacheConfig = notNullOf(responseCacheConfig, "responseCacheConfig");
        this.redisTemplate = notNullOf(redisTemplate, "redisTemplate");
        this.metricsFacade = notNullOf(metricsFacade, "metricsFacade");
        // Build gray request matcher.
        this.requestMatcher = new SpelRequestMatcher(responseCacheConfig.getPreferMatchRuleDefinitions());
    }

    @Override
    public String name() {
        return BEAN_NAME;
    }

    private void applyDefaultToConfig(Config config) {
        try {
            ConfigBeanUtils.configureWithDefault(new ResponseCacheFilterFactory.Config(), config,
                    responseCacheConfig.getDefaultCache());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException("Unable apply defaults to cache filter config", e);
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        applyDefaultToConfig(config);
        return new RequestCacheGatewayFilter(config);
    }

    @SuppressWarnings("deprecation")
    public static String getRequestHashKey(Config config, ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        StringBuffer hashPlain = new StringBuffer(request.getMethod().name());
        Map<String, String> queryParams = request.getQueryParams().toSingleValueMap();
        queryParams.keySet().stream().sorted((k1, k2) -> k1.compareTo(k2)).forEach(
                key -> hashPlain.append(key).append("=").append(queryParams.get(key)).append("&"));
        String hashKey = Hashing.sha1().hashString(hashPlain.toString(), StandardCharsets.UTF_8).toString();
        log.debug("Hashed key '{}' from '{}'", hashKey, hashPlain);
        return hashKey;
    }

    public static class Config extends CachedProperties {
    }

    @AllArgsConstructor
    class RequestCacheGatewayFilter implements GatewayFilter, Ordered {
        private final Config config;

        @Override
        public int getOrder() {
            return SafeFilterOrdered.ORDER_REQUEST_CACHE;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            if (!isCachedRequest(exchange)) {
                if (log.isDebugEnabled()) {
                    log.debug("Not to meet the conditional rule to enable cached request. - headers: {}, queryParams: {}",
                            exchange.getRequest().getURI(), exchange.getRequest().getQueryParams());
                }
                return chain.filter(exchange);
            }

            // Add metrics of total.
            metricsFacade.counter(exchange, MetricsName.RESPONSE_CACHE_TOTAL, 1, MetricsTag.ROUTE_ID,
                    IamGatewayUtil.getRouteId(exchange));

            // Calculate the request unique hash key.
            String hashKey = getRequestHashKey(config, exchange);

            // Gets or create response cache by route.
            ResponseCache responseCache = obtainRouteResponseCache(exchange);

            // First get the response data from the cache.
            return responseCache.get(hashKey).defaultIfEmpty(new byte[0]).flatMap(cachedResponseBytes -> {
                // Response cached data.
                if (nonNull(cachedResponseBytes) && cachedResponseBytes.length > 0) {
                    // Add metrics of hits total.
                    metricsFacade.counter(exchange, MetricsName.RESPONSE_CACHE_HITS_TOTAL, 1, MetricsTag.ROUTE_ID,
                            IamGatewayUtil.getRouteId(exchange));
                    return responseWithCached(exchange, hashKey, cachedResponseBytes);
                }

                // Extract new response data to cache.
                ByteBuf respBuf = Unpooled.buffer(responseCacheConfig.getTmpBufferInitialCapacity(),
                        responseCacheConfig.getTmpBufferMaxCapacity());
                ServerHttpResponse newResponse = decorateResponse(exchange, chain, responseBodySegment -> {
                    if (respBuf.isWritable(responseBodySegment.length)) {
                        respBuf.writeBytes(responseBodySegment);
                    }
                    return Mono.just(responseBodySegment);
                });

                // The doFinally() method does not block the response to write
                // data to the remote client.
                return chain.filter(exchange.mutate().response(newResponse).build()).doFinally(signal -> {
                    try {
                        byte[] respBytes = new byte[respBuf.readableBytes()];
                        respBuf.readBytes(respBytes);
                        if (nonNull(respBytes) && respBytes.length > 0) {
                            // Use a subscribe() to prevent reactive
                            // RedisResponseCache from not completing.
                            responseCache.put(hashKey, respBytes).subscribe();
                            log.debug("Cached response body of hashKey: {}, uri: {}", hashKey, exchange.getRequest().getURI());
                        }
                    } finally {
                        ReferenceCountUtil.safeRelease(respBuf);
                    }
                });
            });
        }

        /**
         * Check if enable cached request needs to be filtered.
         * 
         * @param exchange
         * @return
         */
        private boolean isCachedRequest(ServerWebExchange exchange) {
            // Gets current request route.
            Route route = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

            // Add routeId temporary predicates.
            Map<String, Supplier<Predicate<String>>> routeIdPredicateSupplier = singletonMap(VAR_ROUTE_ID,
                    () -> Predicates.equalTo(route.getId()));

            return requestMatcher.matches(new ReactiveRequestExtractor(exchange.getRequest()),
                    responseCacheConfig.getPreferOpenMatchExpression(), routeIdPredicateSupplier);
        }

        /**
         * Obtain request cache by current routeId.
         * 
         * @param exchange
         * @return
         */
        private ResponseCache obtainRouteResponseCache(ServerWebExchange exchange) {
            String routeId = IamGatewayUtil.getRouteId(exchange);
            ResponseCache responseCache = responseCaches.get(routeId);
            if (isNull(responseCache)) {
                synchronized (this) {
                    responseCache = responseCaches.get(routeId);
                    if (isNull(responseCache)) {
                        switch (config.getProvider()) {
                        case SimpleCache:
                            // see:https://github.com/google/guava/wiki/CachesExplained#eviction
                            Cache<String, byte[]> localCache = newBuilder().maximumSize(config.getSimple().getMaximumSize())
                                    .expireAfterAccess(config.getSimple().getExpireAfterAccessMs(), MILLISECONDS)
                                    .expireAfterWrite(config.getSimple().getExpireAfterWriteMs(), MILLISECONDS)
                                    .concurrencyLevel(config.getSimple().getConcurrencyLevel())
                                    .build();
                            responseCache = new SimpleResponseCache(localCache);
                            break;
                        case EhCache:
                            responseCache = new EhCacheResponseCacheTests(config.getEhcache(), routeId);
                            break;
                        case RedisCache:
                            responseCache = new RedisResponseCache(config.getRedis(), redisTemplate);
                            break;
                        }
                        responseCaches.put(routeId, responseCache);
                    }
                }
            }
            return notNull(responseCache, "Cannot obtain request cache, Shouldn't be here!");
        }

        /**
         * The response object decorated as an editable response body to solve
         * the problem that the response body can only be read once.
         * 
         * @param exchange
         * @param chain
         * @param transformer
         * @return
         * @see {@link org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory#apply()}
         * @see https://www.cnblogs.com/hyf-huangyongfei/p/12849406.html
         * @see https://blog.csdn.net/kk380446/article/details/119537443
         */
        private ServerHttpResponse decorateResponse(
                ServerWebExchange exchange,
                GatewayFilterChain chain,
                Function<? super byte[], ? extends Mono<? extends byte[]>> transformer) {
            return new ServerHttpResponseDecorator(exchange.getResponse()) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) { // Mono<NettyDataBuffer>
                    // Tip: String type can also be used.
                    // Class<String> inClass = String.class;
                    // Class<String> outClass = String.class;
                    Class<byte[]> inClass = byte[].class;
                    Class<byte[]> outClass = byte[].class;

                    String responseContentType = exchange
                            .getAttribute(ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
                    HttpHeaders newHeaders = new HttpHeaders();
                    newHeaders.add(HttpHeaders.CONTENT_TYPE, responseContentType);

                    ClientResponse clientResponse = ClientResponse.create(exchange.getResponse().getStatusCode())
                            .headers(headers -> headers.putAll(newHeaders))
                            .body(Flux.from(body))
                            .build();

                    Mono<byte[]> modifiedBody = clientResponse.bodyToMono(inClass).flatMap(transformer);
                    BodyInserter<Mono<byte[]>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(modifiedBody,
                            outClass);

                    // [FIX]: If the order of this filter is set to be executed
                    // before, then the return header here is of type
                    // ReadOnlyHttpHeaders, but it will be abnormal when other
                    // filters need to modify the header. Is this due to the
                    // order
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
         * Respond directly to the last cached response bytes data.
         * 
         * @param exchange
         * @param hashKey
         * @param cachedResponseBytes
         * @return
         */
        private Mono<Void> responseWithCached(ServerWebExchange exchange, String hashKey, byte[] cachedResponseBytes) {
            // see:https://github.com/spring-cloud/spring-cloud-gateway/issues/268
            HttpHeaders headers = exchange.getResponse().getHeaders();
            headers.add(responseCacheConfig.getResponseCachedHeader(), hashKey);
            headers.add(HttpHeaders.CONTENT_LENGTH, valueOf(cachedResponseBytes.length));
            return exchange.getResponse().writeWith(Flux.just(exchange.getResponse().bufferFactory().wrap(cachedResponseBytes)));
        }

    }

    public static final String BEAN_NAME = "ResponseCache";
    public static final String VAR_ROUTE_ID = "routeId";

}
