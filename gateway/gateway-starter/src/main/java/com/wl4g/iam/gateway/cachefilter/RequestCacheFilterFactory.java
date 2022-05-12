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

import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.cache.Cache;
import com.wl4g.iam.gateway.cachefilter.config.CacheFilterProperties;
import com.wl4g.iam.gateway.cachefilter.config.CacheFilterProperties.CachedProperties;
import com.wl4g.iam.gateway.util.IamGatewayUtil.SafeDefaultFilterOrdered;
import com.wl4g.infra.common.bean.ConfigBeanUtils;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link IamRetryFilterFactory}
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
            throw new IllegalStateException("Unable apply defaults to cache filter config", e);
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        applyDefaultToConfig(config);

        SpelRequestMatcher requestMatcher = new SpelRequestMatcher(cacheFilterConfig.getPreferMatchRuleDefinitions());

        switch (config.getProvider()) {
        case LocalCache:
            Cache<String, Object> localCache = newBuilder()
                    .expireAfterAccess(config.getLocal().getExpireAfterAccess(), MILLISECONDS)
                    .expireAfterWrite(config.getLocal().getExpireAfterWrite(), MILLISECONDS)
                    .concurrencyLevel(4)// TODO
                    .build();
            return new RequestCacheGatewayFilter(config, requestMatcher);
        case RedisCache:
            break;
        default:
            break;
        }
        return new RequestCacheGatewayFilter(config, requestMatcher);
    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class Config extends CachedProperties {
    }

    @AllArgsConstructor
    class RequestCacheGatewayFilter implements GatewayFilter, Ordered {
        private final Config config;
        private final SpelRequestMatcher requestMatcher;

        @Override
        public int getOrder() {
            return SafeDefaultFilterOrdered.ORDER_CACHE_FILTER;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            // TODO

            return chain.filter(exchange);
        }

    }

    public static final String BEAN_NAME = "CacheFilter";
    public static final String VAR_ROUTE_ID = "routeId";

}
