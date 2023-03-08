/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
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
package com.wl4g.iam.gateway.requestlimit.config;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import com.wl4g.iam.common.constant.GatewayIAMConstants;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.requestlimit.IamRequestLimiterFilterFactory;
import com.wl4g.iam.gateway.requestlimit.configurer.LimiterStrategyConfigurer;
import com.wl4g.iam.gateway.requestlimit.configurer.RedisLimiterStrategyConfigurer;
import com.wl4g.iam.gateway.requestlimit.event.DefaultRedisRequestLimitEventRecorder;
import com.wl4g.iam.gateway.requestlimit.key.HeaderIamKeyResolver;
import com.wl4g.iam.gateway.requestlimit.key.HostIamKeyResolver;
import com.wl4g.iam.gateway.requestlimit.key.IamKeyResolver;
import com.wl4g.iam.gateway.requestlimit.key.IamKeyResolver.KeyResolverProvider;
import com.wl4g.iam.gateway.requestlimit.key.IamKeyResolver.KeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.IntervalIamKeyResolver;
import com.wl4g.iam.gateway.requestlimit.key.IpRangeIamKeyResolver;
import com.wl4g.iam.gateway.requestlimit.key.PathIamKeyResolver;
import com.wl4g.iam.gateway.requestlimit.key.PrincipalIamKeyResolver;
import com.wl4g.iam.gateway.requestlimit.limiter.IamRequestLimiter;
import com.wl4g.iam.gateway.requestlimit.limiter.IamRequestLimiter.RequestLimiterPrivoder;
import com.wl4g.iam.gateway.requestlimit.limiter.quota.RedisQuotaIamRequestLimiter;
import com.wl4g.iam.gateway.requestlimit.limiter.rate.RedisRateIamRequestLimiter;
import com.wl4g.infra.common.eventbus.EventBusSupport;
import com.wl4g.infra.common.framework.operator.GenericOperatorAdapter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link IamRequestLimiterAutoConfiguration}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-10-13 v1.0.0
 * @since v1.0.0
 */
@Slf4j
public class IamRequestLimiterAutoConfiguration {

    //
    // IAM rate limiter configuration.
    //

    @Bean
    @ConfigurationProperties(prefix = GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_REQUESTLIMIT)
    public IamRequestLimiterProperties iamRequestLimiterProperties() {
        return new IamRequestLimiterProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public LimiterStrategyConfigurer redisLimiterStrategyConfigurer() {
        return new RedisLimiterStrategyConfigurer();
    }

    //
    // IAM Key resolver.
    //

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> hostIamKeyResolver() {
        return new HostIamKeyResolver();
    }

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> ipRangeIamKeyResolver() {
        return new IpRangeIamKeyResolver();
    }

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> headerIamKeyResolver() {
        return new HeaderIamKeyResolver();
    }

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> pathIamKeyResolver() {
        return new PathIamKeyResolver();
    }

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> principalNameIamKeyResolver() {
        return new PrincipalIamKeyResolver();
    }

    @Bean
    public IamKeyResolver<? extends KeyResolverStrategy> intervalIamKeyResolver() {
        return new IntervalIamKeyResolver();
    }

    @Bean
    public GenericOperatorAdapter<KeyResolverProvider, IamKeyResolver<? extends KeyResolverStrategy>> iamKeyResolverAdapter(
            List<IamKeyResolver<? extends KeyResolverStrategy>> resolvers) {
        return new GenericOperatorAdapter<KeyResolverProvider, IamKeyResolver<? extends KeyResolverStrategy>>(resolvers) {
        };
    }

    //
    // IAM request limiter.
    //

    /**
     * {@link org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration#redisRateLimite}
     */
    @Bean
    public RedisRateLimiter warningDeprecatedRedisRateLimiter(
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
            ConfigurationService configurationService) {
        return new WarningDeprecatedRedisRateLimiter(redisTemplate, redisScript, configurationService);
    }

    /**
     * {@link org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration#redisRateLimite}
     */
    @Bean
    public IamRequestLimiter redisRateIamRequestLimiter(
            RedisScript<List<Long>> redisScript,
            IamRequestLimiterProperties requestLimiterConfig,
            LimiterStrategyConfigurer configurer,
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus,
            IamGatewayMetricsFacade metricsFacade) {
        return new RedisRateIamRequestLimiter(redisScript, requestLimiterConfig, configurer, redisTemplate, eventBus,
                metricsFacade);
    }

    @Bean
    public IamRequestLimiter redisQuotaIamRequestLimiter(
            IamRequestLimiterProperties requestLimiterConfig,
            LimiterStrategyConfigurer configurer,
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus,
            IamGatewayMetricsFacade metricsFacade) {
        return new RedisQuotaIamRequestLimiter(requestLimiterConfig, configurer, redisTemplate, eventBus, metricsFacade);
    }

    @Bean
    public GenericOperatorAdapter<RequestLimiterPrivoder, IamRequestLimiter> iamRequestLimiterAdapter(
            List<IamRequestLimiter> rqeuestLimiters) {
        return new GenericOperatorAdapter<RequestLimiterPrivoder, IamRequestLimiter>(rqeuestLimiters) {
        };
    }

    /**
     * @see {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration#requestRateLimiterGatewayFilterFactory}
     */
    @Bean
    public IamRequestLimiterFilterFactory iamRequestLimiterFilterFactory(
            IamRequestLimiterProperties requsetLimiterConfig,
            GenericOperatorAdapter<KeyResolverProvider, IamKeyResolver<? extends KeyResolverStrategy>> keyResolverAdapter,
            GenericOperatorAdapter<RequestLimiterPrivoder, IamRequestLimiter> requestLimiterAdapter) {
        return new IamRequestLimiterFilterFactory(requsetLimiterConfig, keyResolverAdapter, requestLimiterAdapter);
    }

    //
    // IAM limiter event.
    //

    @Bean(name = BEAN_REDIS_RATELIMITE_EVENTBUS, destroyMethod = "close")
    public EventBusSupport redisRateLimiteEventBusSupport(IamRequestLimiterProperties requestLimiteConfig) {
        return new EventBusSupport(requestLimiteConfig.getEventRecorder().getPublishEventBusThreads());
    }

    @Bean
    public DefaultRedisRequestLimitEventRecorder redisRateLimiteEventRecoder(
            @Qualifier(BEAN_REDIS_RATELIMITE_EVENTBUS) EventBusSupport eventBus) {
        DefaultRedisRequestLimitEventRecorder recorder = new DefaultRedisRequestLimitEventRecorder();
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

    public static final String BEAN_REDIS_RATELIMITE_EVENTBUS = "redisRateLimiteEventBusSupport";
    public static final String LOG_MESSAGE_WARNING_REDIS_RATE_LIMITER = "\n[WARNING]: The default redisRateLimiter is deprecated, please use the IAM rate limiter with the configuration key prefix: 'spring.iam.gateway.ratelimit'\n";

}
