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

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF_QUOTA;
import static java.lang.System.nanoTime;

import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.validation.annotation.Validated;

import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.iam.gateway.requestlimit.event.QuotaLimitHitEvent;
import com.wl4g.infra.common.eventbus.EventBusSupport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link RedisQuotaIamRequestLimiter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-21 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class RedisQuotaIamRequestLimiter extends AbstractRedisIamRequestLimiter {

    @Override
    public RequestLimiterPrivoder kind() {
        return RequestLimiterPrivoder.RedisQuotaLimiter;
    }

    @Override
    public Mono<LimitedResult> isAllowed(String routeId, String limitKey) {
        metricsFacade.counter(MetricsName.REDIS_QUOTALIMIT_TOTAL, routeId, 1);
        long beginTime = nanoTime();

        // TODO
        redisTemplate.opsForValue().increment("TODO", 1).onErrorResume(ex -> {
            if (log.isDebugEnabled()) {
                log.debug("Error calling quota limiter redis", ex);
            }
            return Mono.empty();
        }).map(accumulated -> {
            // TODO use separated config
            long requestCapacity = requestLimiterConfig.getDefaultLimiter().getQuota().getRequestCapacity();
            long tokensLeft = requestCapacity - accumulated;
            boolean allowed = accumulated < requestCapacity;

            LimitedResult resp = new LimitedResult(allowed, tokensLeft, createHeaders(routeConfig, tokensLeft));
            if (log.isDebugEnabled()) {
                log.debug("response: {}", resp);
            }
            metricsFacade.timer(MetricsName.REDIS_QUOTALIMIT_TIME, routeId, beginTime);
            if (!allowed) { // Total hits metric
                metricsFacade.counter(MetricsName.REDIS_QUOTALIMIT_HITS_TOTAL, routeId, 1);
                eventBus.post(new QuotaLimitHitEvent(limitKey));
            }
            return Mono.just(resp);
        });

        return Mono.just(new LimitedResult(true, -1L, createHeaders(routeConfig, -1L)));
    }

    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RedisQuotaLimiterStrategy extends LimiterStrategy {

        /**
         * Redis quota limiter user-level configuration key prefix.
         */
        private String prefix = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF_QUOTA;

        /**
         * The number of total maximum allowed requests capacity.
         */
        private @Min(0) Long requestCapacity = 1000L;

        /**
         * The date pattern of request quota limit calculation cycle.
         */
        private String cycleDatePattern = "yyyyMMdd";

    }

}
