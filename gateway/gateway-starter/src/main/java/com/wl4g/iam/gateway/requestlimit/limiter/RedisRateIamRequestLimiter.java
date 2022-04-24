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
package com.wl4g.iam.gateway.requestlimit.limiter;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF_RATE;
import static java.lang.System.nanoTime;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.validation.annotation.Validated;

import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.iam.gateway.requestlimit.event.RateLimitHitEvent;
import com.wl4g.iam.gateway.requestlimit.limiter.IamRequestLimiter.LimiterStrategy;
import com.wl4g.infra.common.eventbus.EventBusSupport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link RedisRateIamRequestLimiter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-19 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter}
 * @see https://blog.csdn.net/songhaifengshuaige/article/details/93372437
 */
@Getter
@Setter
@Slf4j
public class RedisRateIamRequestLimiter extends AbstractRedisIamRequestLimiter {

    private @Autowired IamRequestLimiterProperties requestLimiterConfig;
    private @Autowired ReactiveStringRedisTemplate redisTemplate;
    private @Autowired RedisScript<List<Long>> redisScript;
    private @Autowired EventBusSupport eventBus;
    private @Autowired IamGatewayMetricsFacade metricsFacade;
    private @Autowired RedisRateLimiterStrategy defaultConfig;

    // public RedisRateIamRequestLimiter( ) {
    // this.rateLimiterConfig = notNullOf(rateLimiterConfig,
    // "rateLimiterConfig");
    // this.redisTemplate = notNullOf(redisTemplate, "redisTemplate");
    // this.redisScript = notNullOf(redisScript, "redisScript");
    // this.metricsFacade = notNullOf(metricsFacade, "metricsFacade");
    // this.eventBus = notNullOf(eventBus, "eventBus");
    // this.defaultConfig = new
    // Config(rateLimiterConfig.getRateLimitConfig().getDefaultReplenishRate(),
    // rateLimiterConfig.getRateLimitConfig().getDefaultBurstCapacity(),
    // rateLimiterConfig.getRateLimitConfig().getDefaultRequestedTokens());
    // }

    @Override
    public RequestLimiterPrivoder kind() {
        return RequestLimiterPrivoder.RedisRateLimiter;
    }

    /**
     * This uses a basic token bucket algorithm and relies on the fact that
     * Redis scripts execute atomically. No other operations can run between
     * fetching the count and writing the new count.
     */
    @Override
    public Mono<LimitedResult> isAllowed(String routeId, String id) {
        metricsFacade.counter(MetricsName.REDIS_RATELIMIT_TOTAL, routeId, 1);
        long beginTime = nanoTime();

        RedisRateLimiterStrategy strategy = loadConfiguration(routeId);
        // How many requests per second do you want a user to be allowed to do?
        int replenishRate = strategy.getReplenishRate();
        // How much bursting do you want to allow?
        int burstCapacity = strategy.getBurstCapacity();
        // How many tokens are requested per request?
        int requestedTokens = strategy.getRequestedTokens();
        try {
            List<String> keys = getKeys(id);

            // The arguments to the LUA script. time() returns unixtime in
            // seconds.
            List<String> scriptArgs = Arrays.asList(replenishRate + "", burstCapacity + "", Instant.now().getEpochSecond() + "",
                    requestedTokens + "");
            // allowed, tokens_left = redis.eval(SCRIPT, keys, args)
            Flux<List<Long>> flux = redisTemplate.execute(redisScript, keys, scriptArgs);
            // .log("redisratelimiter", Level.FINER);
            return flux.onErrorResume(throwable -> {
                if (log.isDebugEnabled()) {
                    log.debug("Error calling rate limiter lua", throwable);
                }
                return Flux.just(Arrays.asList(1L, -1L));
            }).reduce(new ArrayList<Long>(), (longs, l) -> {
                longs.addAll(l);
                return longs;
            }).map(results -> {
                boolean allowed = results.get(0) == 1L;
                Long tokensLeft = results.get(1);

                LimitedResult resp = new LimitedResult(allowed, tokensLeft, createHeaders(strategy, tokensLeft));
                if (log.isDebugEnabled()) {
                    log.debug("response: {}", resp);
                }

                // [Begin] ADD feature for metrics
                metricsFacade.timer(MetricsName.REDIS_RATELIMIT_TIME, routeId, beginTime);
                if (!allowed) { // Total hits metric
                    metricsFacade.counter(MetricsName.REDIS_RATELIMIT_HITS_TOTAL, routeId, 1);
                    eventBus.post(new RateLimitHitEvent(id));
                }
                // [End] ADD feature for metrics

                return resp;
            });
        } catch (Exception e) {
            /*
             * We don't want a hard dependency on Redis to allow traffic. Make
             * sure to set an alert so you know if this is happening too much.
             * Stripe's observed failure rate is 0.01%.
             */
            log.error("Error determining if user allowed from redis", e);
        }
        return Mono.just(new LimitedResult(true, -1L, createHeaders(strategy, -1L)));
    }

