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
package com.wl4g.iam.authc.credential.secure;

import org.apache.shiro.authc.CredentialsException;

import com.wl4g.infra.common.codec.CodecSource;
import com.wl4g.iam.core.authc.IamAuthenticationInfo;

import javax.validation.constraints.NotNull;

/**
 * IAM credentials securer.
 *
 * @author wangl.sir
 * @version v1.0 2019年1月16日
 * @see {@link org.apache.shiro.crypto.hash.DefaultHashService#combine()}
 * @since
 */
public interface CredentialsSecurer {

    /**
     * Encryption credentials
     *
     * @param token
     *            Request principal and credentials token information.
     * @param publicSalt
     *            Current authentication credentials public salt.
     * @return
     */
    default String signature(@NotNull CredentialsToken token, @NotNull CodecSource publicSalt) {
        throw new UnsupportedOperationException();
    }

    /**
     * Validation credentials
     *
     * @param token
     *            Request principal and credentials token information.
     * @param info
     *            Database stored credentials information.
     * @return
     */
    default boolean validate(@NotNull CredentialsToken token, @NotNull IamAuthenticationInfo info)
            throws CredentialsException, RuntimeException {
        throw new UnsupportedOperationException();
    }

}