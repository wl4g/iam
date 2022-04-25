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
import com.wl4g.iam.gateway.requestlimit.configurer.LimiterStrategyConfigurer;
import com.wl4g.iam.gateway.requestlimit.limiter.IamRequestLimiter.LimiterStrategy;
import com.wl4g.infra.common.eventbus.EventBusSupport;

import reactor.core.publisher.Mono;

/**
 * {@link AbstractRedisIamRequestLimiter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-21 v3.0.0
 * @since v3.0.0
 */
public abstract class AbstractRedisIamRequestLimiter<S extends LimiterStrategy> implements IamRequestLimiter {

    protected @Autowired IamRequestLimiterProperties requestLimiterConfig;
    protected @Autowired LimiterStrategyConfigurer configurer;
    protected @Autowired ReactiveStringRedisTemplate redisTemplate;
    protected @Autowired EventBusSupport eventBus;
    protected @Autowired IamGatewayMetricsFacade metricsFacade;

    @SuppressWarnings("unchecked")
    protected Mono<S> loadStrategy(String routeId, String limitKey) {
        return (Mono<S>) configurer.loadStrategy(this, routeId, limitKey).defaultIfEmpty(getDefaultStrategy());
    }

    protected abstract LimiterStrategy getDefaultStrategy();

}
