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
package com.wl4g.iam.gateway.traffic;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter.filterRequest;
import static org.springframework.cloud.gateway.support.RouteMetadataUtils.CONNECT_TIMEOUT_ATTR;
import static org.springframework.cloud.gateway.support.RouteMetadataUtils.RESPONSE_TIMEOUT_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.PRESERVE_HOST_HEADER_ATTRIBUTE;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter.Type;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.iam.gateway.traffic.config.TrafficProperties;
import com.wl4g.iam.gateway.traffic.config.TrafficProperties.ImagerProperties;
import com.wl4g.infra.common.bean.ConfigBeanUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

/**
 * {@link TrafficImageGatewayFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-26 v3.0.0
 * @since v3.0.0
 * @see {@link see:org.springframework.cloud.gateway.filter.NettyRoutingFilter}
 */
@Slf4j
public class TrafficImagerFilterFactory extends AbstractGatewayFilterFactory<TrafficImagerFilterFactory.Config> {

    private final TrafficProperties trafficConfig;
    private final HttpClient httpClient;
    private final ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider;
    // do not use this headersFilters directly, use getHeadersFilters() instead.
    private volatile List<HttpHeadersFilter> headersFilters;

    public TrafficImagerFilterFactory(TrafficProperties trafficConfig, HttpClient httpClient,
            ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider) {
        super(TrafficImagerFilterFactory.Config.class);
        this.trafficConfig = notNullOf(trafficConfig, "trafficConfig");
        this.httpClient = notNullOf(httpClient, "httpClient");
        this.headersFiltersProvider = notNullOf(headersFiltersProvider, "headersFiltersProvider");
    }

    @Override
    public String name() {
        return BEAN_NAME;
    }

