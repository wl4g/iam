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

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.System.nanoTime;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Min;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.validation.annotation.Validated;

import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.requestlimit.IamRequestLimiterGatewayFilterFactory;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties.RedisRateLimiterStrategyProperties;
import com.wl4g.iam.gateway.requestlimit.configurer.LimiterStrategyConfigurer;
import com.wl4g.iam.gateway.requestlimit.event.RateLimitHitEvent;
import com.wl4g.iam.gateway.requestlimit.limiter.RedisRateIamRequestLimiter.RedisRateLimiterStrategy;
import com.wl4g.infra.common.eventbus.EventBusSupport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
public class RedisRateIamRequestLimiter extends AbstractRedisIamRequestLimiter<RedisRateLimiterStrategy> {

    private final RedisScript<List<Long>> redisScript;

    public RedisRateIamRequestLimiter(RedisScript<List<Long>> redisScript, IamRequestLimiterProperties requestLimiterConfig,
            LimiterStrategyConfigurer configurer, ReactiveStringRedisTemplate redisTemplate, EventBusSupport eventBus,
            IamGatewayMetricsFacade metricsFacade) {
        super(requestLimiterConfig, configurer, redisTemplate, eventBus, metricsFacade);
        this.redisScript = notNullOf(redisScript, "redisScript");
    }

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
    public Mono<LimitedResult> isAllowed(IamRequestLimiterGatewayFilterFactory.Config config, String routeId, String limitKey) {
        metricsFacade.counter(MetricsName.REDIS_RATELIMIT_TOTAL, routeId, 1);
        final long beginTime = nanoTime();

        return configurer.loadRateStrategy(routeId, limitKey)
                .defaultIfEmpty(requestLimiterConfig.getDefaultLimiter().getRate())
                .flatMap(strategy -> {
                    // How many requests per second do you want a user to be
                    // allowed to do?
                    int replenishRate = strategy.getReplenishRate();
                    // How much bursting do you want to allow?
                    int burstCapacity = strategy.getBurstCapacity();
                    // How many tokens are requested per request?
                    int requestedTokens = strategy.getRequestedTokens();
                    try {
                        List<String> keys = getKeys(strategy, limitKey);

                        // The arguments to the LUA script. time() returns
                        // unixtime in
                        // seconds.
                        List<String> scriptArgs = Arrays.asList(replenishRate + "", burstCapacity + "",
                                Instant.now().getEpochSecond() + "", requestedTokens + "");

                        // allowed, tokens_left = redis.eval(SCRIPT, keys, args)
                        return redisTemplate.execute(redisScript, keys, scriptArgs)
                                // .log("redisRateIamRequestLimiter",
                                // Level.FINER);
                                .onErrorResume(throwable -> {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Error calling rate limiter lua", throwable);
                                    }
                                    return Flux.just(Arrays.asList(1L, -1L));
                                })
                                .reduce(new ArrayList<Long>(), (longs, l) -> {
                                    longs.addAll(l);
                                    return longs;
                                })
                                .map(results -> {
                                    boolean allowed = results.get(0) == 1L;
                                    Long tokensLeft = results.get(1);

                                    LimitedResult result = new LimitedResult(allowed, tokensLeft,
                                            createHeaders(strategy, tokensLeft));
                                    if (log.isDebugEnabled()) {
                                        log.debug("response: {}", result);
                                    }

                                    // [Begin] ADD feature for metrics
                                    metricsFacade.timer(MetricsName.REDIS_RATELIMIT_TIME, routeId, beginTime);
                                    if (!allowed) { // Total hits metric
                                        metricsFacade.counter(MetricsName.REDIS_RATELIMIT_HITS_TOTAL, routeId, 1);
                                        eventBus.post(new RateLimitHitEvent(limitKey));
                                    }
                                    // [End] ADD feature for metrics

                                    return result;
                                });
                    } catch (Exception e) {
                        /*
                         * We don't want a hard dependency on Redis to allow
                         * traffic. Make sure to set an alert so you know if
                         * this is happening too much. Stripe's observed failure
                         * rate is 0.01%.
                         */
                        log.error("Error determining if user allowed from redis", e);
                    }
                    return Mono.just(new LimitedResult(true, -1L, createHeaders(strategy, -1L)));
                });
    }

    protected List<String> getKeys(RedisRateLimiterStrategy strategy, String limitKey) {
        // use `{}` around keys to use Redis Key hash tags
        // this allows for using redis cluster

        // Make a unique key per user.
        String prefix = requestLimiterConfig.getDefaultLimiter().getRate().getTokenPrefix().concat(".{").concat(limitKey);

        // You need two Redis keys for Token Bucket.
        String tokenKey = prefix.concat("}.tokens");
        String timestampKey = prefix.concat("}.timestamp");

        return Arrays.asList(tokenKey, timestampKey);
    }

    protected Map<String, String> createHeaders(RedisRateLimiterStrategy strategy, Long tokensLeft) {
        Map<String, String> headers = new HashMap<>();
        if (strategy.isIncludeHeaders()) {
            RedisRateLimiterStrategyProperties config = requestLimiterConfig.getDefaultLimiter().getRate();
            headers.put(config.getRemainingHeader(), tokensLeft.toString());
            headers.put(config.getReplenishRateHeader(), String.valueOf(strategy.getReplenishRate()));
            headers.put(config.getBurstCapacityHeader(), String.valueOf(strategy.getBurstCapacity()));
            headers.put(config.getRequestedTokensHeader(), String.valueOf(strategy.getRequestedTokens()));
        }
        return headers;
    }

    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RedisRateLimiterStrategy extends IamRequestLimiter.RequestLimiterStrategy {

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

        @Override
        public RequestLimiterPrivoder getProvider() {
            return RequestLimiterPrivoder.RedisRateLimiter;
        }
    }

}
