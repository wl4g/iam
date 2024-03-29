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
package com.wl4g.iam.crypto;

import com.wl4g.infra.common.crypto.asymmetric.DSACryptor;
import com.wl4g.infra.common.crypto.asymmetric.spec.DSAKeyPairSpec;
import com.wl4g.infra.common.locks.JedisLockManager;

/**
 * DSA cryptographic service.
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2019-08-30
 * @since
 */
public final class DSASecureCryptService extends AbstractAymmetricSecureCryptService<DSAKeyPairSpec> {

    public DSASecureCryptService(JedisLockManager lockManager) {
        super(lockManager, new DSACryptor());
    }

    @Override
    public CryptKind kind() {
        return CryptKind.DSA;
    }

}