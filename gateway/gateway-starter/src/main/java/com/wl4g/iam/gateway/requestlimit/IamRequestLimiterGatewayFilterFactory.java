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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimitedProperties;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties.LimiterProperties;
import com.wl4g.iam.gateway.requestlimit.configurer.IamRequestLimiterConfigurer;
import com.wl4g.iam.gateway.requestlimit.key.IamKeyResolver;
import com.wl4g.iam.gateway.requestlimit.key.IamKeyResolver.KeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.IamKeyResolver.KeyResolverProvider;
import com.wl4g.iam.gateway.requestlimit.limiter.IamRequestLimiter;
import com.wl4g.iam.gateway.requestlimit.limiter.IamRequestLimiter.RequestLimiterPrivoder;
import com.wl4g.iam.gateway.requestlimit.limiter.RedisRateIamRequestLimiter;
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

    private @Autowired IamRequestLimitedProperties config;
    private @Autowired IamRequestLimiterConfigurer rateLimiterConfigure;
    private @Autowired RedisRateIamRequestLimiter iamRateLimiter;
    private @Autowired GenericOperatorAdapter<KeyResolverProvider, IamKeyResolver<? extends KeyResolverStrategy>> keyResolverAdapter;
    private @Autowired GenericOperatorAdapter<RequestLimiterPrivoder, IamRequestLimiter> requestLimiterAdapter;

    public IamRequestLimiterGatewayFilterFactory() {
        super(IamRequestLimiterGatewayFilterFactory.Config.class);
    }

    @Override
    public String name() {
        return BEAN_IAM_RATELIMIT_FILTER;
    }

    @Override
    public GatewayFilter apply(IamRequestLimiterGatewayFilterFactory.Config config) {
        applyDefaultToConfig(config);
        return new IamRequestLimiterGatewayFilter(config, getKeyResolver(config), getRequestLimiter(config));
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

    private IamKeyResolver<? extends KeyResolverStrategy> getKeyResolver(IamRequestLimiterGatewayFilterFactory.Config config) {
        return keyResolverAdapter.forOperator(config.getKeyResolverPrivoder());
    }

    private IamRequestLimiter getRequestLimiter(IamRequestLimiterGatewayFilterFactory.Config config) {
        return requestLimiterAdapter.forOperator(config.getLimiterPrivoder());
    }

    @Getter
    @Setter
    @ToString
    public static class Config {

        /**
         * Switch to deny requests if the Key Resolver returns an empty key,
         * defaults to true.
         */
        private boolean denyEmptyKey = true;

        /**
         * HttpStatus to return when denyEmptyKey is true, defaults to
         * FORBIDDEN.
         */
        private String emptyKeyStatusCode = HttpStatus.FORBIDDEN.name();

        /**
         * HttpStatus to return when limiter is true, defaults to
         * TOO_MANY_REQUESTS.
         */
        private String statusCode = HttpStatus.TOO_MANY_REQUESTS.name();

        /**
         * Request limiter key resolver provider.
         */
        private KeyResolverProvider keyProvider;

        /**
         * Request limiter provider.
         */
        private RequestLimiterPrivoder limiterPrivoder;
    }

    @AllArgsConstructor
    public static class IamRequestLimiterGatewayFilter implements GatewayFilter {
        private final Config config;
        private final IamKeyResolver<? extends KeyResolverStrategy> resolver;
        private final IamRequestLimiter requestLimiter;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            // TODO parameter 1
            return resolver.resolve(null, exchange).defaultIfEmpty(EMPTY_KEY).flatMap(key -> {
                if (EMPTY_KEY.equals(key)) {
                    if (config.getDenyEmptyKey()) {
                        setResponseStatus(exchange, HttpStatusHolder.parse(config.getEmptyKeyStatus()));
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                }
                Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                return requestLimiter.isAllowed(route.getId(), key).flatMap(response -> {
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

    public static final String BEAN_IAM_RATELIMIT_FILTER = "IamRequestLimiter";
}
