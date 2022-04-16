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
package com.wl4g.iam.gateway.circuitbreaker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.common.constant.GatewayIAMConstants;
import com.wl4g.iam.gateway.circuitbreaker.DefaultCircuitBreakerCustomizer;

/**
 * {@link CircuitBreakerAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-16 v3.0.0
 * @since v3.0.0
 * @see https://cloud.spring.io/spring-cloud-circuitbreaker/reference/html/spring-cloud-circuitbreaker.html#auto-configuration
 * @see https://resilience4j.readme.io/docs/examples
 */
public class CustomCircuitBreakerAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_CIRCUITBREAKER)
    public CustomCircuitBreakerProperties customCircuitBreakerProperties() {
        return new CustomCircuitBreakerProperties();
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
            CustomCircuitBreakerProperties config,
            DefaultCircuitBreakerCustomizer customizer) {
        return factory -> {
            factory.configureDefault(id -> new Resilience4JConfigBuilder(id).circuitBreakerConfig(config.toCircuitBreakerConfig())
                    .timeLimiterConfig(config.toTimeLimiterConfig())
                    .build());
            factory.addCircuitBreakerCustomizer(customizer, customizer.getClass().getSimpleName());
        };
    }

}
