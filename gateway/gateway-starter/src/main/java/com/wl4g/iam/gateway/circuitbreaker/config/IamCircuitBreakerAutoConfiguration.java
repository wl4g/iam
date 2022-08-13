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
package com.wl4g.iam.gateway.circuitbreaker.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.DispatcherHandler;

import com.wl4g.iam.common.constant.GatewayIAMConstants;
import com.wl4g.iam.gateway.circuitbreaker.DefaultCircuitBreakerCustomizer;
import com.wl4g.iam.gateway.circuitbreaker.IamSpringCloudCircuitBreakerResilience4JFilterFactory;

/**
 * {@link CircuitBreakerAutoConfiguration}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-16 v3.0.0
 * @since v3.0.0
 * @see https://cloud.spring.io/spring-cloud-circuitbreaker/reference/html/spring-cloud-circuitbreaker.html#auto-configuration
 * @see https://resilience4j.readme.io/docs/examples
 */
public class IamCircuitBreakerAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_CIRCUITBREAKER)
    public IamCircuitBreakerProperties iamCircuitBreakerProperties() {
        return new IamCircuitBreakerProperties();
    }

    @SuppressWarnings("rawtypes")
    @Bean
    public IamSpringCloudCircuitBreakerResilience4JFilterFactory iamSpringCloudCircuitBreakerResilience4JFilterFactory(
            ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory,
            ObjectProvider<DispatcherHandler> dispatcherHandlerProvider) {
        return new IamSpringCloudCircuitBreakerResilience4JFilterFactory(reactiveCircuitBreakerFactory,
                dispatcherHandlerProvider);
    }

    @Bean
    public DefaultCircuitBreakerCustomizer defaultCircuitBreakerCustomizer() {
        return new DefaultCircuitBreakerCustomizer();
    }

    /**
     * {@link org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory#configureDefault(java.util.function.Function)}
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultReactiveResilience4JCircuitBreakerCustomizer(
            IamCircuitBreakerProperties circuitBreakerConfig,
            DefaultCircuitBreakerCustomizer customizer) {
        return factory -> {
            factory.configureDefault(
                    id -> new Resilience4JConfigBuilder(id).circuitBreakerConfig(circuitBreakerConfig.toCircuitBreakerConfig())
                            .timeLimiterConfig(circuitBreakerConfig.toTimeLimiterConfig())
                            .build());
            factory.addCircuitBreakerCustomizer(customizer, customizer.getClass().getSimpleName());
        };
    }

}
