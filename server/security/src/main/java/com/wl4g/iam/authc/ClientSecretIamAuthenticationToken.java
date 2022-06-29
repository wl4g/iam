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
package com.wl4g.iam.authc;

import static com.wl4g.infra.common.lang.Assert2.*;

import javax.validation.constraints.NotNull;

import com.wl4g.iam.crypto.SecureCryptService.CryptKind;

/**
 * Client secret IAM authentication token
 *
 * @author James Wong <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月19日
 * @since
 */
public abstract class ClientSecretIamAuthenticationToken extends ServerIamAuthenticationToken {

    private static final long serialVersionUID = 5483061935073949894L;

    /**
     * Iam asymmetric secure crypt algorithm kind definitions..
     */
    @NotNull
    final private CryptKind cryptKind;

    /**
     * The secret key that the client requests for authentication is used to
     * login successfully encrypted additional ticket.
     */
    final private String clientSecretKey;

    /**
     * UMID token.
     */
    final private String umidToken;

    public ClientSecretIamAuthenticationToken(final CryptKind kind, final String clientSecretKey, String umidToken) {
        this(kind, clientSecretKey, umidToken, null);
    }

    public ClientSecretIamAuthenticationToken(final CryptKind kind, final String clientSecretKey, String umidToken,
            final String remoteHost) {
        this(kind, clientSecretKey, umidToken, remoteHost, null);
    }

    public ClientSecretIamAuthenticationToken(final CryptKind kind, final String clientSecretKey, String umidToken,
            final String remoteHost, final RedirectInfo redirectInfo) {
        super(remoteHost, redirectInfo);
        notNullOf(kind, "kind");
        hasTextOf(clientSecretKey, "clientSecretKey");
        hasTextOf(umidToken, "umidToken");
        this.cryptKind = kind;
        this.clientSecretKey = clientSecretKey;
        this.umidToken = umidToken;
    }

    public CryptKind getCryptKind() {
        return cryptKind;
    }

    public String getClientSecretKey() {
        return clientSecretKey;
    }

    public String getUmidToken() {
        return umidToken;
    }

}