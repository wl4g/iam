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
package com.wl4g.iam.gateway.util.http;

import static org.springframework.cloud.gateway.config.HttpClientProperties.Pool.PoolType.DISABLED;
import static org.springframework.cloud.gateway.config.HttpClientProperties.Pool.PoolType.FIXED;

import java.security.cert.X509Certificate;
import java.util.List;

import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.cloud.gateway.config.HttpClientProperties;
import org.springframework.cloud.gateway.config.HttpClientProperties.Pool;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.CustomLog;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.ProxyProvider;

/**
 * {@link ReactiveHttpClientBuilder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-29 v3.0.0
 * @since v3.0.0
 */
@CustomLog
public abstract class ReactiveHttpClientBuilder {

    /**
     * Build a reactive based http client.
     * 
     * @see refer to:
     *      {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration.NettyConfiguration#gatewayHttpClient}
     */
    @SuppressWarnings("deprecation")
    public static HttpClient build(HttpClientProperties clientConfig, List<HttpClientCustomizer> customizers) {
        Pool pool = clientConfig.getPool();
        ConnectionProvider connectionProvider;
        if (pool.getType() == DISABLED) {
            connectionProvider = ConnectionProvider.newConnection();
        } else if (pool.getType() == FIXED) {
            connectionProvider = ConnectionProvider.fixed(pool.getName(), pool.getMaxConnections(), pool.getAcquireTimeout(),
                    pool.getMaxIdleTime(), pool.getMaxLifeTime());
        } else {
            connectionProvider = ConnectionProvider.elastic(pool.getName(), pool.getMaxIdleTime(), pool.getMaxLifeTime());
        }

        HttpClient httpClient = HttpClient.create(connectionProvider)
                // TODO: move customizations to HttpClientCustomizers
                .httpResponseDecoder(spec -> {
                    if (clientConfig.getMaxHeaderSize() != null) {
                        // cast to int is ok, since @Max is Integer.MAX_VALUE
                        spec.maxHeaderSize((int) clientConfig.getMaxHeaderSize().toBytes());
                    }
                    if (clientConfig.getMaxInitialLineLength() != null) {
                        // cast to int is ok, since @Max is Integer.MAX_VALUE
                        spec.maxInitialLineLength((int) clientConfig.getMaxInitialLineLength().toBytes());
                    }
                    return spec;
                })
                .tcpConfiguration(tcpClient -> {
                    if (clientConfig.getConnectTimeout() != null) {
                        tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeout());
                    }
                    // configure proxy if proxy host is set.
                    HttpClientProperties.Proxy proxy = clientConfig.getProxy();

                    if (StringUtils.hasText(proxy.getHost())) {
                        tcpClient = tcpClient.proxy(proxySpec -> {
                            ProxyProvider.Builder builder = proxySpec.type(ProxyProvider.Proxy.HTTP).host(proxy.getHost());
                            PropertyMapper map = PropertyMapper.get();

                            map.from(proxy::getPort).whenNonNull().to(builder::port);
                            map.from(proxy::getUsername).whenHasText().to(builder::username);
                            map.from(proxy::getPassword).whenHasText().to(password -> builder.password(s -> password));
                            map.from(proxy::getNonProxyHostsPattern).whenHasText().to(builder::nonProxyHosts);
                        });
                    }
                    return tcpClient;
                });

        HttpClientProperties.Ssl ssl = clientConfig.getSsl();
        if ((ssl.getKeyStore() != null && ssl.getKeyStore().length() > 0)
                || ssl.getTrustedX509CertificatesForTrustManager().length > 0 || ssl.isUseInsecureTrustManager()) {
            httpClient = httpClient.secure(sslContextSpec -> {
                // configure ssl
                SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();

                X509Certificate[] trustedX509Certificates = ssl.getTrustedX509CertificatesForTrustManager();
                if (trustedX509Certificates.length > 0) {
                    sslContextBuilder = sslContextBuilder.trustManager(trustedX509Certificates);
                } else if (ssl.isUseInsecureTrustManager()) {
                    sslContextBuilder = sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                }

                try {
                    sslContextBuilder = sslContextBuilder.keyManager(ssl.getKeyManagerFactory());
                } catch (Exception e) {
                    log.error("", e);
                }
                sslContextSpec.sslContext(sslContextBuilder)
                        .defaultConfiguration(ssl.getDefaultConfigurationType())
                        .handshakeTimeout(ssl.getHandshakeTimeout())
                        .closeNotifyFlushTimeout(ssl.getCloseNotifyFlushTimeout())
                        .closeNotifyReadTimeout(ssl.getCloseNotifyReadTimeout());
            });
        }

        if (clientConfig.isWiretap()) {
            httpClient = httpClient.wiretap(true);
        }

        if (!CollectionUtils.isEmpty(customizers)) {
            customizers.sort(AnnotationAwareOrderComparator.INSTANCE);
            for (HttpClientCustomizer customizer : customizers) {
                httpClient = customizer.customize(httpClient);
            }
        }
        return httpClient;
    }

}
