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
package com.wl4g.iam.gateway.security.sign;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.security.MessageDigest.isEqual;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static reactor.core.publisher.Flux.just;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.validation.constraints.NotNull;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.cache.Cache;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsTag;
import com.wl4g.iam.gateway.security.config.IamSecurityProperties;
import com.wl4g.iam.gateway.security.config.IamSecurityProperties.SecretStore;
import com.wl4g.iam.gateway.security.sign.event.SimpleSignAuthingFailureEvent;
import com.wl4g.iam.gateway.security.sign.event.SimpleSignAuthingSuccessEvent;
import com.wl4g.iam.gateway.util.IamGatewayUtil.SafeFilterOrdered;
import com.wl4g.iam.gateway.util.bloom.RedisBloomFilter;
import com.wl4g.iam.gateway.util.bloom.RedisBloomFilter.BloomConfig;
import com.wl4g.infra.common.eventbus.EventBusSupport;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.runtime.JvmRuntimeTool;
import com.wl4g.infra.common.web.rest.RespBase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link SimpleSignAuthingFilterFactory}
 * 
 * <p>
 * Comparison of global filter and gateway filter: </br>
 * Speaking of their connection, we know that whether it is a global filter or a
 * gateway filter, they can form a filter chain for interception, and this
 * filter chain is composed of a List<GatewayFilter> collection, which seems to
 * be a combination of GatewayFilters, Has nothing to do with GlobalFilter. In
 * fact, SCG adapts GlobalFilter to GatewayFilter by means of an adapter. We can
 * see this change in the constructor of
 * {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#handle(ServerWebExchange)}.
 * </p>
 * 
 * <p>
 * The simple signature filter should be executed before the rate limiting
 * filter because rate limiting needs to be done based on the authentication
 * subject. Also note: all GatewayFilters do not need to implement the Ordered
 * interface, because the chain ordering is determined according to the
 * configuration order of routes.filters. see:
 * {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#handle(ServerWebExchange)}
 * and
 * {@link org.springframework.cloud.gateway.handler.FilteringWebHandler.DefaultGatewayFilterChain#filter(ServerWebExchange)}
 * and
 * {@link org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory}
 * </p>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-01 v3.0.0
 * @since v3.0.0
 */
public class SimpleSignAuthingFilterFactory extends AbstractGatewayFilterFactory<SimpleSignAuthingFilterFactory.Config> {

    private final SmartLogger log = getLogger(getClass());
    private final IamSecurityProperties authingConfig;
    private final StringRedisTemplate redisTemplate;
    private final Cache<String, String> secretCacheStore;
    private final IamGatewayMetricsFacade metricsFacade;
    private final EventBusSupport eventBus;
    private final Map<String, RedisBloomFilter<String>> cachedBloomFilters = new ConcurrentHashMap<>(8);

    public SimpleSignAuthingFilterFactory(@NotNull IamSecurityProperties authingConfig,
            @NotNull StringRedisTemplate redisTemplate, @NotNull IamGatewayMetricsFacade metricsFacade,
            EventBusSupport eventBus) {
        super(SimpleSignAuthingFilterFactory.Config.class);
        this.authingConfig = notNullOf(authingConfig, "authingConfig");
        this.redisTemplate = notNullOf(redisTemplate, "redisTemplate");
        this.metricsFacade = notNullOf(metricsFacade, "metricsFacade");
        this.eventBus = notNullOf(eventBus, "eventBus");
        this.secretCacheStore = newBuilder().expireAfterWrite(authingConfig.getSimpleSign().getSecretLocalCacheSeconds(), SECONDS)
                .build();
    }

    @Override
    public String name() {
        return NAME_SIMPLE_SIGN_FILTER;
    }

    /**
     * For the source code of the gateway filter chain implementation, see to:
     * {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#handle(ServerWebExchange)}
     * 
     * {@link org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator.getRoutes()}
     * 
     * Note: All requests will be filtered if
     * {@link org.springframework.cloud.gateway.filter.GlobalFilter} is
     * implemented. </br>
     * for example:
     * 
     * <pre>
     * storedAppSecret=5aUpyX5X7wzC8iLgFNJuxqj3xJdNQw8yS
     * curl http://wl4g.debug:14085/openapi/v2/test?appId=oi554a94bc416e4edd9ff963ed0e9e25e6c10545&nonce=0L9GyULPfwsD3Swg&timestamp=1599637679878&signature=5ac8747ccc2b1b332e8445b496d0c38529b38fba2c1b8ca8490cbf2932e06943
     * </pre>
     * 
     * Filters are looked up on every request,
     * see:{@link org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory#apply()}
     */
    @Override
    public GatewayFilter apply(SimpleSignAuthingFilterFactory.Config config) {
        return new SimpleSignAuthingGatewayFilter(config);
    }

    private RedisBloomFilter<String> obtainBloomFilter(ServerWebExchange exchange, SimpleSignAuthingFilterFactory.Config config) {
        String routeId = ((Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR)).getId();
        if (isBlank(routeId)) {
            throw new Error(format("Should't be here, cannot to get routeId"));
        }
        RedisBloomFilter<String> bloomFilter = cachedBloomFilters.get(routeId);
        if (isNull(bloomFilter)) {
            synchronized (this) {
                if (isNull(bloomFilter = cachedBloomFilters.get(routeId))) {
                    // Initial bloom filter.
                    bloomFilter = new RedisBloomFilter<String>(redisTemplate, new BloomConfig<>(
                            (Funnel<String>) (from, into) -> into.putString(from, UTF_8), Integer.MAX_VALUE, 0.01));
                    bloomFilter.bloomExpire(getBloomKey(exchange), config.getSignReplayVerifyBloomExpireSeconds());
                    cachedBloomFilters.put(routeId, bloomFilter);
                }
            }
        }
        return bloomFilter;
    }

    private String getBloomKey(ServerWebExchange exchange) {
        String routeId = ((Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR)).getId();
        if (isBlank(routeId)) {
            throw new Error(format("Should't be here, cannot to get routeId"));
        }
        return authingConfig.getSimpleSign().getSignReplayVerifyBloomLoadPrefix().concat(":").concat(routeId);
    }

    private byte[] doSignature(SimpleSignAuthingFilterFactory.Config config, ServerWebExchange exchange, String appId) {
        // Load stored secret.
        byte[] storedAppSecret = loadStoredSecret(config, appId);

        long beginTime = nanoTime();
        try {
            // Make signature plain text.
            byte[] signPlainBytes = config.getSignHashingMode().getFunction().apply(
                    new Object[] { config, storedAppSecret, exchange.getRequest() });
            // Hashing signature.
            return config.getSignAlgorithm().getFunction().apply(new byte[][] { storedAppSecret, signPlainBytes });
        } finally {
            // Add time metrics.
            addTimerMetrics(exchange, MetricsName.SIMPLE_SIGN_TIME, config, beginTime);
        }
    }

    private byte[] loadStoredSecret(SimpleSignAuthingFilterFactory.Config config, String appId) {
        String loadKey = authingConfig.getSimpleSign().getSecretStorePrefix().concat(":").concat(appId);
        switch (authingConfig.getSimpleSign().getSecretStore()) {
        case ENV:
            String storedSecret = System.getenv(loadKey);
            // Downgrade acquisition, for example, during integration testing,
            // process environment variables cannot be modified.
            storedSecret = isBlank(storedSecret) ? System.getProperty(loadKey) : null;
            if (isBlank(storedSecret)) {
                log.warn("No found client secret from {} via '{}'", SecretStore.ENV, loadKey);
                throw new IllegalArgumentException(format("No enables client secret?"));
            }
            return storedSecret.getBytes(UTF_8);
        case REDIS:
            storedSecret = secretCacheStore.asMap().get(loadKey);
            if (isBlank(storedSecret)) {
                synchronized (loadKey) {
                    storedSecret = secretCacheStore.asMap().get(loadKey);
                    if (isBlank(storedSecret)) {
                        storedSecret = redisTemplate.opsForValue().get(loadKey);
                        if (isBlank(storedSecret)) {
                            log.warn("No found client secret from {} via '{}'", SecretStore.REDIS, loadKey);
                            throw new IllegalArgumentException(format("No enables client secret?"));
                        }
                        secretCacheStore.asMap().put(loadKey, storedSecret);
                        return storedSecret.getBytes(UTF_8);
                    }
                }
            }
            return storedSecret.getBytes(UTF_8);
        default:
            throw new Error("Shouldn't be here");
        }
    }

    private String getRequestAppId(SimpleSignAuthingFilterFactory.Config config, ServerWebExchange exchange) {
        // Note: In some special business platform
        // scenarios, the signature authentication protocol may not define
        // appId (such as Alibaba Cloud Market SaaS product authentication
        // API), then the uniqueness of the client application can only be
        // determined according to the request route ID.
        return config.getAppIdExtractor().getFunction().apply(new Object[] { config, exchange });
    }

    private Mono<Void> writeResponse(HttpStatus status, ServerWebExchange exchange, String fmtMessage, Object... args) {
        RespBase<?> resp = RespBase.create().withCode(status.value()).withMessage(format(fmtMessage, args));
        ServerHttpResponse response = exchange.getResponse();
        DataBuffer buffer = response.bufferFactory().wrap(resp.asJson().getBytes(UTF_8));
        response.setStatusCode(status);
        return response.writeWith(just(buffer));
    }

    private Mono<Void> bindSignedToContext(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            SimpleSignAuthingFilterFactory.Config config,
            String appId) {

        // Add the current authenticated client ID to the request header,
        // this will allow the back-end resource services to recognize the
        // current client ID.
        ServerHttpRequest request = exchange.getRequest().mutate().header(config.getAddSignAuthClientIdHeader(), appId).build();

        // Sets the current authenticated client ID to context principal,
        // For example: for subsequent current limiting based on client ID.
        // see:org.springframework.cloud.gateway.filter.ratelimit.PrincipalNameKeyResolver#resolve()
        // see:org.springframework.security.web.server.context.SecurityContextServerWebExchangeWebFilter#filter()
        return chain.filter(exchange.mutate().principal(Mono.just(new SimpleSignPrincipal(appId))).request(request).build());
    }

    private void addCounterMetrics(
            ServerWebExchange exchange,
            MetricsName metricsName,
            SimpleSignAuthingFilterFactory.Config config) {
        metricsFacade.counter(exchange, metricsName, 1, MetricsTag.SIGN_ALG, config.getSignAlgorithm().name(),
                MetricsTag.SIGN_HASH, config.getSignHashingMode().name());
    }

    private void addTimerMetrics(
            ServerWebExchange exchange,
            MetricsName metricsName,
            SimpleSignAuthingFilterFactory.Config config,
            long beginNanoTime) {
        metricsFacade.timer(exchange, metricsName, beginNanoTime, MetricsTag.SIGN_ALG, config.getSignAlgorithm().name(),
                MetricsTag.SIGN_HASH, config.getSignHashingMode().name());
    }

    private void publishSuccessEvent(String appId, SimpleSignAuthingFilterFactory.Config config) {
        eventBus.post(new SimpleSignAuthingSuccessEvent(appId, config.getAppIdExtractor(), config.getSignAlgorithm(),
                config.getSignHashingMode()));
    }

    private void publishFailureEvent(String appId, SimpleSignAuthingFilterFactory.Config config) {
        eventBus.post(new SimpleSignAuthingFailureEvent(appId, config.getAppIdExtractor(), config.getSignAlgorithm(),
                config.getSignHashingMode()));
    }

    @Getter
    @Setter
    @ToString
    public static class Config {
        public static final String DEFAULT_SIGN_AUTH_CLIENT_HEADER = "X-Sign-Auth-AppId";

        /**
         * AppId parameter extract configuration.
         */
        private AppIdExtractor appIdExtractor = AppIdExtractor.Parameter;

        /**
         * Only valid when appId extract mode is parameter.
         */
        private String appIdParam = "appId";

        /**
         * Note: It is only used to concatenate plain-text string salts when
         * hashing signatures. (not required as a request parameter)
         */
        private String secretParam = "appSecret";

        /**
         * Whether to enable signature replay attack interception.
         */
        private boolean signReplayVerifyEnabled = true;

        /**
         * Bloom filter sign cache expiration for replay attacks verification.
         */
        private Integer signReplayVerifyBloomExpireSeconds = 7 * 24 * 60 * 60;

        /*
         * Signature parameters configuration.
         */
        private String signParam = "sign";
        private SignAlgorithm signAlgorithm = SignAlgorithm.S256;
        private SignHashingMode signHashingMode = SignHashingMode.UriParamsKeySortedHashing;
        private List<String> signHashingIncludeParams = new ArrayList<>(4);
        private List<String> signHashingExcludeParams = new ArrayList<>(4);
        private List<String> signHashingRequiredIncludeParams = new ArrayList<>(4);

        /**
         * Add the current authenticated client ID to the request header, this
         * will allow the back-end resource services to recognize the current
         * client ID.
         */
        private String addSignAuthClientIdHeader = DEFAULT_SIGN_AUTH_CLIENT_HEADER;

        //
        // Temporary fields.
        //
        @Setter(lombok.AccessLevel.NONE)
        private transient Boolean isIncludeAll;

        public boolean isIncludeAll() {
            if (nonNull(isIncludeAll)) {
                return isIncludeAll;
            }
            return (isIncludeAll = safeList(getSignHashingIncludeParams()).stream().anyMatch(n -> eqIgnCase("*", n)));
        }
    }

    @Getter
    @AllArgsConstructor
    public static enum AppIdExtractor {

        Parameter(args -> {
            Config config = (Config) args[0];
            ServerWebExchange exchange = (ServerWebExchange) args[1];
            return hasText(exchange.getRequest().getQueryParams().getFirst(config.getAppIdParam()), "%s missing",
                    config.getAppIdParam());
        }),

        /**
         * In some special business platform scenarios, the signature
         * authentication protocol may not define appId (such as Alibaba Cloud
         * Market SaaS product authentication API), then the uniqueness of the
         * client application can only be determined according to the request
         * route ID.
         */
        @SuppressWarnings("unused")
        RouteId(args -> {
            Config config = (Config) args[0];
            ServerWebExchange exchange = (ServerWebExchange) args[1];
            return ((Route) exchange.getAttributes().get(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR)).getId();
        });

        private final Function<Object[], String> function;
    }

    @SuppressWarnings("deprecation")
    @Getter
    @AllArgsConstructor
    public static enum SignAlgorithm {
        MD5(input -> Hashing.md5().hashBytes(input[1]).asBytes()),

        S1(input -> Hashing.sha1().hashBytes(input[1]).asBytes()),

        S256(input -> Hashing.sha256().hashBytes(input[1]).asBytes()),

        S384(input -> Hashing.sha384().hashBytes(input[1]).asBytes()),

        S512(input -> Hashing.sha512().hashBytes(input[1]).asBytes()),

        HMD5(input -> Hashing.hmacMd5(input[0]).hashBytes(input[1]).asBytes()),

        HS1(input -> Hashing.hmacSha1(input[0]).hashBytes(input[1]).asBytes()),

        HS256(input -> Hashing.hmacSha256(input[0]).hashBytes(input[1]).asBytes()),

        HS512(input -> Hashing.hmacSha512(input[0]).hashBytes(input[1]).asBytes());

        private final Function<byte[][], byte[]> function;
    }

    @Getter
    @AllArgsConstructor
    public static enum SignHashingMode {

        SimpleParamsBytesSortedHashing(args -> {
            Config config = (Config) args[0];
            byte[] storedAppSecret = (byte[]) args[1];
            ServerHttpRequest request = (ServerHttpRequest) args[2];
            Map<String, String> queryParams = request.getQueryParams().toSingleValueMap();
            String[] params = getEffectiveHashingParamNames(config, queryParams);
            StringBuffer signPlaintext = new StringBuffer();
            for (Object key : params) {
                if (!config.getSignParam().equals(key)) {
                    signPlaintext.append(queryParams.get(key));
                }
            }
            // Add stored secret.
            signPlaintext.append(new String(storedAppSecret, UTF_8));
            // ASCII sort characters.
            byte[] signPlainBytes = signPlaintext.toString().getBytes(UTF_8);
            Arrays.sort(signPlainBytes);
            return signPlainBytes;
        }),

        UriParamsKeySortedHashing(args -> {
            Config config = (Config) args[0];
            byte[] storedAppSecret = (byte[]) args[1];
            ServerHttpRequest request = (ServerHttpRequest) args[2];
            Map<String, String> queryParams = request.getQueryParams().toSingleValueMap();
            String[] params = getEffectiveHashingParamNames(config, queryParams);
            // ASCII sort by parameters key.
            Arrays.sort(params);
            StringBuffer signPlaintext = new StringBuffer();
            for (Object name : params) {
                if (!config.getSignParam().equals(name)) {
                    signPlaintext.append(name).append("=").append(queryParams.get(name)).append("&");
                }
            }
            // Add stored secret.
            signPlaintext.append(config.getSecretParam()).append("=").append(new String(storedAppSecret, UTF_8));
            return signPlaintext.toString().getBytes(UTF_8);
        });

        private final Function<Object[], byte[]> function;

        private static String[] getEffectiveHashingParamNames(Config config, Map<String, String> queryParams) {
            List<String> hashingParamNames = queryParams.keySet()
                    .stream()
                    .filter(n -> config.isIncludeAll() || safeList(config.getSignHashingIncludeParams()).contains(n))
                    .filter(n -> !safeList(config.getSignHashingExcludeParams()).contains(n))
                    .collect(toList());

            // Validation required parameters.
            boolean allMatch = safeList(config.getSignHashingRequiredIncludeParams()).stream()
                    .allMatch(p -> hashingParamNames.contains(p));
            if (!allMatch) {
                throw new IllegalArgumentException(format("Parameters missing, These parameters are required: %s",
                        config.getSignHashingRequiredIncludeParams()));
            }
            return hashingParamNames.toArray(new String[0]);
        }
    }

    @AllArgsConstructor
    public static class SimpleSignPrincipal implements Principal {
        private final String appId;

        @Override
        public String getName() {
            return appId;
        }
    }

    @AllArgsConstructor
    class SimpleSignAuthingGatewayFilter implements GatewayFilter, Ordered {
        private final Config config;

        @Override
        public int getOrder() {
            return SafeFilterOrdered.ORDER_SIMPLE_SIGN;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            if (JvmRuntimeTool.isJvmInDebugging && authingConfig.getSimpleSign().isAnonymousAuthingWithJvmDebug()) {
                return chain.filter(exchange);
            }

            // Gets request signature.(required)
            String sign = null;
            try {
                sign = hasText(exchange.getRequest().getQueryParams().getFirst(config.getSignParam()), "%s missing",
                        config.getSignParam());
            } catch (IllegalArgumentException e) {
                log.warn("Bad request missing signature. - {}", exchange.getRequest().getURI());
                return writeResponse(HttpStatus.BAD_REQUEST, exchange, "bad_request - hint '%s'", e.getMessage());
            }
            // Determine request appId.
            String appId = null;
            try {
                appId = getRequestAppId(config, exchange);
            } catch (IllegalArgumentException e) {
                log.warn("Bad request missing the appId. - {}", exchange.getRequest().getURI());
                return writeResponse(HttpStatus.BAD_REQUEST, exchange, "bad_request - hint '%s'", e.getMessage());
            }

            // Check replay attacks.
            if (config.isSignReplayVerifyEnabled()) {
                if (obtainBloomFilter(exchange, config).bloomExist(getBloomKey(exchange), sign)) {
                    log.warn("Illegal signature locked. - sign={}, appId={}", sign, appId);
                    addCounterMetrics(exchange, MetricsName.SIMPLE_SIGN_BLOOM_FAIL_TOTAL, config);
                    // Publish failure event.
                    publishFailureEvent(appId, config);
                    return writeResponse(HttpStatus.LOCKED, exchange, "illegal_signature");
                }
            }

            // Verify signature.
            try {
                byte[] _sign = doSignature(config, exchange, appId);
                if (!isEqual(_sign, Hex.decodeHex(sign.toCharArray()))) {
                    log.warn("Invalid request sign='{}', sign='{}'", sign, Hex.encodeHexString(_sign));
                    addCounterMetrics(exchange, MetricsName.SIMPLE_SIGN_FAIL_TOTAL, config);
                    // Publish failure event.
                    publishFailureEvent(appId, config);
                    return writeResponse(HttpStatus.UNAUTHORIZED, exchange, "invalid_signature");
                }
                log.info("Verified request appId='{}', sign='{}'", appId, sign);

                metricsFacade.counter(exchange, MetricsName.SIMPLE_SIGN_SUCCCESS_TOTAL, 1);
                if (config.isSignReplayVerifyEnabled()) {
                    obtainBloomFilter(exchange, config).bloomAdd(getBloomKey(exchange), sign);
                    addCounterMetrics(exchange, MetricsName.SIMPLE_SIGN_BLOOM_SUCCESS_TOTAL, config);
                }
                // Publish success event.
                publishSuccessEvent(appId, config);
            } catch (DecoderException e) {
                return writeResponse(HttpStatus.INTERNAL_SERVER_ERROR, exchange, "unavailable");
            } catch (IllegalArgumentException e) {
                return writeResponse(HttpStatus.BAD_REQUEST, exchange, "invalid_signature - hint '%s'", e.getMessage());
            }

            return bindSignedToContext(exchange, chain, config, appId);
        }
    }

    public static final String NAME_SIMPLE_SIGN_FILTER = "SimpleSignAuthing";

}