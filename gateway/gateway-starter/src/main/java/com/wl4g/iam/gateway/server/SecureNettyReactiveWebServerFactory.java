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
package com.wl4g.iam.gateway.server;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.netty.NettyRouteProvider;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.embedded.netty.NettyWebServer;
import org.springframework.boot.web.reactive.server.AbstractReactiveWebServerFactory;
import org.springframework.boot.web.server.Compression;
import org.springframework.boot.web.server.Shutdown;
import org.springframework.boot.web.server.WebServer;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.util.Assert;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.wl4g.iam.gateway.server.config.GatewayWebServerProperties;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.resources.LoopResources;

/**
 * {@link SecureNettyReactiveWebServerFactory}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-10 v3.0.0
 * @since v3.0.0
 * @see {@link org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext#createWebServer()}
 * @see {@link org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory}
 */
public class SecureNettyReactiveWebServerFactory extends AbstractReactiveWebServerFactory {

    //
    // ADD feature configuration properties.
    //
    private @Autowired GatewayWebServerProperties gatewayWebServerProperties;

    private Set<NettyServerCustomizer> serverCustomizers = new LinkedHashSet<>();
    private List<NettyRouteProvider> routeProviders = new ArrayList<>();
    private Duration lifecycleTimeout;
    private boolean useForwardHeaders;
    private ReactorResourceFactory resourceFactory;
    private Shutdown shutdown;

    public SecureNettyReactiveWebServerFactory() {
    }

    public SecureNettyReactiveWebServerFactory(int port) {
        super(port);
    }

    @Override
    public WebServer getWebServer(HttpHandler httpHandler) {
        HttpServer httpServer = createHttpServer();
        ReactorHttpHandlerAdapter handlerAdapter = new ReactorHttpHandlerAdapter(httpHandler);
        NettyWebServer webServer = createNettyWebServer(httpServer, handlerAdapter, this.lifecycleTimeout, getShutdown());
        webServer.setRouteProviders(this.routeProviders);
        return webServer;
    }

    NettyWebServer createNettyWebServer(
            HttpServer httpServer,
            ReactorHttpHandlerAdapter handlerAdapter,
            Duration lifecycleTimeout,
            Shutdown shutdown) {
        return new NettyWebServer(httpServer, handlerAdapter, lifecycleTimeout, shutdown);
    }

    /**
     * Returns a mutable collection of the {@link NettyServerCustomizer}s that
     * will be applied to the Netty server builder.
     * 
     * @return the customizers that will be applied
     */
    public Collection<NettyServerCustomizer> getServerCustomizers() {
        return this.serverCustomizers;
    }

    /**
     * Set {@link NettyServerCustomizer}s that should be applied to the Netty
     * server builder. Calling this method will replace any existing
     * customizers.
     * 
     * @param serverCustomizers
     *            the customizers to set
     */
    public void setServerCustomizers(Collection<? extends NettyServerCustomizer> serverCustomizers) {
        Assert.notNull(serverCustomizers, "ServerCustomizers must not be null");
        this.serverCustomizers = new LinkedHashSet<>(serverCustomizers);
    }

    /**
     * Add {@link NettyServerCustomizer}s that should applied while building the
     * server.
     * 
     * @param serverCustomizers
     *            the customizers to add
     */
    public void addServerCustomizers(NettyServerCustomizer... serverCustomizers) {
        Assert.notNull(serverCustomizers, "ServerCustomizer must not be null");
        this.serverCustomizers.addAll(Arrays.asList(serverCustomizers));
    }

    /**
     * Add {@link NettyRouteProvider}s that should be applied, in order, before
     * the handler for the Spring application.
     * 
     * @param routeProviders
     *            the route providers to add
     */
    public void addRouteProviders(NettyRouteProvider... routeProviders) {
        Assert.notNull(routeProviders, "NettyRouteProvider must not be null");
        this.routeProviders.addAll(Arrays.asList(routeProviders));
    }

    /**
     * Set the maximum amount of time that should be waited when starting or
     * stopping the server.
     * 
     * @param lifecycleTimeout
     *            the lifecycle timeout
     */
    public void setLifecycleTimeout(Duration lifecycleTimeout) {
        this.lifecycleTimeout = lifecycleTimeout;
    }

    /**
     * Set if x-forward-* headers should be processed.
     * 
     * @param useForwardHeaders
     *            if x-forward headers should be used
     * @since 2.1.0
     */
    public void setUseForwardHeaders(boolean useForwardHeaders) {
        this.useForwardHeaders = useForwardHeaders;
    }

