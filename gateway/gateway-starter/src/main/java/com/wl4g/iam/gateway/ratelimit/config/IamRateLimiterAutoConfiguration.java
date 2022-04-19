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
package com.wl4g.iam.gateway.ratelimit.config;

import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.PrincipalNameKeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import com.wl4g.iam.common.constant.GatewayIAMConstants;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.ratelimit.IamRedisRateLimiter;
import com.wl4g.iam.gateway.ratelimit.key.HostKeyResolver;
import com.wl4g.iam.gateway.ratelimit.key.UriKeyResolver;
import com.wl4g.iam.gateway.ratelimit.recorder.RedisRateLimitEventRecorder;
import com.wl4g.infra.common.eventbus.EventBusSupport;
import com.wl4g.infra.common.log.SmartLogger;

import reactor.core.publisher.Mono;

/**
 * {@link IamRateLimiterAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-10-13 v1.0.0
 * @since v1.0.0
 */
public class IamRateLimiterAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_RATELIMIT)
    public IamRateLimiterProperties iamRateLimiterProperties() {
        return new IamRateLimiterProperties();
    }

    @Primary
    @Bean(BEAN_HOST_RESOLVER)
    public HostKeyResolver hostKeyResolver() {
        return new HostKeyResolver();
    }

    @Bean(PrincipalNameKeyResolver.BEAN_NAME)
    public PrincipalNameKeyResolver principalNameKeyResolver() {
        return new PrincipalNameKeyResolver();
    }

    @Bean(BEAN_URI_RESOLVER)
    public UriKeyResolver uriKeyResolver() {
        return new UriKeyResolver();
    }

    /**
     * {@link org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration#redisRateLimite}
     */
    @Bean
    @ConditionalOnBean(IamRedisRateLimiter.class)
    public RedisRateLimiter warningDeprecatedRedisRateLimiter(
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
            ConfigurationService configurationService) {
        return new RedisRateLimiter(redisTemplate, redisScript, configurationService) {
            private final SmartLogger log = getLogger(getClass());

            @Override
            public Mono<Response> isAllowed(String routeId, String id) {
                log.warn(
                        "\n[WARNING]: The default redisRateLimiter is deprecated, please use the IAM rate limiter with the configuration prefix: 'spring.iam.gateway.ratelimit'\n");
                return Mono.empty(); // Ignore
            }
        };
    }

    /**
     * {@link org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration#redisRateLimite}
     */
    @Bean
    @Primary
    public IamRedisRateLimiter iamRedisRateLimiter(
            IamRateLimiterProperties rateLimiterConfig,
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
            IamGatewayMetricsFacade metricsFacade,
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus,
            ConfigurationService configurationService) {
        return new IamRedisRateLimiter(rateLimiterConfig, redisTemplate, redisScript, eventBus, metricsFacade,
                configurationService);
    }

    // Redis rate-limit event recorder

    @Bean(name = BEAN_REDIS_RATELIMITE_EVENTBUS, destroyMethod = "close")
    public EventBusSupport redisRateLimiteEventBusSupport(IamRateLimiterProperties rateLimiteConfig) {
        return new EventBusSupport(rateLimiteConfig.getEvent().getPublishEventBusThreads());
    }

    @Bean
    public RedisRateLimitEventRecorder redisRateLimiteEventRecoder(
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus) {
        RedisRateLimitEventRecorder recorder = new RedisRateLimitEventRecorder();
        eventBus.register(recorder);
        return recorder;
    }

    public static final String BEAN_HOST_RESOLVER = "hostKeyResolver";
    public static final String BEAN_URI_RESOLVER = "uriKeyResolver";
    public static final String BEAN_REDIS_RATELIMITE_EVENTBUS = "redisRateLimiteEventBusSupport";

}
