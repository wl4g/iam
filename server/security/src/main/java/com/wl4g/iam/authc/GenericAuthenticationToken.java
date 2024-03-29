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
package com.wl4g.iam.authc;

import static com.wl4g.infra.common.lang.Assert2.*;

import org.apache.shiro.authc.RememberMeAuthenticationToken;

import com.wl4g.iam.core.authc.ClientRef;
import com.wl4g.iam.crypto.SecureCryptService.CryptKind;
import com.wl4g.iam.verify.SecurityVerifier.VerifyKind;

/**
 * General (Username/Password) authentication token
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2018年11月19日
 * @since
 */
public class GenericAuthenticationToken extends ClientSecretIamAuthenticationToken
        implements RememberMeAuthenticationToken, VerifyAuthenticationToken {
    private static final long serialVersionUID = 8587329689973009598L;

    /**
     * The username principal
     */
    final private String principal;

    /**
     * The password credentials
     */
    final private String credentials;

    /**
     * Whether or not 'rememberMe' should be enabled for the corresponding login
     * attempt; default is <code>false</code>
     */
    final private boolean rememberMe;

    /**
     * User client type.
     */
    final private ClientRef clientRef;

    /**
     * Verification code verifiedToken.
     */
    final private String verifiedToken;

    /**
     * Verifier type.
     */
    final private VerifyKind verifyKind;

    public GenericAuthenticationToken(final String remoteHost, final RedirectInfo redirectInfo, final String principal,
            final String credentials, final CryptKind kind, final String clientSecretKey, final String umidToken,
            final String clientRef, final String verifiedToken, final VerifyKind verifyType) {
        this(remoteHost, redirectInfo, principal, credentials, kind, clientSecretKey, umidToken, clientRef, verifiedToken,
                verifyType, false);
    }

    public GenericAuthenticationToken(final String remoteHost, final RedirectInfo redirectInfo, final String principal,
            final String credentials, final CryptKind secureAlgKind, final String clientSecretKey, String umidToken,
            final String clientRef, final String verifiedToken, final VerifyKind verifyKind, final boolean rememberMe) {
        super(secureAlgKind, clientSecretKey, umidToken, remoteHost, redirectInfo);
        hasTextOf(principal, "principal");
        hasTextOf(credentials, "credentials");
        hasTextOf(clientRef, "clientRef");
        // hasTextOf(verifiedToken, "verifiedToken");
        notNullOf(verifyKind, "verifyKind");
        this.principal = principal;
        this.credentials = credentials;
        this.clientRef = ClientRef.of(clientRef);
        this.verifiedToken = verifiedToken;
        this.verifyKind = verifyKind;
        this.rememberMe = rememberMe;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public boolean isRememberMe() {
        return rememberMe;
    }

    public ClientRef getClientRef() {
        return clientRef;
    }

    @Override
    public String getVerifiedToken() {
        return verifiedToken;
    }

    @Override
    public VerifyKind getVerifyType() {
        return verifyKind;
    }

}