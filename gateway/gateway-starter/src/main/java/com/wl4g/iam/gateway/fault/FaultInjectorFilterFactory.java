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
import static java.util.Collections.singletonMap;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.base.Predicates;
import com.wl4g.iam.gateway.fault.config.FaultProperties;
import com.wl4g.iam.gateway.fault.config.FaultProperties.AbstractInjectorProperties;
import com.wl4g.iam.gateway.fault.config.FaultProperties.InjectorProperties;
import com.wl4g.iam.gateway.fault.config.FaultProperties.InjectorProvider;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsName;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade.MetricsTag;
import com.wl4g.iam.gateway.util.IamGatewayUtil;
import com.wl4g.iam.gateway.util.IamGatewayUtil.SafeFilterOrdered;
import com.wl4g.infra.common.bean.ConfigBeanUtils;
import com.wl4g.infra.core.utils.web.ReactiveRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
    private final SpelRequestMatcher requestMatcher;
    private final IamGatewayMetricsFacade metricsFacade;

    public FaultInjectorFilterFactory(FaultProperties faultConfig, IamGatewayMetricsFacade metricsFacade) {
        super(FaultInjectorFilterFactory.Config.class);
        this.faultConfig = notNullOf(faultConfig, "faultConfig");
        this.metricsFacade = notNullOf(metricsFacade, "metricsFacade");
        // Build gray request matcher.
        this.requestMatcher = new SpelRequestMatcher(faultConfig.getPreferMatchRuleDefinitions());

    }

    @Override
    public String name() {
        return BEAN_NAME;
    }

    private void applyDefaultToConfig(Config config) {
        try {
            ConfigBeanUtils.configureWithDefault(new FaultInjectorFilterFactory.Config(), config, faultConfig.getDefaultInject());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException("Unable apply defaults to fault config", e);
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        applyDefaultToConfig(config);
        return new FaultInjectorGatewayFilter(config);
    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class Config extends InjectorProperties {
    }

    @AllArgsConstructor
    class FaultInjectorGatewayFilter implements GatewayFilter, Ordered {
        private final Config config;

        @Override
        public int getOrder() {
            return SafeFilterOrdered.ORDER_FAULT;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            if (!isFaultRequest(exchange)) {
                return chain.filter(exchange);
            }

            // Add metrics of total.
            metricsFacade.counter(exchange, MetricsName.FAULT_TOTAL, 1, MetricsTag.ROUTE_ID, IamGatewayUtil.getRouteId(exchange),
                    MetricsTag.FAULT_INJECTOR, config.getProvider().name());

            switch (config.getProvider()) {
            case Abort:
                if (isFaultWithPercentage(config.getAbort())) {
                    setResponseHeaders(exchange, InjectorProvider.Abort);
                    ServerWebExchangeUtils.setResponseStatus(exchange, HttpStatusHolder.parse(config.getAbort().getStatusCode()));
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            case FixedDelay:
                if (isFaultWithPercentage(config.getFixedDelay())) {
                    setResponseHeaders(exchange, InjectorProvider.FixedDelay);
                    return Mono.delay(Duration.ofMillis(config.getFixedDelay().getDelayMs())).then(chain.filter(exchange));
                }
                return chain.filter(exchange);
            case RangeDelay:
                if (isFaultWithPercentage(config.getRangeDelay())) {
                    setResponseHeaders(exchange, InjectorProvider.RangeDelay);
                    long delayMs = ThreadLocalRandom.current().nextLong(config.getRangeDelay().getMinDelayMs(),
                            config.getRangeDelay().getMaxDelayMs());
                    return Mono.delay(Duration.ofMillis(delayMs)).then(chain.filter(exchange));
                }
                return chain.filter(exchange);
            default:
                // throw new Error("Shouldn't be here");
                log.warn("Failed to inject fault because injector provider '{}' is not recognized.", config.getProvider());
                return chain.filter(exchange);
            }
        }

        /**
         * Determine if fault injection is required based on request matcher.
         * 
         * @param exchange
         * @return
         */
        private boolean isFaultRequest(ServerWebExchange exchange) {
            // Gets current request route.
            Route route = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

            // Add routeId temporary predicates.
            Map<String, Supplier<Predicate<String>>> routeIdPredicateSupplier = singletonMap(VAR_ROUTE_ID,
                    () -> Predicates.equalTo(route.getId()));

            return requestMatcher.matches(new ReactiveRequestExtractor(exchange.getRequest()),
                    faultConfig.getPreferOpenMatchExpression(), routeIdPredicateSupplier);
        }

        private boolean isFaultWithPercentage(AbstractInjectorProperties injectorConfig) {
            // if fault injection is required based on random percentage.
            double per = ThreadLocalRandom.current().nextDouble();
            return per < injectorConfig.getPercentage();
        }

        private void setResponseHeaders(ServerWebExchange exchange, InjectorProvider provider) {
            exchange.getResponse().getHeaders().add(faultConfig.getFaultInjectedHeader(), provider.name());
        }

    }

    public static final String BEAN_NAME = "FaultInjector";
    public static final String VAR_ROUTE_ID = "routeId";

}
