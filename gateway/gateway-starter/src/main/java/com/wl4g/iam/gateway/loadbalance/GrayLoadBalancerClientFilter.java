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
package com.wl4g.iam.gateway.loadbalance;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

import java.net.URI;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.LoadBalancerUriTools;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.loadbalance.rule.GrayLoadBalancerRule;
import com.wl4g.infra.common.log.SmartLogger;

import reactor.core.publisher.Mono;

/**
 * Grayscale filter
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 */
public class GrayLoadBalancerClientFilter extends ReactiveLoadBalancerClientFilter {

    private final SmartLogger log = getLogger(getClass());
    private final LoadBalancerProperties properties;
    private final GrayLoadBalancerRule grayLoadBalancerRule;

    public GrayLoadBalancerClientFilter(LoadBalancerClientFactory clientFactory, LoadBalancerProperties properties,
            GrayLoadBalancerRule grayLoadBalancerRule) {
        super(clientFactory, properties);
        this.properties = notNullOf(properties, "properties");
        this.grayLoadBalancerRule = notNullOf(grayLoadBalancerRule, "grayLoadBalancerRule");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10000;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI requestUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String schemePrefix = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR);

        // Ignore the URi prefix If it does not start with LB, go to the
        // next filter.
        if (isNull(requestUri) || (!equalsAnyIgnoreCase("LB", requestUri.getScheme(), schemePrefix))) {
            return chain.filter(exchange);
        }

        // According to the original URL of the gateway. Replace the URI of
        // http://IP:PORT/path
        addOriginalRequestUrl(exchange, requestUri);
        log.trace(ReactiveLoadBalancerClientFilter.class.getSimpleName() + " url before: " + requestUri);

        return doChoose(exchange).doOnNext(response -> {
            if (!response.hasServer()) {
                throw NotFoundException.create(properties.isUse404(), "Unable to find instance for " + requestUri.getHost());
            }
            URI uri = exchange.getRequest().getURI();
            // if the `lb:<scheme>` mechanism was used, use `<scheme>` as the
            // default, if the loadbalancer doesn't provide one.
            String overrideScheme = !isBlank(schemePrefix) ? requestUri.getScheme() : null;
            DelegatingServiceInstance instance = new DelegatingServiceInstance(response.getServer(), overrideScheme);
            URI newRequestUri = LoadBalancerUriTools.reconstructURI(instance, uri);
            log.trace("LoadBalancerClientFilter url chosen: {}", newRequestUri);
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, newRequestUri);
        }).then(chain.filter(exchange));
    }

    private Mono<Response<ServiceInstance>> doChoose(ServerWebExchange exchange) {
        URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        return Mono.just(new DefaultResponse(grayLoadBalancerRule.choose(uri.getHost(), exchange.getRequest())));
    }

}