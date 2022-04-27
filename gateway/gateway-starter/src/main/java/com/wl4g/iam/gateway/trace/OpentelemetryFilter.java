//package com.wl4g.iam.gateway.trace;
///*
// * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.wl4g.iam.gateway.trace;
//
//import java.net.URI;
//
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.web.server.ServerWebExchange;
//
//import io.opentelemetry.api.OpenTelemetry;
//import io.opentelemetry.api.trace.Span;
//import io.opentelemetry.api.trace.SpanKind;
//import io.opentelemetry.api.trace.Tracer;
//import io.opentelemetry.context.Context;
//import io.opentelemetry.context.Scope;
//import io.opentelemetry.context.propagation.TextMapPropagator;
//import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
//import reactor.core.publisher.Mono;
//
///**
// * {@link OpentelemetryGlobalFilter}
// * 
// * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
// * @version 2021-09-02 v3.0.0
// * @since v3.0.0
// */
//public class OpentelemetryFilter implements GlobalFilter {
//
//    private OpenTelemetry openTelemetry = null;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        // // TODO
//        // exchange.getRequest().mutate().header("traceId",
//        // java.util.UUID.randomUUID().toString()).build();
//        // return chain.filter(exchange);
//
//        // ServerHttpRequest request = exchange.getRequest();
//        // Span span =
//        // getServerSpan(openTelemetry.getTracer("appConfig.getApplicationName()"),
//        // request);
//        // Scope scope = span.makeCurrent();
//        // exchange.getResponse().getHeaders().add("traceId",
//        // span.getSpanContext().getTraceId());
//        // span.setAttribute("params", request.getQueryParams().toString());
//        //
//        // return chain.filter(exchange).doFinally((signalType) -> {
//        // scope.close();
//        // span.end();
//        // }).doOnError(span::recordException);
//
//        // TODO
//        Span span = getClientSpan(openTelemetry.getTracer("appConfig.getApplicationName()"), exchange);
//        Scope scope = span.makeCurrent();
//        inject(exchange);
//        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
//            scope.close();
//            span.end();
//        }));
//    }
//
//    private void inject(ServerWebExchange exchange) {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        TextMapPropagator textMapPropagator = openTelemetry.getPropagators().getTextMapPropagator();
//        textMapPropagator.inject(Context.current(), httpHeaders, HttpHeaders::add);
//        ServerHttpRequest request = exchange.getRequest().mutate().headers(headers -> headers.addAll(httpHeaders)).build();
//        exchange.mutate().request(request).build();
//    }
//
//    private Span getServerSpan(Tracer tracer, ServerHttpRequest request) {
//        return tracer.spanBuilder(request.getPath().toString())
//                .setNoParent()
//                .setSpanKind(SpanKind.SERVER)
//                .setAttribute(SemanticAttributes.HTTP_METHOD, request.getMethod().name())
//                .startSpan();
//    }
//
//    private Span getClientSpan(Tracer tracer, ServerWebExchange exchange) {
//        ServerHttpRequest request = exchange.getRequest();
//        URI routeUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
//        return tracer.spanBuilder(routeUri.getPath())
//                .setSpanKind(SpanKind.CLIENT)
//                .setAttribute(SemanticAttributes.HTTP_METHOD, request.getMethod().name())
//                .startSpan();
//    }
//
//}
