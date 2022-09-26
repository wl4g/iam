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
package com.wl4g.iam.gateway.trace;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;

import java.lang.reflect.Field;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.http.HttpServerHandler;
import org.springframework.cloud.sleuth.instrument.web.TraceWebFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.wl4g.iam.gateway.trace.config.GrayTraceProperties;
import com.wl4g.iam.gateway.util.IamGatewayUtil;
import com.wl4g.infra.context.utils.web.ReactiveRequestExtractor;
import com.wl4g.infra.context.web.matcher.SpelRequestMatcher;

import lombok.CustomLog;
import reactor.core.publisher.Mono;

/**
 * {@link GrayTraceWebFilter}
 * 
 * <p>
 * Comparison of {@link org.springframework.cloud.gateway.filter.GlobalFilter}
 * and {@link org.springframework.cloud.gateway.filter.GatewayFilter}: </br>
 * Speaking of their connection, we know that whether it is a global filter or a
 * gateway filter, they can form a filter chain for interception, and this
 * filter chain is composed of a List<GatewayFilter> collection, which seems to
 * be a combination of GatewayFilters, Has nothing to do with
 * {@linkplain GlobalFilter}. In fact, SCG adapts GlobalFilter to GatewayFilter
 * by means of an adapter. We can see this change in the constructor of
 * {@link org.springframework.cloud.gateway.handler.FilteringWebHandler}.
 * </p>
 * 
 * <p>
 * and comparison of {@link org.springframework.web.server.WebFilter}: The web
 * filter is also global and will be executed regardless of whether the route
 * matches or not. This is the difference from the {@linkplain GlobalFilter}.
 * The execution order is the earliest.
 * </p>
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
@CustomLog
public class GrayTraceWebFilter extends TraceWebFilter {

    private final GrayTraceProperties grayTraceConfig;
    private final SpelRequestMatcher requestMatcher;

    public GrayTraceWebFilter(GrayTraceProperties grayTraceConfig, Tracer tracer, HttpServerHandler handler,
            CurrentTraceContext currentTraceContext) {
        super(tracer, handler, currentTraceContext);
        this.grayTraceConfig = notNullOf(grayTraceConfig, "grayTraceConfig");
        // Build canary request matcher.
        this.requestMatcher = new SpelRequestMatcher(grayTraceConfig.getPreferMatchRuleDefinitions());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!isTraceRequest(exchange)) {
            if (log.isDebugEnabled()) {
                ServerHttpRequest request = exchange.getRequest();
                log.debug("Not to meet the conditional rule to enable tracing. - uri: {}, headers: {}, queryParams: {}",
                        request.getURI(), request.getHeaders(), request.getQueryParams());
            }
            return chain.filter(exchange);
        }

        /**
         * see;{@link org.springframework.cloud.sleuth.otel.bridge.EventPublishingContextWrapper#apply(ContextStorage)}↓
         * see;{@link org.springframework.cloud.sleuth.otel.bridge.Slf4jBaggageApplicationListener#onScopeAttached(ScopeAttachedEvent)}↓
         * see:{@link io.opentelemetry.api.baggage.BaggageContextKey.KEY}↓
         */
        return exchange.getPrincipal().defaultIfEmpty(IamGatewayUtil.UNKNOWN_PRINCIPAL).flatMap(principal -> {
            getTracer().getBaggage("principal").set(principal.getName());
            return Mono.justOrEmpty(principal);
        }).then(super.filter(exchange, chain));
    }

    /**
     * Check if enable tracking needs to be filtered.
     * 
     * @param exchange
     * @return
     */
    protected boolean isTraceRequest(ServerWebExchange exchange) {
        if (!grayTraceConfig.isEnabled()) {
            return false;
        }
        return requestMatcher.matches(new ReactiveRequestExtractor(exchange.getRequest()),
                grayTraceConfig.getPreferOpenMatchExpression());
    }

    protected Tracer getTracer() {
        return getField(TRACER_FIELD, this, true);
    }

    public static final Field TRACER_FIELD = findField(TraceWebFilter.class, "tracer", Tracer.class);

}
