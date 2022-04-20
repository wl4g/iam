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

package com.wl4g.iam.gateway.requestlimit;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.iam.gateway.requestlimit.configure.IamRequestLimiterConfigure;
import com.wl4g.iam.gateway.requestlimit.key.IamKeyResolver;
import com.wl4g.iam.gateway.requestlimit.key.IamKeyResolver.KeyResolverType;
import com.wl4g.infra.core.framework.operator.GenericOperatorAdapter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * {@link IamRequestLimiterGatewayFilterFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-19 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory}
 */
@Getter
@Setter
@ToString
public class IamRequestLimiterGatewayFilterFactory
        extends AbstractGatewayFilterFactory<IamRequestLimiterGatewayFilterFactory.Config> {
    private static final String EMPTY_KEY = "____EMPTY_KEY__";

    private @Autowired IamRequestLimiterProperties config;
    private @Autowired IamRequestLimiterConfigure rateLimiterConfigure;
    private @Autowired IamRedisTokenRateLimiter iamRateLimiter;
    private @Autowired GenericOperatorAdapter<KeyResolverType, IamKeyResolver> keyResolverAdapter;

    public IamRequestLimiterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public String name() {
        return BEAN_IAM_RATELIMIT_FILTER;
    }

    @Override
    public GatewayFilter apply(Config config) {
        applyDefaultToConfig(config);
        IamKeyResolver resolver = getKeyResolver(config);
        return new IamRequestRateLimiterGatewayFilter(config, resolver, iamRateLimiter);
    }

    private void applyDefaultToConfig(Config config) {
        // TODO the requestLimiter includes of rateLimiter and counterLimiter

        // try {
        // ConfigBeanUtils.configureWithDefault(new ChooseProperties(),
        // config.getChoose(), loadBalancerConfig.getChoose());
        // } catch (IllegalArgumentException | IllegalAccessException e) {
        // throw new IllegalStateException(e);
        // }
    }

    private IamKeyResolver getKeyResolver(Config config) {
        return keyResolverAdapter.forOperator(config.getKeyResolverType());
    }

    @Getter
    @Setter
    @ToString
    public static class Config implements HasRouteId {
        private KeyResolverType keyResolverType;
        private HttpStatus statusCode = HttpStatus.TOO_MANY_REQUESTS;
        private Boolean denyEmptyKey;
        private String emptyKeyStatus;
        private String routeId;
    }

    @AllArgsConstructor
    public static class IamRequestRateLimiterGatewayFilter implements GatewayFilter {
        private final Config config;
        private final IamKeyResolver resolver;
        private final IamRedisTokenRateLimiter iamRateLimiter;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            return resolver.resolve(exchange).defaultIfEmpty(EMPTY_KEY).flatMap(key -> {
                if (EMPTY_KEY.equals(key)) {
                    if (config.getDenyEmptyKey()) {
                        setResponseStatus(exchange, HttpStatusHolder.parse(config.getEmptyKeyStatus()));
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                }
                String routeId = config.getRouteId();
                if (routeId == null) {
                    Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                    routeId = route.getId();
                }
                return iamRateLimiter.isAllowed(routeId, key).flatMap(response -> {
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
    }

    public static final String BEAN_IAM_RATELIMIT_FILTER = "IamRequestRateLimiter";
}