    @SuppressWarnings("finally")
    @Override
    public GatewayFilter apply(Config config) {
        applyDefaultToConfig(config);
        return (exchange, chain) -> {
            try {
                URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);
                String scheme = requestUrl.getScheme();
                if ((!"http".equals(scheme) && !"https".equals(scheme))) {
                    return chain.filter(exchange);
                }

                // Replication image requests.
                safeList(config.getTargetUris()).forEach(targetUri -> {
                    try {
                        doImageRequest(exchange, targetUri);
                    } catch (Exception e) {
                        log.warn(format("Failed to replication image requests for targetUri: {}", targetUri), e);
                    }
                });
            } finally {
                // see:https://stackoverflow.com/questions/17481251/finally-block-does-not-complete-normally-eclipse-warning
                // Ensure that traffic mirroring request exceptions should not
                // affect real traffic forwarding.
                return chain.filter(exchange);
            }
        };
    }

    private void applyDefaultToConfig(Config config) {
        try {
            ConfigBeanUtils.configureWithDefault(new TrafficImagerFilterFactory.Config(), config, trafficConfig.getImager());
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException("Unable apply defaults to traffic imager gateway config", e);
        }
    }

    private void doImageRequest(ServerWebExchange exchange, String targetUri) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = HttpMethod.valueOf(request.getMethodValue());

        HttpHeaders filtered = filterRequest(getHeadersFilters(), exchange);

        final DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        filtered.forEach(httpHeaders::set);

        boolean preserveHost = exchange.getAttributeOrDefault(PRESERVE_HOST_HEADER_ATTRIBUTE, false);
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);

        Flux<HttpClientResponse> responseFlux = getHttpClient(route, exchange).headers(headers -> {
            headers.add(httpHeaders);
            // Will either be set below, or later by Netty
            headers.remove(HttpHeaders.HOST);
            if (preserveHost) {
                String host = request.getHeaders().getFirst(HttpHeaders.HOST);
                headers.add(HttpHeaders.HOST, host);
            }
        }).request(method).uri(targetUri).send((req, nettyOutbound) -> {
            if (log.isTraceEnabled()) {
                nettyOutbound.withConnection(connection -> log.trace("Image request outbound route: {}, inbound: {}",
                        connection.channel().id().asShortText(), exchange.getLogPrefix()));
            }
            // TODO use copy read???
            return nettyOutbound.send(request.getBody().map(this::getByteBuf));
        }).responseConnection((res, connection) -> {
            // Defer committing the response until all route filters have
            // run Put client response as ServerWebExchange attribute and write
            // response later NettyWriteResponseFilter

            // TODO use print !!
            // exchange.getAttributes().put(CLIENT_RESPONSE_ATTR, res);
            // exchange.getAttributes().put(CLIENT_RESPONSE_CONN_ATTR,connection);

            ServerHttpResponse response = exchange.getResponse();

            // put headers and status so filters can modify the response
            HttpHeaders headers = new HttpHeaders();
            res.responseHeaders().forEach(entry -> headers.add(entry.getKey(), entry.getValue()));

            String contentTypeValue = headers.getFirst(HttpHeaders.CONTENT_TYPE);
            if (StringUtils.hasLength(contentTypeValue)) {
                // TODO use print !!
                // exchange.getAttributes().put(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR,contentTypeValue);
            }

            int statusCode = getResponseStatusCode(res, targetUri);

            // make sure headers filters run after setting status so it is
            // available in response
            HttpHeaders filteredResponseHeaders = HttpHeadersFilter.filter(getHeadersFilters(), headers, exchange, Type.RESPONSE);

            if (!filteredResponseHeaders.containsKey(HttpHeaders.TRANSFER_ENCODING)
                    && filteredResponseHeaders.containsKey(HttpHeaders.CONTENT_LENGTH)) {
                // It is not valid to have both the transfer-encoding header
                // and the content-length header.
                // Remove the transfer-encoding header in the response if
                // the content-length header is present.

                // TODO use print
                // response.getHeaders().remove(HttpHeaders.TRANSFER_ENCODING);
            }

            // TODO use print !!
            // exchange.getAttributes().put(CLIENT_RESPONSE_HEADER_NAMES,filteredResponseHeaders.keySet());
            // response.getHeaders().putAll(filteredResponseHeaders);

            return Mono.just(res);
        });

        Duration responseTimeout = getResponseTimeout(route);
        if (responseTimeout != null) {
            responseFlux = responseFlux
                    .timeout(responseTimeout,
                            Mono.error(new TimeoutException("Response took longer than timeout: " + responseTimeout)))
                    .onErrorMap(TimeoutException.class,
                            th -> new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, th.getMessage(), th));
        }

        // TODO
        responseFlux.subscribe();
        // responseFlux.subscribe(consumer, errorConsumer, completeConsumer);
    }

    private List<HttpHeadersFilter> getHeadersFilters() {
        if (headersFilters == null) {
            headersFilters = headersFiltersProvider.getIfAvailable();
        }
        return headersFilters;
    }

    private ByteBuf getByteBuf(DataBuffer dataBuffer) {
        if (dataBuffer instanceof NettyDataBuffer) {
            NettyDataBuffer buffer = (NettyDataBuffer) dataBuffer;
            return buffer.getNativeBuffer();
        }
        // MockServerHttpResponse creates these
        else if (dataBuffer instanceof DefaultDataBuffer) {
            DefaultDataBuffer buffer = (DefaultDataBuffer) dataBuffer;
            return Unpooled.wrappedBuffer(buffer.getNativeBuffer());
        }
        throw new IllegalArgumentException("Unable to handle DataBuffer of type " + dataBuffer.getClass());
    }

    private int getResponseStatusCode(HttpClientResponse clientResponse, String targetUri) {
        HttpStatus status = HttpStatus.resolve(clientResponse.status().code());
        if (status != null) {
            return status.value();
        }
        log.warn("Unable to resolve status code {} on traffic image response of targetUri: {}", clientResponse.status().code(),
                targetUri);
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    /**
     * Creates a new HttpClient with per route timeout configuration.
     * Sub-classes that override, should call super.getHttpClient() if they want
     * to honor the per route timeout configuration.
     * 
     * @param route
     *            the current route.
     * @param exchange
     *            the current ServerWebExchange.
     * @param chain
     *            the current GatewayFilterChain.
     * @return
     */
    private HttpClient getHttpClient(Route route, ServerWebExchange exchange) {
        Object connectTimeoutAttr = route.getMetadata().get(CONNECT_TIMEOUT_ATTR);
        if (connectTimeoutAttr != null) {
            Integer connectTimeout = getConnectTimeout(connectTimeoutAttr);
            return httpClient
                    .tcpConfiguration((tcpClient) -> tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout));
        }
        return httpClient;
    }

    private Integer getConnectTimeout(Object connectTimeoutAttr) {
        Integer connectTimeout;
        if (connectTimeoutAttr instanceof Integer) {
            connectTimeout = (Integer) connectTimeoutAttr;
        } else {
            connectTimeout = Integer.parseInt(connectTimeoutAttr.toString());
        }
        return connectTimeout;
    }

    private Duration getResponseTimeout(Route route) {
        Object responseTimeoutAttr = route.getMetadata().get(RESPONSE_TIMEOUT_ATTR);
        Long responseTimeout = null;
        if (responseTimeoutAttr != null) {
            if (responseTimeoutAttr instanceof Number) {
                responseTimeout = ((Number) responseTimeoutAttr).longValue();
            } else {
                responseTimeout = Long.valueOf(responseTimeoutAttr.toString());
            }
        }
        return responseTimeout != null ? Duration.ofMillis(responseTimeout) : trafficConfig.getImager().getResponseTimeout();
    }

    @Getter
    @Setter
    @ToString
    public static class Config extends ImagerProperties {
    }

    public static final String BEAN_NAME = "TrafficImager";
}
