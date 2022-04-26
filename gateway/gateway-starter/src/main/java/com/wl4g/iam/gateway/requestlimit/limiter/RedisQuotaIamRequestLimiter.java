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

import static java.lang.System.nanoTime;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Min;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.validation.annotation.Validated;

import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.requestlimit.IamRequestLimiterGatewayFilterFactory;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties.RedisQuotaLimiterStrategyProperties;
import com.wl4g.iam.gateway.requestlimit.configurer.LimiterStrategyConfigurer;
import com.wl4g.iam.gateway.requestlimit.event.QuotaLimitHitEvent;
import com.wl4g.iam.gateway.requestlimit.limiter.RedisQuotaIamRequestLimiter.RedisQuotaLimiterStrategy;
import com.wl4g.infra.common.eventbus.EventBusSupport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link RedisQuotaIamRequestLimiter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-21 v3.0.0
 * @since v3.0.0
 */
public class RedisQuotaIamRequestLimiter extends AbstractRedisIamRequestLimiter<RedisQuotaLimiterStrategy> {

    public RedisQuotaIamRequestLimiter(IamRequestLimiterProperties requestLimiterConfig, LimiterStrategyConfigurer configurer,
            ReactiveStringRedisTemplate redisTemplate, EventBusSupport eventBus, IamGatewayMetricsFacade metricsFacade) {
        super(requestLimiterConfig, configurer, redisTemplate, eventBus, metricsFacade);
    }

    @Override
    public RequestLimiterPrivoder kind() {
        return RequestLimiterPrivoder.RedisQuotaLimiter;
    }

    @Override
    public Mono<LimitedResult> isAllowed(IamRequestLimiterGatewayFilterFactory.Config config, String routeId, String limitKey) {
        metricsFacade.counter(MetricsName.REDIS_QUOTALIMIT_TOTAL, routeId, 1);
        final long beginTime = nanoTime();

        return configurer.loadQuotaStrategy(routeId, limitKey)
                .defaultIfEmpty(requestLimiterConfig.getDefaultLimiter().getQuota())
                .flatMap(strategy -> {
                    try {
                        String key = getKey(strategy, routeId, limitKey);
                        return redisTemplate.opsForValue().increment(key, 1).onErrorResume(ex -> {
                            if (log.isDebugEnabled()) {
                                log.debug("Error calling quota limiter redis", ex);
                            }
                            return Mono.empty();
                        }).map(accumulated -> {
                            long requestCapacity = strategy.getRequestCapacity();
                            long tokensLeft = requestCapacity - accumulated;
                            boolean allowed = accumulated < requestCapacity;

                            LimitedResult result = new LimitedResult(allowed, tokensLeft, createHeaders(strategy, tokensLeft));
                            if (log.isDebugEnabled()) {
                                log.debug("response: {}", result);
                            }
                            metricsFacade.timer(MetricsName.REDIS_QUOTALIMIT_TIME, routeId, beginTime);
                            if (!allowed) { // Total hits metric
                                metricsFacade.counter(MetricsName.REDIS_QUOTALIMIT_HITS_TOTAL, routeId, 1);
                                eventBus.post(new QuotaLimitHitEvent(limitKey));
                            }
                            return result;
                        });
                    } catch (Exception e) {
                        /*
                         * We don't want a hard dependency on Redis to allow
                         * traffic. Make sure to set an alert so you know if
                         * this is happening too much. Stripe's observed failure
                         * rate is 0.01%.
                         */
                        log.error("Error determining if user allowed quota from redis", e);
                    }

                    return Mono.just(new LimitedResult(true, -1L, createHeaders(strategy, -1L)));
                });
    }

    protected Map<String, String> createHeaders(RedisQuotaLimiterStrategy strategy, Long tokensLeft) {
        Map<String, String> headers = new HashMap<>();
        if (strategy.isIncludeHeaders()) {
            RedisQuotaLimiterStrategyProperties config = requestLimiterConfig.getDefaultLimiter().getQuota();
            headers.put(config.getRemainingHeader(), tokensLeft.toString());
            headers.put(config.getRequestCapacityHeader(), String.valueOf(strategy.getRequestCapacity()));
        }
        return headers;
    }

    protected String getKey(RedisQuotaLimiterStrategy strategy, String routeId, String limitKey) {
        return requestLimiterConfig.getDefaultLimiter()
                .getQuota()
                .getTokenPrefix()
                .concat(":")
                .concat(routeId)
                .concat(":")
                .concat(limitKey);
    }

    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RedisQuotaLimiterStrategy extends IamRequestLimiter.RequestLimiterStrategy {

        /**
         * The number of total maximum allowed requests capacity.
         */
        private @Min(0) Long requestCapacity = 1000L;

        /**
         * The date pattern of request quota limit calculation cycle.
         */
        private String cycleDatePattern = "yyyyMMdd";

        @Override
        public RequestLimiterPrivoder getProvider() {
            return RequestLimiterPrivoder.RedisQuotaLimiter;
        }
    }

}
