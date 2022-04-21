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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.iam.gateway.requestlimit.event.QuotaLimitHitEvent;
import com.wl4g.infra.common.eventbus.EventBusSupport;

import reactor.core.publisher.Mono;

/**
 * {@link RedisQuotaIamRequestLimiter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-21 v3.0.0
 * @since v3.0.0
 */
public class RedisQuotaIamRequestLimiter implements IamRequestLimiter {

    private @Autowired IamRequestLimiterProperties rateLimiterConfig;
    private @Autowired ReactiveStringRedisTemplate redisTemplate;
    private @Autowired EventBusSupport eventBus;
    private @Autowired IamGatewayMetricsFacade metricsFacade;

    @Override
    public RequestLimiterPrivoder kind() {
        return RequestLimiterPrivoder.RedisQuotaLimiter;
    }

    @Override
    public Mono<LimitedResponse> isAllowed(String routeId, String id) {
        metricsFacade.counter(MetricsName.REDIS_QUOTALIMIT_TOTAL, routeId, 1);
        long beginTime = nanoTime();

        boolean allowed = false;

        metricsFacade.timer(MetricsName.REDIS_QUOTALIMIT_TIME, routeId, beginTime);
        if (!allowed) { // Total hits metric
            metricsFacade.counter(MetricsName.REDIS_QUOTALIMIT_HITS_TOTAL, routeId, 1);
            eventBus.post(new QuotaLimitHitEvent(id));
        }

        // TODO Auto-generated method stub
        return null;
    }

}