    /**
     * Set the {@link ReactorResourceFactory} to get the shared resources from.
     * 
     * @param resourceFactory
     *            the server resources
     * @since 2.1.0
     */
    public void setResourceFactory(ReactorResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    @Override
    public void setShutdown(Shutdown shutdown) {
        this.shutdown = shutdown;
    }

    @Override
    public Shutdown getShutdown() {
        return this.shutdown;
    }

    private HttpServer createHttpServer() {
        // see:https://developer.aliyun.com/article/319181
        HttpServer server = HttpServer.create();
        if (this.resourceFactory != null) {
            LoopResources resources = this.resourceFactory.getLoopResources();
            Assert.notNull(resources, "No LoopResources: is ReactorResourceFactory not initialized yet?");
            server = server.runOn(resources).bindAddress(this::getListenAddress);
        } else {
            server = server.bindAddress(this::getListenAddress);
        }
        if (getSsl() != null && getSsl().isEnabled()) {
            //
            // Disable for custom.
            //
            // SslServerCustomizer sslServerCustomizer=new
            // SslServerCustomizer(getSsl(), getHttp2(), getSslStoreProvider());
            // server=sslServerCustomizer.apply(server);
            //
            // [Begin] ADD for custom.
            //
            // TODO secureWebServerConfig for injection
            SecureSslServerCustomizer sslServerCustomizer = new SecureSslServerCustomizer(gatewayWebServerProperties, getSsl(),
                    getHttp2(), getSslStoreProvider());
            server = sslServerCustomizer.apply(server);
            //
            // [End] ADD for custom.
            //
        }
        if (getCompression() != null && getCompression().getEnabled()) {
            CompressionCustomizer compressionCustomizer = new CompressionCustomizer(getCompression());
            server = compressionCustomizer.apply(server);
        }
        server = server.protocol(listProtocols()).forwarded(this.useForwardHeaders);
        return applyCustomizers(server);
    }

    private HttpProtocol[] listProtocols() {
        if (getHttp2() != null && getHttp2().isEnabled() && getSsl() != null && getSsl().isEnabled()) {
            return new HttpProtocol[] { HttpProtocol.H2, HttpProtocol.HTTP11 };
        }
        return new HttpProtocol[] { HttpProtocol.HTTP11 };
    }

    private InetSocketAddress getListenAddress() {
        if (getAddress() != null) {
            return new InetSocketAddress(getAddress().getHostAddress(), getPort());
        }
        return new InetSocketAddress(getPort());
    }

    private HttpServer applyCustomizers(HttpServer server) {
        for (NettyServerCustomizer customizer : this.serverCustomizers) {
            server = customizer.apply(server);
        }
        return server;
    }

    /**
     * Configure the HTTP compression on a Reactor Netty request/response
     * handler.
     *
     * @author Stephane Maldini
     * @author Phillip Webb
     * @author Brian Clozel
     * @see {@link org.springframework.boot.web.embedded.netty.CompressionCustomizer}
     */
    static class CompressionCustomizer implements NettyServerCustomizer {
        private static final CompressionPredicate ALWAYS_COMPRESS = (request, response) -> true;

        private final Compression compression;

        CompressionCustomizer(Compression compression) {
            this.compression = compression;
        }

        @Override
        public HttpServer apply(HttpServer server) {
            if (!this.compression.getMinResponseSize().isNegative()) {
                server = server.compress((int) this.compression.getMinResponseSize().toBytes());
            }
            CompressionPredicate mimeTypes = getMimeTypesPredicate(this.compression.getMimeTypes());
            CompressionPredicate excludedUserAgents = getExcludedUserAgentsPredicate(this.compression.getExcludedUserAgents());
            server = server.compress(mimeTypes.and(excludedUserAgents));
            return server;
        }

        private CompressionPredicate getMimeTypesPredicate(String[] mimeTypeValues) {
            if (ObjectUtils.isEmpty(mimeTypeValues)) {
                return ALWAYS_COMPRESS;
            }
            List<MimeType> mimeTypes = Arrays.stream(mimeTypeValues).map(MimeTypeUtils::parseMimeType).collect(
                    Collectors.toList());
            return (request, response) -> {
                String contentType = response.responseHeaders().get(HttpHeaderNames.CONTENT_TYPE);
                if (StringUtils.hasLength(contentType)) {
                    return false;
                }
                try {
                    MimeType contentMimeType = MimeTypeUtils.parseMimeType(contentType);
                    return mimeTypes.stream().anyMatch((candidate) -> candidate.isCompatibleWith(contentMimeType));
                } catch (InvalidMimeTypeException ex) {
                    return false;
                }
            };
        }

        private CompressionPredicate getExcludedUserAgentsPredicate(String[] excludedUserAgents) {
            if (ObjectUtils.isEmpty(excludedUserAgents)) {
                return ALWAYS_COMPRESS;
            }
            return (request, response) -> {
                HttpHeaders headers = request.requestHeaders();
                return Arrays.stream(excludedUserAgents)
                        .noneMatch((candidate) -> headers.contains(HttpHeaderNames.USER_AGENT, candidate, true));
            };
        }
    }

    private interface CompressionPredicate extends BiPredicate<HttpServerRequest, HttpServerResponse> {
    }
}
