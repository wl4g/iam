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
package com.wl4g.iam.gateway.server.config;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CRL;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;

import com.google.common.io.Resources;
import com.wl4g.iam.gateway.util.cert.KeyStoreUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link GatewayWebServerProperties}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-10 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
public class GatewayWebServerProperties {

    private SslServerVerifier sslVerifier = new SslServerVerifier();

    @Getter
    @Setter
    @ToString
    public static class SslServerVerifier {
        private SniVerifier sni = new SniVerifier();
        private PeerVerifier peer = new PeerVerifier();
    }

    @Getter
    @Setter
    @ToString
    public static class SniVerifier {

        private boolean enabled = true;

        private List<String> hosts = new ArrayList<>();
    }

    @Getter
    @Setter
    @ToString
    public static class PeerVerifier {

        private boolean enabled = true;

        private boolean checkCNHost = true;

        private Resource checkCNWhiteFile;

        private Resource checkCrlFile;

        private boolean allowRenegociate = true;
        //
        // Temporary fields.
        //

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private transient List<String> cachedCheckCNWhitelist;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private transient List<CRL> cachedCheckCrls;

        public synchronized List<String> loadCheckCNWhitelist() throws CertificateException {
            try {
                if (isNull(cachedCheckCNWhitelist)) {
                    String white = Resources.toString(getCheckCNWhiteFile().getURL(), UTF_8);
                    this.cachedCheckCNWhitelist = asList(white.split("\\s+"));
                }
                return cachedCheckCNWhitelist;
            } catch (FileNotFoundException e) {
                throw new CertificateException("CN does not match white. no white file.");
            } catch (IOException e) {
                throw new CertificateException("CN does not match white. can not read file.");
            }
        }

        public synchronized List<CRL> loadCrls() throws CertificateException {
            try {
                if (isNull(cachedCheckCrls)) {
                    this.cachedCheckCrls = KeyStoreUtil.createCRL(getCheckCrlFile().getInputStream());
                }
                return cachedCheckCrls;
            } catch (FileNotFoundException e) {
                throw new CertificateException("CN does not match white. no white file.");
            } catch (IOException e) {
                throw new CertificateException("CN does not match white. can not read file.");
            }
        }

    }

}
