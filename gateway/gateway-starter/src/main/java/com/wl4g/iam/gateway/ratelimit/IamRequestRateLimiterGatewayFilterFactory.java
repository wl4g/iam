/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.iam.gateway.ratelimit;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;

import com.wl4g.iam.gateway.ratelimit.config.IamRateLimiterProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link IamRequestRateLimiterGatewayFilterFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-19 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory}
 */
@Getter
@Setter
@ToString
public class IamRequestRateLimiterGatewayFilterFactory
        extends AbstractGatewayFilterFactory<IamRequestRateLimiterGatewayFilterFactory.Config> {

    /**
     * Key-Resolver key.
     */
    public static final String KEY_RESOLVER_KEY = "keyResolver";
    private static final String EMPTY_KEY = "____EMPTY_KEY__";

    private final IamRateLimiterProperties rateLimiterConfig;
    private final IamRedisTokenRateLimiter defaultRateLimiter;
    private final KeyResolver defaultKeyResolver;

    public IamRequestRateLimiterGatewayFilterFactory(IamRateLimiterProperties rateLimiterConfig,
            IamRedisTokenRateLimiter defaultRateLimiter, KeyResolver defaultKeyResolver) {
        super(Config.class);
        this.rateLimiterConfig = rateLimiterConfig;
        this.defaultRateLimiter = defaultRateLimiter;
        this.defaultKeyResolver = defaultKeyResolver;
    }

    @Override
    public String name() {
        return BEAN_IAM_RATELIMIT_FILTER;
    }

    @Override
    public GatewayFilter apply(Config config) {
        KeyResolver resolver = getOrDefault(config.keyResolver, defaultKeyResolver);
        IamRedisTokenRateLimiter limiter = getOrDefault(config.rateLimiter, defaultRateLimiter);
        boolean denyEmpty = getOrDefault(config.denyEmptyKey, rateLimiterConfig.isDenyEmptyKey());
        HttpStatusHolder emptyKeyStatus = HttpStatusHolder
                .parse(getOrDefault(config.emptyKeyStatus, rateLimiterConfig.getEmptyKeyStatusCode()));

        return (exchange, chain) -> resolver.resolve(exchange).defaultIfEmpty(EMPTY_KEY).flatMap(key -> {
            if (EMPTY_KEY.equals(key)) {
                if (denyEmpty) {
                    setResponseStatus(exchange, emptyKeyStatus);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            }
            String routeId = config.getRouteId();
            if (routeId == null) {
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                routeId = route.getId();
            }
            return limiter.isAllowed(routeId, key).flatMap(response -> {
                for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                    exchange.getResponse().getHeaders().add(header.getKey(), header.getValue());
                }

                if (response.isAllowed()) {
                    return chain.filter(exchange);
                }

                setResponseStatus(exchange, config.getStatusCode());
                return exchange.getResponse().setComplete();
            });
        });
    }

    // private void applyDefaultToConfig(Config config) {
    // try {
    // ConfigBeanUtils.configureWithDefault(new ChooseProperties(),
    // config.getChoose(), loadBalancerConfig.getChoose());
    // } catch (IllegalArgumentException | IllegalAccessException e) {
    // throw new IllegalStateException(e);
    // }
    // }

    private <T> T getOrDefault(T configValue, T defaultValue) {
        return (configValue != null) ? configValue : defaultValue;
    }

    @Getter
    @Setter
    @ToString
    public static class Config implements HasRouteId {
        private KeyResolver keyResolver;
        private IamRedisTokenRateLimiter rateLimiter;
        private HttpStatus statusCode = HttpStatus.TOO_MANY_REQUESTS;
        private Boolean denyEmptyKey;
        private String emptyKeyStatus;
        private String routeId;
    }

    public static final String BEAN_IAM_RATELIMIT_FILTER = "IamRequestRateLimiter";
}