    protected List<String> getKeys(String id) {
        // use `{}` around keys to use Redis Key hash tags
        // this allows for using redis cluster

        // Make a unique key per user.
        String prefix = "request_rate_limiter.{" + id;

        // You need two Redis keys for Token Bucket.
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    protected RedisRateLimiterStrategy loadConfiguration(String routeId) {
        RedisRateLimiterStrategy routeConfig = getConfig().getOrDefault(routeId, defaultConfig);
        if (routeConfig == null) {
            routeConfig = getConfig().get(RouteDefinitionRouteLocator.DEFAULT_FILTERS);
        }
        if (routeConfig == null) {
            throw new IllegalArgumentException("No Configuration found for route " + routeId + " or defaultFilters");
        }
        return routeConfig;
    }

    protected Map<String, String> createHeaders(RedisRateLimiterStrategy config, Long tokensLeft) {
        Map<String, String> headers = new HashMap<>();
        if (requestLimiterConfig.isIncludeHeaders()) {
            headers.put(requestLimiterConfig.getRemainingHeader(), tokensLeft.toString());
            headers.put(requestLimiterConfig.getReplenishRateHeader(), String.valueOf(config.getReplenishRate()));
            headers.put(requestLimiterConfig.getBurstCapacityHeader(), String.valueOf(config.getBurstCapacity()));
            headers.put(requestLimiterConfig.getRequestedTokensHeader(), String.valueOf(config.getRequestedTokens()));
        }
        return headers;
    }

    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RedisRateLimiterStrategy extends LimiterStrategy {

        /**
         * Redis tokens rate limiter user-level configuration key prefix.
         */
        private String prefix = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF_RATE;

        /**
         * The default token bucket capacity, that is, the total number of
         * concurrency allowed.
         */
        private @Min(0) int burstCapacity = 1;

        /**
         * How many requests per second do you want a user to be allowed to do?
         */
        private @Min(1) int replenishRate = 1;

        /**
         * How many tokens are requested per request?
         */
        private @Min(1) int requestedTokens = 1;

        /**
         * The name of the header that returns the burst capacity configuration.
         */
        private String burstCapacityHeader = BURST_CAPACITY_HEADER;

        /**
         * The name of the header that returns the replenish rate configuration.
         */
        private String replenishRateHeader = REPLENISH_RATE_HEADER;

        /**
         * The name of the header that returns the requested tokens
         * configuration.
         */
        private String requestedTokensHeader = REQUESTED_TOKENS_HEADER;

        /**
         * The name of the header that returns number of remaining requests
         * during the current second.
         */
        private String remainingHeader = REMAINING_HEADER;

    }

    /**
     * Remaining Rate Limit header name.
     */
    public static final String REMAINING_HEADER = "X-RateLimit-Remaining";

    /**
     * Replenish Rate Limit header name.
     */
    public static final String REPLENISH_RATE_HEADER = "X-RateLimit-Replenish-Rate";

    /**
     * Burst Capacity header name.
     */
    public static final String BURST_CAPACITY_HEADER = "X-RateLimit-Burst-Capacity";

    /**
     * Requested Tokens header name.
     */
    public static final String REQUESTED_TOKENS_HEADER = "X-RateLimit-Requested-Tokens";

}
