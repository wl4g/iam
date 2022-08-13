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

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Functional Methods for Certificate handling.
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-10 v3.0.0
 * @since v3.0.0
 */
public abstract class CertificateUtil {
    private static final int SUBALTNAME_DNSNAME = 2;
    private static final int SUBALTNAME_IPADDRESS = 7;

    /**
     * Gets the certificate owner from the certificate chain. That is, the owner
     * of the certificate issued by the CA. at the bottom of the certificate
     * chain. <b>Notice: </b>The incoming certificate must be "one full
     * certificate chain".
     * 
     * @param cerChain
     *            The certificate chain that will be sorted.
     * @return certificate owner.
     */
    public static X509Certificate findOwner(X509Certificate[] cerChain) {
        X509Certificate[] sorted = sort(cerChain);
        return sorted[sorted.length - 1];
    }

    /**
     * Get the certificate Common Name from the certificate chain
     * 
     * @param cert
     * @return
     */
    public static Set<String> getCommonNames(X509Certificate cert) {
        Set<String> names = new HashSet<>();

        // 读取CN
        String subjectDN = cert.getSubjectX500Principal().getName();
        String[] pairs = subjectDN.split(",");
        for (String p : pairs) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals("CN")) {
                names.add(kv[1]);
            }
        }

        // 读取SubjectAlternativeNames
        try {
            Collection<List<?>> collection = cert.getSubjectAlternativeNames();
            if (collection != null) {
                for (List<?> list : collection) {
                    if (list.size() == 2) {
                        Object key = list.get(0);
                        Object value = list.get(1);
                        if (key instanceof Integer && value instanceof String) {
                            int intKey = ((Integer) key).intValue();
                            String strValue = (String) value;
                            if (intKey == SUBALTNAME_DNSNAME || intKey == SUBALTNAME_IPADDRESS) {
                                names.add(strValue);
                            }
                        }
                    }
                }
            }
        } catch (CertificateParsingException e) {
            throw new IllegalArgumentException("can not read AlternativeNames.");
        }

        return names;
    }

    /**
     * Sort the certificate chain. The certificate of the issuer comes first,
     * and the owner comes last. Such as: rootCA > subCA > owner. <b>Notice:
     * </b>The incoming certificate must be "one full certificate chain".
     * 
     * @param cerChain
     *            The certificate chain that will be sorted.
     * @return Sorted certificate chain.
     */
    private static X509Certificate[] sort(X509Certificate[] cerChain) {
        X509Certificate[] chain = new X509Certificate[cerChain.length];
        X509Certificate root = findRootCA(cerChain);
        chain[0] = root;

        for (int i = 1; i < chain.length; i++) {
            X509Certificate parent = chain[i - 1];
            for (X509Certificate child : cerChain) {
                String parentDN = parent.getSubjectX500Principal().getName();
                String childDN = child.getSubjectX500Principal().getName();
                if (parentDN.equals(childDN)) {
                    continue;
                }

                String childIssuerDN = child.getIssuerX500Principal().getName();
                if (parentDN.equals(childIssuerDN)) {
                    chain[i] = child;
                    break;
                }
            }
        }

        return chain;
    }

    /**
     * Returns the root certificate, the self-signed certificate, from the
     * certificate chain. <b>Notice: </b>The incoming certificate must be "one
     * full certificate chain".
     * 
     * @param cerChain
     *            The certificate chain that will be sorted.
     * @return CA Root certificate.
     */
    private static X509Certificate findRootCA(X509Certificate[] cerChain) {
        if (cerChain.length == 1) {
            return cerChain[0];
        }

        for (X509Certificate item : cerChain) {
            String subjectDN = item.getSubjectX500Principal().getName();
            String issuserDN = item.getIssuerX500Principal().getName();
            if (subjectDN.equals(issuserDN)) {
                return item;
            }
        }

        throw new IllegalArgumentException("bad certificate chain: no root CA.");
    }

}