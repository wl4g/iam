/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import com.wl4g.iam.gateway.ipfilter.config.IpFilterProperties;
import com.wl4g.iam.gateway.ipfilter.config.IpFilterProperties.IPSubnet;
import com.wl4g.iam.gateway.metrics.IamGatewayMetricsFacade;
import com.wl4g.iam.gateway.mock.MockGatewayFilterChain;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * {@link IpSubnetFilterFactoryTests}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-05 v3.0.0
 * @since v3.0.0
 */
public class IpSubnetFilterFactoryTests {

    private IamGatewayMetricsFacade mockMetricsFacade;

    @Before
    public void init() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.application.name", "test-iam-gateway");
        env.setProperty("server.port", "12345");

        this.mockMetricsFacade = new IamGatewayMetricsFacade(new PrometheusMeterRegistry(new PrometheusConfig() {
            @Override
            public String get(String key) {
                return null;
            }
        }), new InetUtils(new InetUtilsProperties()), env);
        mockMetricsFacade.afterPropertiesSet();
    }

    @Test
    public void testAllowedWithAcceptAndNotReject() {
        List<IPSubnet> subnets = new ArrayList<>();
        subnets.add(new IPSubnet(false, asList("192.168.8.0/24")));
        subnets.add(new IPSubnet(true, asList("192.168.3.0/24")));
        subnets.add(new IPSubnet(true, asList("10.88.3.0/24")));

        IpSubnetFilterFactory.Config config = new IpSubnetFilterFactory.Config();
        config.setIPSubnets(subnets);

        boolean allowed = doTestIpFilter(config, "192.168.3.2");

        Assertions.assertTrue(allowed);
    }

    @Test
    public void testAllowedWithAcceptAndRejectAndPreferAcceptOnCidrConflict() {
        List<IPSubnet> subnets = new ArrayList<>();
        subnets.add(new IPSubnet(false, asList("192.168.0.0/16")));
        subnets.add(new IPSubnet(true, asList("192.168.3.0/24")));
        subnets.add(new IPSubnet(true, asList("10.88.3.0/24")));

        IpSubnetFilterFactory.Config config = new IpSubnetFilterFactory.Config();
        config.setPreferRejectOnCidrConflict(false);
        config.setIPSubnets(subnets);

        boolean allowed = doTestIpFilter(config, "192.168.3.2");

        Assertions.assertTrue(allowed);
    }

    @Test
    public void testNotAllowedWithAcceptAndReject() {
        List<IPSubnet> subnets = new ArrayList<>();
        subnets.add(new IPSubnet(false, asList("192.168.0.0/16")));
        subnets.add(new IPSubnet(true, asList("192.168.3.0/24")));
        subnets.add(new IPSubnet(true, asList("10.88.3.0/24")));

        IpSubnetFilterFactory.Config config = new IpSubnetFilterFactory.Config();
        config.setIPSubnets(subnets);

        boolean allowed = doTestIpFilter(config, "192.168.3.2");

        Assertions.assertFalse(allowed);
    }

    boolean doTestIpFilter(IpSubnetFilterFactory.Config config, String remoteIp) {
        AtomicBoolean allowedFlag = new AtomicBoolean(false);

        MockServerHttpRequest request = MockServerHttpRequest.get("http://httpbin.org/hello")
                .remoteAddress(IpSubnetFilterFactory.createInetSocketAddress(remoteIp, 0, false))
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

        IpSubnetFilterFactory factory = new IpSubnetFilterFactory(new IpFilterProperties(), mockMetricsFacade);

        GatewayFilter tailFilter = (_exchange, chain) -> {
            System.out.println(">>>>> Accpeted !");
            allowedFlag.set(true);
            return chain.filter(_exchange);
        };

        GatewayFilter ipFilter = factory.apply(config);
        Mono<Void> mono = new MockGatewayFilterChain(asList(ipFilter, tailFilter)).filter(exchange);

        System.out.println(">>>>> Await ...");
        StepVerifier.create(mono).expectComplete().verify(Duration.ofSeconds(10));

        return allowedFlag.get();
    }

}
