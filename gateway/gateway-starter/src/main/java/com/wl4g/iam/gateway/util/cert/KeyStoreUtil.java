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
package com.wl4g.iam.gateway.util.cert;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * {@link KeyStoreUtil}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-10 v3.0.0
 * @since v3.0.0
 */
public abstract class KeyStoreUtil {

    public static KeyStore createKeyStore(String storeName, String storeType, char[] storeValue) {
        if (storeName == null) {
            return null;
        }
        File storeFile = new File(storeName);

        try (FileInputStream in = new FileInputStream(storeFile);) {
            if (storeFile.isFile()) {
                return createKeyStore(in, storeType, storeValue);
            }
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader() == null ? KeyStoreUtil.class.getClassLoader()
                    : Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(storeName);
            if (resource != null) {
                return createKeyStore(resource.openStream(), storeType, storeValue);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Bad key store or value." + e.getMessage());
        }

        return null;
    }

    public static KeyStore createKeyStore(InputStream store, String storeType, char[] storeValue) {
        try (InputStream is = store) {
            KeyStore keystore = KeyStore.getInstance(storeType);
            keystore.load(is, storeValue);
            return keystore;
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad key store or value." + e.getMessage());
        }
    }

    public static List<CRL> createCRL(InputStream crlfile) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection<? extends CRL> crls = cf.generateCRLs(crlfile);
            return crls.stream().map(c -> (CRL) c).collect(toList());
        } catch (CertificateException e) {
            throw new IllegalArgumentException("bad cert file.");
        } catch (CRLException e) {
            throw new IllegalArgumentException("bad crl file.");
        }
    }

    public static KeyManager[] createKeyManagers(final KeyStore keystore, char[] keyvalue) {
        try {
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keystore, keyvalue);
            return kmfactory.getKeyManagers();
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad key store." + e.getMessage());
        }
    }

    public static TrustManager[] createTrustManagers(final KeyStore keystore) {
        try {
            TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmfactory.init(keystore);
            TrustManager[] trustmanagers = tmfactory.getTrustManagers();
            return trustmanagers;
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad trust store." + e.getMessage());
        }
    }

}