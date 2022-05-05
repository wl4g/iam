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
package com.wl4g.iam.gateway.mock;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class MockGatewayFilterChain implements GatewayFilterChain {

    private final int index;
    private final List<GatewayFilter> filters;

    public MockGatewayFilterChain(List<GatewayFilter> filters) {
        this.filters = filters;
        this.index = 0;
    }

    private MockGatewayFilterChain(MockGatewayFilterChain parent, int index) {
        this.filters = parent.getFilters();
        this.index = index;
    }

    public List<GatewayFilter> getFilters() {
        return filters;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange) {
        return Mono.defer(() -> {
            if (this.index < filters.size()) {
                GatewayFilter filter = filters.get(this.index);
                MockGatewayFilterChain chain = new MockGatewayFilterChain(this, this.index + 1);
                return filter.filter(exchange, chain);
            } else {
                return Mono.empty(); // complete
            }
        });
    }

}
