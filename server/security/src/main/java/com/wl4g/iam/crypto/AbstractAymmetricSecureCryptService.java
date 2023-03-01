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

import static com.wl4g.infra.common.codec.CodecSource.fromHex;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.CACHE_PREFIX_IAM_CRYPTO;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.exception.ExceptionUtils.wrapAndThrow;

import java.security.spec.KeySpec;
import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

import com.wl4g.infra.common.codec.CodecSource;
import com.wl4g.infra.common.crypto.asymmetric.AsymmetricCryptor;
import com.wl4g.infra.common.crypto.asymmetric.spec.KeyPairSpec;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.locks.JedisLockManager;
import com.wl4g.iam.config.properties.CryptoProperties;
import com.wl4g.iam.core.cache.CacheKey;
import com.wl4g.iam.core.cache.IamCache;
import com.wl4g.iam.core.cache.IamCacheManager;
import com.wl4g.iam.core.exception.IamException;

/**
 * Abstract secretKey asymmetric secure crypt service.
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2019-08-30
 * @since
 */
public abstract class AbstractAymmetricSecureCryptService<K extends KeyPairSpec> implements SecureCryptService {

    protected final SmartLogger log = getLogger(getClass());

    /**
     * KeySpec class.
     */
    protected final Class<K> keyPairSpecClass;

    /**
     * AsymmetricCryptor
     */
    protected final AsymmetricCryptor cryptor;

    /**
     * Simple lock manager.
     */
    protected final Lock lock;

    /**
     * Cryptic properties.
     */
    @Autowired
    protected CryptoProperties config;

    /**
     * Iam cache manager.
     */
    @Autowired
    protected IamCacheManager cacheManager;

    @SuppressWarnings("unchecked")
    public AbstractAymmetricSecureCryptService(JedisLockManager lockManager, AsymmetricCryptor cryptor) {
        notNullOf(lockManager, "lockManager");
        this.cryptor = notNullOf(cryptor, "cryptor");
        this.lock = lockManager.getLock(getClass().getSimpleName(), DEFAULT_LOCK_EXPIRE_MS, MILLISECONDS);

        ResolvableType resolveType = ResolvableType.forClass(getClass());
        this.keyPairSpecClass = (Class<K>) resolveType.getSuperType().getGeneric(0).resolve();
        notNull(keyPairSpecClass, "KeySpecClass must not be null.");
    }

    @Override
    public String encrypt(KeySpec keySpec, String plaintext) {
        return cryptor.encrypt(keySpec, new CodecSource(plaintext)).toHex();
    }

    @Override
    public String decrypt(KeySpec keySpec, String hexCiphertext) {
        return cryptor.decrypt(keySpec, fromHex(hexCiphertext)).toString();
    }

    @Override
    public KeyPairSpec borrowKeyPair() {
        int index = current().nextInt(config.getKeyPairPools());
        return borrowKeyPair(index);
    }

    @Override
    public KeyPairSpec borrowKeyPair(int index) {
        if (index < 0 || index >= config.getKeyPairPools()) {
            throw new IamException(format("Unable borrow keySpec index '{}' of out bound.", index));
        }

        // Load keySpec by index.
        IamCache cryptoCache = cacheManager.getIamCache(CACHE_PREFIX_IAM_CRYPTO);
        CacheKey cacheKey = new CacheKey(index, keyPairSpecClass);
        // Gets keyPairSpec
        KeyPairSpec keySpec = cryptoCache.getMapField(cacheKey);
        if (isNull(keySpec)) { // Expired?
            try {
                if (lock.tryLock(DEFAULT_TRYLOCK_TIMEOUT_MS, MILLISECONDS)) {
                    // Retry getting.
                    keySpec = cryptoCache.getMapField(cacheKey);
                    if (isNull(keySpec)) {
                        initKeyPairSpecPool();
                    }
                }
            } catch (Exception e) {
                wrapAndThrow(e);
            } finally {
                lock.unlock();
            }
        }
        notNull(keySpec, "Unable to borrow keySpec data. index: %s, keyPairPools: %s", index, config.getKeyPairPools());
        return keySpec;
    }

    @Override
    public KeyPairSpec generateKeyPair() {
        return cryptor.generateKeyPair();
    }

    @Override
    public KeyPairSpec generateKeyPair(byte[] publicKey, byte[] privateKey) {
        return cryptor.generateKeyPair(publicKey, privateKey);
    }

    @Override
    public KeySpec generatePubKeySpec(byte[] publicKey) {
        return cryptor.generatePubKeySpec(publicKey);
    }

    @Override
    public KeySpec generateKeySpec(byte[] privateKey) {
        return cryptor.generateKeySpec(privateKey);
    }

    @Override
    public Class<K> getKeyPairSpecClass() {
        return keyPairSpecClass;
    }

    /**
     * Initializing keyPairSpec pool.
     *
     * @return
     */
    private synchronized void initKeyPairSpecPool() {
        // Create generate cryptic keyPairs
        for (int index = 0; index < config.getKeyPairPools(); index++) {
            // Generate keyPairSpec.
            KeyPairSpec keySpec = generateKeyPair();
            // Storage to cache.
            cacheManager.getIamCache(CACHE_PREFIX_IAM_CRYPTO).mapPut(new CacheKey(index, config.getKeyPairExpireMs()), keySpec);
            log.debug("Puts keySpec to cache for index {}, keySpec => {}", index, toJSONString(keySpec));
        }
        log.info("Initialized keySpec total: {}", config.getKeyPairPools());
    }

    /**
     * Default JIGSAW initializing mutex image timeoutMs
     */
    final public static long DEFAULT_LOCK_EXPIRE_MS = 60_000L;

    /**
     * Try lock mutex timeoutMs.
     */
    final public static long DEFAULT_TRYLOCK_TIMEOUT_MS = DEFAULT_LOCK_EXPIRE_MS / 2;

}