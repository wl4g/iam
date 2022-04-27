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
package com.wl4g.iam.gateway.traffic.config;

import static org.springframework.cloud.gateway.config.HttpClientProperties.Pool.PoolType.DISABLED;
import static org.springframework.cloud.gateway.config.HttpClientProperties.Pool.PoolType.FIXED;

import java.security.cert.X509Certificate;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.cloud.gateway.config.HttpClientProperties;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.wl4g.iam.common.constant.GatewayIAMConstants;
import com.wl4g.iam.gateway.traffic.TrafficImagerFilterFactory;
import com.wl4g.iam.gateway.traffic.config.TrafficProperties.ImagerProperties;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.ProxyProvider;

/**
 * {@link TrafficAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-26 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class TrafficAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = GatewayIAMConstants.CONF_PREFIX_IAM_GATEWAY_TRAFFIC)
    public TrafficProperties trafficProperties() {
        return new TrafficProperties();
    }

    @Bean
    public TrafficImagerFilterFactory trafficImagerFilterFactory(
            TrafficProperties trafficConfig,
            ObjectProvider<List<HttpHeadersFilter>> headersFilters,
            List<HttpClientCustomizer> customizers) {
        return new TrafficImagerFilterFactory(trafficConfig, newTrafficImagerHttpClient(trafficConfig.getImager(), customizers),
                headersFilters);
    }

    /**
     * @see {@link org.springframework.cloud.gateway.config.GatewayAutoConfiguration.NettyConfiguration#gatewayHttpClient}
     */
    @SuppressWarnings("deprecation")
    private HttpClient newTrafficImagerHttpClient(ImagerProperties imagerConfig, List<HttpClientCustomizer> customizers) {
        // configure pool resources
        HttpClientProperties.Pool pool = imagerConfig.getPool();

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
                    if (imagerConfig.getMaxHeaderSize() != null) {
                        // cast to int is ok, since @Max is Integer.MAX_VALUE
                        spec.maxHeaderSize((int) imagerConfig.getMaxHeaderSize().toBytes());
                    }
                    if (imagerConfig.getMaxInitialLineLength() != null) {
                        // cast to int is ok, since @Max is Integer.MAX_VALUE
                        spec.maxInitialLineLength((int) imagerConfig.getMaxInitialLineLength().toBytes());
                    }
                    return spec;
                })
                .tcpConfiguration(tcpClient -> {

                    if (imagerConfig.getConnectTimeout() != null) {
                        tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, imagerConfig.getConnectTimeout());
                    }

                    // configure proxy if proxy host is set.
                    HttpClientProperties.Proxy proxy = imagerConfig.getProxy();

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

        HttpClientProperties.Ssl ssl = imagerConfig.getSsl();
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

        if (imagerConfig.isWiretap()) {
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
