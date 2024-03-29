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
package com.wl4g.iam.core.crypto;

import static com.wl4g.infra.common.codec.CodecSource.*;

import com.wl4g.infra.common.codec.CodecSource;
import com.wl4g.infra.common.crypto.symmetric.AES128ECBPKCS5;

/**
 * {@link AesIamCipherService}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020年3月29日 v1.0.0
 * @see
 */
public class AesIamCipherService extends AbstractSymmetricCipherService {

    @Override
    public CipherCryptKind kind() {
        return CipherCryptKind.AES;
    }

    @Override
    public String encrypt(byte[] key, String plaintext) {
        return new AES128ECBPKCS5().encrypt(key, new CodecSource(plaintext)).toHex();
    }

    @Override
    public String decrypt(byte[] key, String hexCiphertext) {
        return new AES128ECBPKCS5().decrypt(key, fromHex(hexCiphertext)).toString();
    }

}