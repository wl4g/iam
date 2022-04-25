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
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.iam.gateway.requestlimit.configurer.LimiterStrategyConfigurer;
import com.wl4g.iam.gateway.requestlimit.limiter.IamRequestLimiter.RequestLimiterStrategy;
import com.wl4g.infra.common.eventbus.EventBusSupport;
import com.wl4g.infra.common.log.SmartLogger;

/**
 * {@link AbstractRedisIamRequestLimiter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-21 v3.0.0
 * @since v3.0.0
 */
public abstract class AbstractRedisIamRequestLimiter<S extends RequestLimiterStrategy> implements IamRequestLimiter {
    protected final SmartLogger log = getLogger(getClass());

    protected final IamRequestLimiterProperties requestLimiterConfig;
    protected final LimiterStrategyConfigurer configurer;
    protected final ReactiveStringRedisTemplate redisTemplate;
    protected final EventBusSupport eventBus;
    protected final IamGatewayMetricsFacade metricsFacade;

    public AbstractRedisIamRequestLimiter(IamRequestLimiterProperties requestLimiterConfig, LimiterStrategyConfigurer configurer,
            ReactiveStringRedisTemplate redisTemplate, EventBusSupport eventBus, IamGatewayMetricsFacade metricsFacade) {
        this.requestLimiterConfig = notNullOf(requestLimiterConfig, "requestLimiterConfig");
        this.configurer = notNullOf(configurer, "configurer");
        this.redisTemplate = notNullOf(redisTemplate, "redisTemplate");
        this.eventBus = notNullOf(eventBus, "eventBus");
        this.metricsFacade = notNullOf(metricsFacade, "metricsFacade");
    }

}
