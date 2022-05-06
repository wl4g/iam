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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.validation.constraints.NotBlank;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import com.wl4g.iam.gateway.ipfilter.config.IpFilterProperties;
import com.wl4g.iam.gateway.ipfilter.configurer.IpFilterConfigurer;
import com.wl4g.iam.gateway.ipfilter.configurer.IpFilterConfigurer.FilterStrategy;
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
    public void testAllowedWithAcceptAndNotReject() {
        List<FilterStrategy> strategys = new ArrayList<>();
        strategys.add(new FilterStrategy(false, asList("192.168.8.0/24")));
        strategys.add(new FilterStrategy(true, asList("192.168.3.0/24")));
        strategys.add(new FilterStrategy(true, asList("10.88.3.0/24")));

        IpSubnetFilterFactory.Config config = new IpSubnetFilterFactory.Config();

        boolean result = doTestIpFilter(config, "192.168.3.2", strategys);

        Assertions.assertTrue(result);
    }

    @Test
    public void testNotAllowedWithAcceptAndReject() {
        List<FilterStrategy> strategys = new ArrayList<>();
        strategys.add(new FilterStrategy(false, asList("192.168.0.0/16")));
        strategys.add(new FilterStrategy(true, asList("192.168.3.0/24")));
        strategys.add(new FilterStrategy(true, asList("10.88.3.0/24")));

        IpSubnetFilterFactory.Config config = new IpSubnetFilterFactory.Config();

        boolean result = doTestIpFilter(config, "192.168.3.2", strategys);

        Assertions.assertFalse(result);
    }

    @Test
    public void testAllowedWithAcceptAndRejectAndPreferAcceptOnCidrConflict() {
        List<FilterStrategy> strategys = new ArrayList<>();
        strategys.add(new FilterStrategy(false, asList("192.168.0.0/16")));
        strategys.add(new FilterStrategy(true, asList("192.168.3.0/24")));
        strategys.add(new FilterStrategy(true, asList("10.88.3.0/24")));

        IpSubnetFilterFactory.Config config = new IpSubnetFilterFactory.Config();
        config.setPreferRejectOnCidrConflict(false);

        boolean result = doTestIpFilter(config, "192.168.3.2", strategys);

        Assertions.assertTrue(result);
    }

    public boolean doTestIpFilter(IpSubnetFilterFactory.Config config, String remoteIp, List<FilterStrategy> strategys) {
        AtomicBoolean acceptedFlag = new AtomicBoolean(false);

        MockServerHttpRequest request = MockServerHttpRequest.get("http://httpbin.org/hello")
                .remoteAddress(InetSocketAddressUtil.createUnresolved(remoteIp, 0))
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
                return Mono.just(strategys);
            }
        });

        GatewayFilter tailFilter = (_exchange, chain) -> {
            System.out.println(">>>>> Accpeted !");
            acceptedFlag.set(true);
            return chain.filter(_exchange);
        };

        GatewayFilter filter = factory.apply(config);
        Mono<Void> mono = new MockGatewayFilterChain(asList(filter, tailFilter)).filter(exchange);

        System.out.println(">>>>> Await ...");
        StepVerifier.create(mono).expectComplete().verify(Duration.ofSeconds(10));

        return acceptedFlag.get();
    }

}
