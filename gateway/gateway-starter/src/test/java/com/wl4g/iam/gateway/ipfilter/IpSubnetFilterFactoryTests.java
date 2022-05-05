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
package com.wl4g.iam.gateway.ipfilter;

import static java.util.Arrays.asList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import org.junit.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import com.wl4g.iam.gateway.ipfilter.IpSubnetFilterFactory;
import com.wl4g.iam.gateway.ipfilter.config.IpFilterProperties;
import com.wl4g.iam.gateway.ipfilter.configurer.IpFilterConfigurer;
import com.wl4g.iam.gateway.mock.MockGatewayFilterChain;

import reactor.core.publisher.Mono;
import reactor.netty.tcp.InetSocketAddressUtil;
import reactor.test.StepVerifier;

/**
 * {@link IpSubnetFilterFactoryTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-05 v3.0.0
 * @since v3.0.0
 */
public class IpSubnetFilterFactoryTests {

    @Test
    public void testIpFilterAllowed() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://httpbin.org/hello")
                .remoteAddress(InetSocketAddressUtil.createUnresolved("192.168.3.2", 0))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Add route to attributes.
        // see:org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator#convertToRoute(RouteDefinition)
        Route route = Route.async()
                .asyncPredicate(AsyncPredicate.from(_exchange -> true))
                .id("my-test-route")
                .uri("http://httpbin.org/")
                .build();
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);

        IpSubnetFilterFactory factory = new IpSubnetFilterFactory(new IpFilterProperties(), new IpFilterConfigurer() {
            @Override
            public Mono<List<FilterStrategy>> loadStrategy(@NotBlank String routeId, @NotBlank String principalName) {
                List<FilterStrategy> strategys = new ArrayList<>();
                // strategys.add(new FilterStrategy(false,
                // asList("192.168.0.0/16")));
                strategys.add(new FilterStrategy(true, asList("192.168.3.0/24")));
                strategys.add(new FilterStrategy(true, asList("10.88.3.0/24")));
                return Mono.just(strategys);
            }
        });

        GatewayFilter verifierTailFilter = (_exchange, chain) -> {
            System.out.println(">>>>>>>>>> Access passed!!!");
            return chain.filter(_exchange);
        };

        GatewayFilter filter = factory.apply(new IpSubnetFilterFactory.Config());
        Mono<Void> mono = new MockGatewayFilterChain(asList(filter, verifierTailFilter)).filter(exchange);

        StepVerifier.create(mono).expectComplete().verify(Duration.ofSeconds(5));

        System.out.println(">>>>>>>>>> Completion");

    }

}
