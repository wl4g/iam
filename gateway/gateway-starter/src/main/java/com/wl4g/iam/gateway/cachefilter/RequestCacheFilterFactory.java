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
package com.wl4g.iam.gateway.cachefilter;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.cachefilter.config.CacheFilterProperties;
import com.wl4g.iam.gateway.cachefilter.config.CacheFilterProperties.CachedProperties;
import com.wl4g.infra.common.bean.ConfigBeanUtils;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link RequestCacheFilterFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-11 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class RequestCacheFilterFactory extends AbstractGatewayFilterFactory<RequestCacheFilterFactory.Config> {

    private final CacheFilterProperties cacheFilterConfig;

    public RequestCacheFilterFactory(CacheFilterProperties cacheFilterConfig) {
        super(RequestCacheFilterFactory.Config.class);
        this.cacheFilterConfig = notNullOf(cacheFilterConfig, "cacheFilterConfig");
    }

    @Override
    public String name() {
        return BEAN_NAME;
    }

    private void applyDefaultToConfig(Config config) {
        try {
            ConfigBeanUtils.configureWithDefault(new RequestCacheFilterFactory.Config(), config,
                    cacheFilterConfig.getDefaultCache());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException("Unable apply defaults to traffic imager gateway config", e);
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        applyDefaultToConfig(config);
        return new RequestCacheGatewayFilter(config, new SpelRequestMatcher(cacheFilterConfig.getPreferMatchRuleDefinitions()));
    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class Config extends CachedProperties {
    }

    @AllArgsConstructor
    class RequestCacheGatewayFilter implements GatewayFilter {
        private final Config config;
        private final SpelRequestMatcher requestMatcher;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            // TODO

            return chain.filter(exchange);
        }
    }

    public static final String BEAN_NAME = "CacheFilter";
    public static final String VAR_ROUTE_ID = "routeId";

}
