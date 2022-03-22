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
package com.wl4g.iam.authc.credential.secure;

import static com.wl4g.infra.common.lang.Assert2.*;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.wl4g.iam.crypto.SecureCryptService.CryptKind;

/**
 * Final credentials token
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年4月6日
 * @since
 */
public final class CredentialsToken {

    /**
     * Request authentication principal.
     */
    @NotBlank
    private final String principal;

    /**
     * Request authentication credentials.
     */
    @NotBlank
    private final String credentials;

    /**
     * Iam asymmetric secure crypt algorithm kind definitions..
     */
    @NotNull
    private final CryptKind kind;

    /**
     * Whether the tag has resolved the encrypted password passed from the front
     * end.
     */
    private final boolean isSolved;

    public CredentialsToken(CredentialsToken token) {
        this(token.getPrincipal(), token.getCredentials(), token.getKind());
    }

    public CredentialsToken(@NotBlank String principal, @NotBlank String credentials, @NotNull CryptKind kind) {
        this(principal, credentials, kind, false);
    }

    public CredentialsToken(@NotBlank String principal, @NotBlank String credentials, @NotNull CryptKind kind, boolean isSolved) {
        this.principal = hasTextOf(principal, "principal");
        this.credentials = hasTextOf(credentials, "credentials");
        this.kind = notNullOf(kind, "kind");
        this.isSolved = isSolved;
    }

    final public String getPrincipal() {
        return principal;
    }

    final public String getCredentials() {
        return credentials;
    }

    public CryptKind getKind() {
        return kind;
    }

    final public boolean isSolved() {
        return isSolved;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
    }

}