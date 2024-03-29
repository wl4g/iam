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
package com.wl4g.iam.client.security.crypto;

import com.wl4g.infra.common.codec.CodecSource;

/**
 * {@link DynamicCipherService}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2020-09-02
 * @since
 */
public interface DynamicCipherService {

	/**
	 * Encryption cipher text.
	 * 
	 * @param key
	 * @param plaintext
	 * @return
	 */
	CodecSource encrypt(byte[] key, CodecSource plaintext);

	/**
	 * Decryption plain text.
	 * 
	 * @param key
	 * @param ciphertext
	 * @return
	 */
	CodecSource decrypt(byte[] key, CodecSource ciphertext);

}
