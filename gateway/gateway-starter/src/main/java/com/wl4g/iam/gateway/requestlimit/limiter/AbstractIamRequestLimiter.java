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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.iam.gateway.requestlimit.configurer.IamRequestLimiterConfigurer;
import com.wl4g.iam.gateway.requestlimit.configurer.LimitStrategy;
import com.wl4g.infra.common.eventbus.EventBusSupport;

import reactor.core.publisher.Mono;

/**
 * {@link AbstractIamRequestLimiter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-21 v3.0.0
 * @since v3.0.0
 */
public abstract class AbstractIamRequestLimiter implements IamRequestLimiter {

    protected @Autowired IamRequestLimiterProperties rateLimiterConfig;
    protected @Autowired IamRequestLimiterConfigurer configurer;
    protected @Autowired ReactiveStringRedisTemplate redisTemplate;
    protected @Autowired EventBusSupport eventBus;
    protected @Autowired IamGatewayMetricsFacade metricsFacade;

    protected LimitStrategy loadLimitStrategy(String routeId, String limitId) {
        Mono<LimitStrategy> strategy = configurer.loadStrategy(routeId, limitId);

        if (strategy.equals(Mono.empty())) {
            return rateLimiterConfig.getLimitConfig().getDefaultStrategy();
        }

        // TODO use reactor mono return
        // strategy.then(Mono.defer(() -> {
        // return Mono.empty();
        // }));
        return strategy.block();
    }

}
