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
package com.wl4g.iam.gateway.trace;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static java.lang.String.valueOf;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.trace.config.TraceProperties;
import com.wl4g.infra.common.codec.Base58;

import reactor.core.publisher.Mono;

/**
 * <p>
 * Comparison of global filter and gateway filter: </br>
 * Speaking of their connection, we know that whether it is a global filter or a
 * gateway filter, they can form a filter chain for interception, and this
 * filter chain is composed of a List<GatewayFilter> collection, which seems to
 * be a combination of GatewayFilters, Has nothing to do with GlobalFilter. In
 * fact, SCG adapts GlobalFilter to GatewayFilter by means of an adapter. We can
 * see this change in the constructor of
 * {@link org.springframework.cloud.gateway.handler.FilteringWebHandler}.
 * </p>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-02 v3.0.0
 * @since v3.0.0
 */
public class SimpleTraceFilter implements GlobalFilter, Ordered {

    private @Autowired TraceProperties traceConfig;
    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public int getOrder() {
        return ORDER_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getRequest().mutate().header(traceConfig.getTraceIdRequestHeader(), generateTraceId()).build();
        return chain.filter(exchange);
    }

    public String generateTraceId() {
        String traceId = valueOf(counter.incrementAndGet()).concat("@").concat(currentTimeMillis() + "");
        return Base58.encodeBase58(traceId.getBytes(UTF_8)).toLowerCase();
    }

    public static final int ORDER_FILTER = Ordered.HIGHEST_PRECEDENCE + 10;
}
