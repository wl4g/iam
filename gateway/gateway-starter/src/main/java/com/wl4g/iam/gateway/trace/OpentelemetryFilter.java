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

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;

import java.net.URI;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.base.Predicates;
import com.wl4g.iam.gateway.logging.AbstractDyeingLoggingFilter;
import com.wl4g.iam.gateway.trace.config.TraceProperties;
import com.wl4g.iam.gateway.util.IamGatewayUtil;
import com.wl4g.infra.core.constant.CoreInfraConstants;
import com.wl4g.infra.core.utils.web.ReactiveRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link OpentelemetryGlobalFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class OpentelemetryFilter implements GlobalFilter, Ordered {

    private final TraceProperties traceConfig;
    private final OpenTelemetry openTelemetry;
    private final SpelRequestMatcher requestMatcher;

    public OpentelemetryFilter(TraceProperties traceConfig, OpenTelemetry openTelemetry) {
        this.traceConfig = notNullOf(traceConfig, "traceConfig");
        this.openTelemetry = notNullOf(openTelemetry, "openTelemetry");
        this.requestMatcher = new SpelRequestMatcher(traceConfig.getPreferMatchRuleDefinitions());
    }

    /**
     * @see {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#loadFilters()}
     */
    @Override
    public int getOrder() {
        return ORDER_FILTER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!isTraceRequest(exchange)) {
            if (log.isDebugEnabled()) {
                log.debug("Not to meet the conditional rule to enable tracing. - headers: {}, queryParams: {}",
                        exchange.getRequest().getURI(), exchange.getRequest().getQueryParams());
            }
            return chain.filter(exchange);
        }

        Tracer tracer = openTelemetry.getTracer(traceConfig.getServiceName());
        return buildTraceSpan(tracer, exchange).flatMap(span -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            String traceId = span.getSpanContext().getTraceId();

            request.mutate().header(CoreInfraConstants.TRACE_REQUEST_ID_HEADER, traceId).build();
            response.getHeaders().add(CoreInfraConstants.TRACE_REQUEST_ID_HEADER, traceId);

            span.setAttribute(TRACE_TAG_PARAMETERS, request.getQueryParams().toString());

            Scope scope = span.makeCurrent();
            inject(exchange);
            return chain.filter(exchange).doFinally(signal -> {
                scope.close();
                span.end();
            }).doOnError(span::recordException);
        });
    }

    /**
     * Check if enable tracking needs to be filtered.
     * 
     * @param exchange
     * @return
     */
    protected boolean isTraceRequest(ServerWebExchange exchange) {
        if (!traceConfig.isEnabled()) {
            return false;
        }
        // Gets current request route.
        Route route = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

        // Add routeId temporary predicates.
        Map<String, Supplier<Predicate<String>>> routeIdPredicateSupplier = singletonMap(VAR_ROUTE_ID,
                () -> Predicates.equalTo(route.getId()));

        return requestMatcher.matches(new ReactiveRequestExtractor(exchange.getRequest()),
                traceConfig.getPreferOpenMatchExpression(), routeIdPredicateSupplier);
    }

    protected void inject(ServerWebExchange exchange) {
        HttpHeaders traceHeaders = new HttpHeaders();

        TextMapPropagator propagator = openTelemetry.getPropagators().getTextMapPropagator();
        propagator.inject(Context.current(), traceHeaders, HttpHeaders::add);

        ServerHttpRequest request = exchange.getRequest().mutate().headers(headers -> headers.addAll(traceHeaders)).build();
        exchange.mutate().request(request).build();
    }

    public static Mono<Span> buildTraceSpan(Tracer tracer, ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        return exchange.getPrincipal().defaultIfEmpty(IamGatewayUtil.UNKNOWN_PRINCIPAL).flatMap(principal -> {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            String routeId = isNull(route) ? "Unknown" : route.getId();
            URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            String path = isNull(uri) ? "Unknown" : uri.getPath();

            Span span = tracer.spanBuilder(path)
                    .setNoParent()
                    // .setSpanKind(SpanKind.SERVER)
                    .setSpanKind(SpanKind.CLIENT)
                    .setAttribute(TRACE_TAG_ROUTEID, routeId)
                    .setAttribute(TRACE_TAG_PRINCIPAL, principal.getName())
                    .setAttribute(TRACE_TAG_PARAMETERS, request.getQueryParams().toString())
                    .setAttribute(SemanticAttributes.HTTP_METHOD, request.getMethod().name())
                    .startSpan();
            return Mono.just(span);
        });
    }

    // for logging print traceId.
    public static final int ORDER_FILTER = AbstractDyeingLoggingFilter.ORDER_FILTER - 10;

    public static final String VAR_ROUTE_ID = "routeId";
    public static final String TRACE_TAG_ROUTEID = "routeId";
    public static final String TRACE_TAG_PRINCIPAL = "principal";
    public static final String TRACE_TAG_PARAMETERS = "parameters";

}
