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
package com.wl4g.iam.gateway.server;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findMethod;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.invokeMethod;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.makeAccessible;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.cert.CRL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.embedded.netty.SslServerCustomizer;
import org.springframework.boot.web.server.Http2;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.SslStoreProvider;

import com.google.common.io.Resources;
import com.wl4g.iam.gateway.server.config.GatewayWebServerProperties;
import com.wl4g.iam.gateway.util.cert.CertificateUtil;
import com.wl4g.iam.gateway.util.cert.KeyStoreUtil;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.internal.tcnative.CertificateVerifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.SslProvider;
import reactor.netty.tcp.SslProvider.Builder;

/**
 * {@link SecureWebServerFactoryCustomizer}
 * 
 * https://www.saoniuhuo.com/article/detail-374589.html </br>
 * </br>
 * </br>
 * https://github.com/apache/servicecomb-java-chassis/blob/master/foundations/foundation-ssl/src/main/java/org/apache/servicecomb/foundation/ssl/TrustManagerExt.java#L168
 * </br>
 * </br>
 * https://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/7fcf35286d52/src/share/classes/javax/net/ssl/SNIMatcher.java
 * </br>
 * </br>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-09 v3.0.0
 * @since v3.0.0
 * @see https://github.com/netty/netty/issues/8537
 * @see {@link io.netty.handler.ssl.ReferenceCountedOpenSslServerContext#newSessionContext()}
 * @see {@link io.netty.handler.ssl.ReferenceCountedOpenSslServerContext#setVerifyCallback()}
 * @see {@link io.netty.handler.ssl.Java8SslUtils#checkSniHostnameMatch()}
 */
@SuppressWarnings("unused")
public class SecureSslServerCustomizer extends SslServerCustomizer {

    private final GatewayWebServerProperties secureWebServerConfig;

    public SecureSslServerCustomizer(GatewayWebServerProperties secureWebServerConfig, Ssl ssl, Http2 http2,
            SslStoreProvider sslStoreProvider) {
        super(ssl, http2, sslStoreProvider);
        this.secureWebServerConfig = notNullOf(secureWebServerConfig, "secureWebServerConfig");
    }

