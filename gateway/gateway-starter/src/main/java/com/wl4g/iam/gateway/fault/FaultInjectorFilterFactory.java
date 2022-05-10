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
package com.wl4g.iam.gateway.fault;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.fault.config.FaultProperties;
import com.wl4g.iam.gateway.fault.config.FaultProperties.InjectorProperties;
import com.wl4g.infra.common.bean.ConfigBeanUtils;
import com.wl4g.infra.core.web.matcher.ReactiveRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link FaultInjectorFilterFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-27 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class FaultInjectorFilterFactory extends AbstractGatewayFilterFactory<FaultInjectorFilterFactory.Config> {

    private final FaultProperties faultConfig;

    public FaultInjectorFilterFactory(FaultProperties faultConfig) {
        super(FaultInjectorFilterFactory.Config.class);
        this.faultConfig = notNullOf(faultConfig, "faultConfig");
    }

    @Override
    public String name() {
        return BEAN_NAME;
    }

    private void applyDefaultToConfig(Config config) {
        try {
            ConfigBeanUtils.configureWithDefault(new FaultInjectorFilterFactory.Config(), config, faultConfig.getDefaultInject());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException("Unable apply defaults to traffic imager gateway config", e);
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        applyDefaultToConfig(config);
        return new FaultInjectorGatewayFilter(config, new SpelRequestMatcher(config.getPreferMatchRuleDefinitions()));
    }

    public static class Config extends InjectorProperties {

    }

    @AllArgsConstructor
    public static class FaultInjectorGatewayFilter implements GatewayFilter {

        private final Config config;
        private final SpelRequestMatcher requestMatcher;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            if (!isFilterFault(exchange)) {
                return chain.filter(exchange);
            }

            switch (config.getProvider()) {
            case Abort:
                ServerWebExchangeUtils.setResponseStatus(exchange, HttpStatusHolder.parse(config.getAbort().getStatusCode()));
                return exchange.getResponse().setComplete();
            case FixedDelay:
                return Mono.delay(Duration.ofMillis(config.getFixedDelay().getDelayMs())).then(chain.filter(exchange));
            case RangeDelay:
                long delayMs = ThreadLocalRandom.current().nextLong(config.getRangeDelay().getMinDelayMs(),
                        config.getRangeDelay().getMaxDelayMs());
                return Mono.delay(Duration.ofMillis(delayMs)).then(chain.filter(exchange));
            default:
                // throw new Error("Shouldn't be here");
                log.warn("Failed to inject fault because injector provider '{}' is not recognized.", config.getProvider());
                return chain.filter(exchange);
            }
        }

        private boolean isFilterFault(ServerWebExchange exchange) {
            // Determine if fault injection is required based on request
            // matcher.
            if (!requestMatcher.matches(new ReactiveRequestExtractor(exchange.getRequest()),
                    config.getPreferOpenMatchExpression())) {
                return false;
            }

            // Determine if fault injection is required based on random
            // percentage.
            double percentage = ThreadLocalRandom.current().nextDouble();
            return percentage < config.getPreferMatchPercentage();
        }
    }

    public static final String BEAN_NAME = "FaultInjector";

}
