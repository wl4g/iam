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

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.PrincipalNameKeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import com.wl4g.iam.common.constant.GatewayIAMConstants;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.ratelimit.IamRedisTokenRateLimiter;
import com.wl4g.iam.gateway.ratelimit.IamRequestRateLimiterGatewayFilterFactory;
import com.wl4g.iam.gateway.ratelimit.configure.IamRateLimiterConfigure;
import com.wl4g.iam.gateway.ratelimit.configure.RedisIamRateLimiterConfigure;
import com.wl4g.iam.gateway.ratelimit.key.HostKeyResolver;
import com.wl4g.iam.gateway.ratelimit.key.UriKeyResolver;
import com.wl4g.iam.gateway.ratelimit.recorder.RedisRateLimitEventRecorder;
import com.wl4g.infra.common.eventbus.EventBusSupport;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link IamRateLimiterAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-10-13 v1.0.0
 * @since v1.0.0
 */
@Slf4j
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

    @Bean
    @ConditionalOnMissingBean
    public IamRateLimiterConfigure redisIamRateLimiterConfigure() {
        return new RedisIamRateLimiterConfigure();
    }

    /**
     * {@link org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration#redisRateLimite}
     */
    @Bean
    public RedisRateLimiter warningDeprecatedRedisRateLimiter(
            @Lazy RouteDefinitionLocator routeLocator,
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
            ConfigurationService configurationService) {

        return new WarningDeprecatedRedisRateLimiter(redisTemplate, redisScript, configurationService);
    }

    /**
     * {@link org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration#redisRateLimite}
     */
    @Bean
    @Primary
    public IamRedisTokenRateLimiter iamRedisTokenRateLimiter(
            IamRateLimiterProperties rateLimiterConfig,
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
            IamGatewayMetricsFacade metricsFacade,
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus,
            ConfigurationService configurationService) {
        return new IamRedisTokenRateLimiter(rateLimiterConfig, redisTemplate, redisScript, eventBus, metricsFacade,
                configurationService);
    }

    /**
     * @see {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#requestRateLimiterGatewayFilterFactory}
     */
    @Bean
    public IamRequestRateLimiterGatewayFilterFactory iamRequestRateLimiterGatewayFilterFactory(
            IamRateLimiterProperties rateLimitConfig,
            IamRedisTokenRateLimiter rateLimiter,
            KeyResolver resolver) {
        return new IamRequestRateLimiterGatewayFilterFactory(rateLimitConfig, rateLimiter, resolver);
    }

    // Redis rate-limit event recorder

    @Bean(name = BEAN_REDIS_RATELIMITE_EVENTBUS, destroyMethod = "close")
    public EventBusSupport redisRateLimiteEventBusSupport(IamRateLimiterProperties rateLimiteConfig) {
        return new EventBusSupport(rateLimiteConfig.getEventRecorder().getPublishEventBusThreads());
    }

    @Bean
    public RedisRateLimitEventRecorder redisRateLimiteEventRecoder(
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus) {
        RedisRateLimitEventRecorder recorder = new RedisRateLimitEventRecorder();
        eventBus.register(recorder);
        return recorder;
    }

    class WarningDeprecatedRedisRateLimiter extends RedisRateLimiter implements ApplicationRunner {
        private @Lazy @Autowired RouteDefinitionLocator routeLocator;

        public WarningDeprecatedRedisRateLimiter(ReactiveStringRedisTemplate redisTemplate, RedisScript<List<Long>> script,
                ConfigurationService configurationService) {
            super(redisTemplate, script, configurationService);
        }

        @Override
        public void run(ApplicationArguments args) throws Exception {
            boolean useDefaultRedisRateLimiter = routeLocator.getRouteDefinitions().collectList().block().stream().anyMatch(
                    r -> safeList(r.getFilters()).stream().anyMatch(f -> StringUtils.equals(f.getName(), "RequestRateLimiter")));
            if (useDefaultRedisRateLimiter) {
                log.warn(LOG_MESSAGE_WARNING_REDIS_RATE_LIMITER);
            }
        }

        @Override
        public Mono<Response> isAllowed(String routeId, String id) {
            log.warn(LOG_MESSAGE_WARNING_REDIS_RATE_LIMITER);
            return Mono.empty(); // Ignore
        }
    }

    public static final String BEAN_HOST_RESOLVER = "hostKeyResolver";
    public static final String BEAN_URI_RESOLVER = "uriKeyResolver";
    public static final String BEAN_REDIS_RATELIMITE_EVENTBUS = "redisRateLimiteEventBusSupport";
    public static final String LOG_MESSAGE_WARNING_REDIS_RATE_LIMITER = "\n[WARNING]: The default redisRateLimiter is deprecated, please use the IAM rate limiter with the configuration key prefix: 'spring.iam.gateway.ratelimit'\n";

}