    @Override
    public HttpServer apply(HttpServer server) {
        try {
            return server.secure((contextSpec) -> {
                SslContextBuilder contextBuilder = getContextBuilder()
                        // see:io.netty.handler.ssl.SslContext#newServerContextInternal()
                        // see:io.netty.handler.ssl.ReferenceCountedOpenSslServerContext#newSessionContext()
                        // see:io.netty.handler.ssl.ReferenceCountedOpenSslServerContext#setVerifyCallback()
                        .sslProvider(io.netty.handler.ssl.SslProvider.OPENSSL_REFCNT);

                SslProvider.DefaultConfigurationSpec spec = contextSpec.sslContext(contextBuilder);
                if (getHttp2() != null && getHttp2().isEnabled()) {
                    Builder builder = spec.defaultConfiguration(SslProvider.DefaultConfigurationType.H2);
                    //
                    // [Begin] ADD for SSL subject verifier.
                    //
                    if (secureWebServerConfig.getSslVerifier().getSni().isEnabled()) {
                        // see:io.netty.handler.ssl.Java8SslUtils#checkSniHostnameMatch()
                        builder.handlerConfigurator(handler -> {
                            SSLEngine engine = handler.engine();

                            // TODO

                            SSLSessionContext sessionContext = engine.getSession().getSessionContext();

                            // refer-to:https://www.saoniuhuo.com/article/detail-374589.html
                            // refer-to:https://github.com/apache/servicecomb-java-chassis/blob/master/foundations/foundation-ssl/src/main/java/org/apache/servicecomb/foundation/ssl/TrustManagerExt.java#L168
                            // refer-to:https://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/7fcf35286d52/src/share/classes/javax/net/ssl/SNIMatcher.java
                            String peerHost = engine.getPeerHost();
                            int peerPort = engine.getPeerPort();
                            SSLSession session = engine.getSession();
                            SSLSession handshakeSession = engine.getHandshakeSession();
                            if (nonNull(handshakeSession)) {
                                String peerHost2 = handshakeSession.getPeerHost();
                            }

                            SSLParameters params = new SSLParameters();
                            List<SNIMatcher> matchers = new LinkedList<>();
                            matchers.add(new SNIMatcher(0) {
                                @Override
                                public boolean matches(SNIServerName serverName) {
                                    String servname = new String(serverName.getEncoded());
                                    return safeList(secureWebServerConfig.getSslVerifier().getSni().getHosts()).stream()
                                            .anyMatch(h -> StringUtils.equals(h, servname));
                                }
                            });
                            params.setSNIMatchers(matchers);
                            engine.setSSLParameters(params);
                        });
                        builder.build();
                    }
                    //
                    // [End] ADD for SSL subject verifier.
                    //
                }
            });
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Ssl getSsl() {
        return getField(findField(SslServerCustomizer.class, "ssl", Ssl.class), this, true);
    }

    private Http2 getHttp2() {
        return getField(findField(SslServerCustomizer.class, "http2", Http2.class), this, true);
    }

    private SslStoreProvider getSslStoreProvider() {
        return getField(findField(SslServerCustomizer.class, "sslStoreProvider", SslStoreProvider.class), this, true);
    }

    @Override
    protected TrustManagerFactory getTrustManagerFactory(Ssl ssl, SslStoreProvider sslStoreProvider) {
        TrustManagerFactory tmf = super.getTrustManagerFactory(ssl, sslStoreProvider);
        return SecureX509TrustManagerFactory.wrap(secureWebServerConfig, tmf);
    }

    /**
     * io.netty.handler.ssl.util.InsecureTrustManagerFactory
     */
    public static class SecureX509TrustManagerFactory extends TrustManagerFactory {
        protected final TrustManagerFactorySpi factorySpi;
        protected final Provider provider;
        protected final String algorithm;

        protected SecureX509TrustManagerFactory(TrustManagerFactorySpi factorySpi, Provider provider, String algorithm) {
            super(factorySpi, provider, algorithm);
            this.factorySpi = factorySpi;
            this.provider = provider;
            this.algorithm = algorithm;
        }

        public static SecureX509TrustManagerFactory wrap(
                GatewayWebServerProperties secureWebServerConfig,
                TrustManagerFactory tmf) {
            TrustManagerFactorySpi originalFactorySpi = getField(
                    findField(tmf.getClass(), "factorySpi", TrustManagerFactorySpi.class), tmf, true);

            SecureTrustManagerFactorySpi factorySpi = new SecureTrustManagerFactorySpi(originalFactorySpi,
                    new SecureX509TrustManager(secureWebServerConfig, (X509ExtendedTrustManager) tmf.getTrustManagers()[0]));

            Provider provider = getField(findField(tmf.getClass(), "provider", Provider.class), tmf, true);
            String algorithm = getField(findField(tmf.getClass(), "algorithm", String.class), tmf, true);
            return new SecureX509TrustManagerFactory(factorySpi, provider, algorithm);
        }
    }

    @AllArgsConstructor
    public static class SecureTrustManagerFactorySpi extends TrustManagerFactorySpi {
        private static final Method engineInit1Method = findMethod(TrustManagerFactorySpi.class, "engineInit", KeyStore.class);
        private static final Method engineInit2Method = findMethod(TrustManagerFactorySpi.class, "engineInit",
                ManagerFactoryParameters.class);

        protected final TrustManagerFactorySpi factorySpi;
        protected final SecureX509TrustManager tm;

        @Override
        protected void engineInit(KeyStore ks) throws KeyStoreException {
            makeAccessible(engineInit1Method);
            invokeMethod(engineInit1Method, factorySpi, ks);
        }

        @Override
        protected void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
            makeAccessible(engineInit2Method);
            invokeMethod(engineInit2Method, factorySpi, spec);
        }

        @Override
        protected TrustManager[] engineGetTrustManagers() {
            return new TrustManager[] { new X509ExtendedTrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return tm.getAcceptedIssuers();
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    tm.checkClientTrusted(chain, authType);
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
                        throws CertificateException {
                    tm.checkClientTrusted(chain, authType, socket);
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                        throws CertificateException {
                    tm.checkClientTrusted(chain, authType, engine);
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    tm.checkServerTrusted(chain, authType);
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
                        throws CertificateException {
                    tm.checkServerTrusted(chain, authType, socket);
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                        throws CertificateException {
                    tm.checkServerTrusted(chain, authType, engine);
                }
            } };
        }
    }

    /**
     * Thanks to servicecomb's verification mechanism refer to:
     * https://github.com/apache/servicecomb-java-chassis/blob/master/foundations/foundation-ssl/src/main/java/org/apache/servicecomb/foundation/ssl/TrustManagerExt.java#L168
     */
    @Slf4j
    @AllArgsConstructor
    public static class SecureX509TrustManager extends X509ExtendedTrustManager {
        private final GatewayWebServerProperties config;
        private final X509ExtendedTrustManager tm;

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (!config.getSslVerifier().getPeer().isEnabled()) {
                return;
            }
            checkTrustedCustom(chain, null);
            tm.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            if (!config.getSslVerifier().getPeer().isEnabled()) {
                return;
            }
            String ip = null;
            if (socket != null && socket.isConnected() && socket instanceof SSLSocket) {
                InetAddress inetAddress = socket.getInetAddress();
                if (inetAddress != null) {
                    ip = inetAddress.getHostAddress();
                }
            }
            checkTrustedCustom(chain, ip);
            tm.checkClientTrusted(chain, authType, socket);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            if (!config.getSslVerifier().getPeer().isEnabled()) {
                return;
            }
            String ip = null;
            if (engine != null) {
                SSLSession session = engine.getHandshakeSession();
                ip = session.getPeerHost();
            }
            checkTrustedCustom(chain, ip);
            tm.checkClientTrusted(chain, authType, engine);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if (!config.getSslVerifier().getPeer().isEnabled()) {
                return;
            }
            checkTrustedCustom(chain, null);
            tm.checkServerTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            if (!config.getSslVerifier().getPeer().isEnabled()) {
                return;
            }
            String ip = null;
            if (nonNull(socket) && socket.isConnected() && socket instanceof SSLSocket) {
                InetAddress inetAddress = socket.getInetAddress();
                if (nonNull(inetAddress)) {
                    ip = inetAddress.getHostAddress();
                }
            }
            checkTrustedCustom(chain, ip);
            tm.checkServerTrusted(chain, authType, socket);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            if (!config.getSslVerifier().getPeer().isEnabled()) {
                return;
            }
            String ip = null;
            if (nonNull(engine)) {
                SSLSession session = engine.getHandshakeSession();
                ip = session.getPeerHost();
            }
            checkTrustedCustom(chain, ip);
            tm.checkServerTrusted(chain, authType, engine);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return tm.getAcceptedIssuers();
        }

        private void checkTrustedCustom(X509Certificate[] chain, String ip) throws CertificateException {
            checkCNHost(chain, ip);
            checkCNWhite(chain);
            checkCRL(chain);
        }

        private void checkCNHost(X509Certificate[] chain, String ip) throws CertificateException {
            if (config.getSslVerifier().getPeer().isCheckCNHost()) {
                X509Certificate owner = CertificateUtil.findOwner(chain);
                Set<String> cns = CertificateUtil.getCommonNames(owner);
                // ip = isBlank(ip) ? custom.getHost() : ip;
                // The request from the local machine, as long as the CN matches
                // any IP address of the local machine.
                if (equalsAnyIgnoreCase(ip, "127.0.0.1", "0:0:0:0:0:0:0:1", "::")) {
                    try {
                        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                        if (interfaces != null) {
                            while (interfaces.hasMoreElements()) {
                                NetworkInterface nif = interfaces.nextElement();
                                Enumeration<InetAddress> ias = nif.getInetAddresses();
                                while (ias.hasMoreElements()) {
                                    InetAddress ia = ias.nextElement();
                                    String local = ia.getHostAddress();
                                    if (cnValid(cns, local)) {
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (SocketException e) {
                        throw new CertificateException("Failed to get local adrresses.");
                    }
                } else if (cnValid(cns, ip)) {
                    return;
                }
                throw new CertificateException(format("Illegal client, IP does not match certificate. CNs=%s, IP=%s", cns, ip));
            }
        }

        private boolean cnValid(Set<String> certsCN, String srcCN) {
            for (String cert : certsCN) {
                if (StringUtils.equals(cert, srcCN)) {
                    return true;
                }
            }
            return false;
        }

        private void checkCNWhite(X509Certificate[] certChain) throws CertificateException {
            if (nonNull(config.getSslVerifier().getPeer().getCheckCNWhiteFile())) {
                List<String> cns = config.getSslVerifier().getPeer().loadCheckCNWhitelist();
                Set<String> certCN = CertificateUtil.getCommonNames(CertificateUtil.findOwner(certChain));
                for (String cn : cns) {
                    if (cnValid(certCN, cn)) {
                        return;
                    }
                }
                throw new CertificateException(format("CN does not match white. CNs=%s", cns));
            }
        }

        private void checkCRL(X509Certificate[] certChain) throws CertificateException {
            if (nonNull(config.getSslVerifier().getPeer().getCheckCrlFile())) {
                List<CRL> crls = config.getSslVerifier().getPeer().loadCrls();
                X509Certificate owner = CertificateUtil.findOwner(certChain);
                for (CRL c : crls) {
                    if (c.isRevoked(owner)) {
                        throw new CertificateException(format("Certificates %s has been revoked.", asList(certChain)));
                    }
                }
            }
        }
    }

}