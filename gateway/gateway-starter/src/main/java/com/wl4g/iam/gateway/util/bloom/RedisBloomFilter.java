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
package com.wl4g.iam.gateway.util.bloom;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.wl4g.infra.common.log.SmartLogger;

/**
 * {@link RedisBloomFilter}
 * 
 * <p>
 * 1. What is the definition of Bloom-filter?
 * 
 * Bloom filter was proposed by Bloom in 1970. It's actually a long binary
 * vector and a series of random mapping functions. Bloom filters can Used to
 * retrieve whether an element is in a collection. Its advantage is that the
 * space efficiency and query time are far more than the general algorithm, and
 * the disadvantage is that there is a certain misrecognition rate and deletion
 * difficulty. Bloom Filter (BF) is a random data structure with high space
 * efficiency. It uses a bit array to represent a set very concisely, and can
 * judge whether an element belongs to this set. It is a fast probabilistic
 * algorithm for determining whether an element exists in a set. Bloom Filter
 * may make wrong judgments, but it will not miss judgments. That is, the Bloom
 * Filter judgment element If the elements are no longer assembled, it must not
 * be there. If the judgment element exists in the set, there is a certain
 * probability that the judgment is wrong. Therefore, Bloom Filter is not
 * suitable for "zero error" applications. In applications that can tolerate low
 * error rates, Bloom Filter greatly saves space compared to other common
 * algorithms (such as hash, halved search).
 * </p>
 * 
 * <p>
 * 2. What is Bloom-filter principle?
 * 
 * The principle of the Bloom filter is that when an element is added to the
 * set, the element is mapped to K points in a bit array by K hash functions,
 * and they are set to 1. When retrieving, I We only need to see if these points
 * are all 1s to know (approximately) whether there is it in the set: if any of
 * these points have a 0, the checked element must be absent; if they are all 1,
 * the checked element element is likely to be there. This is the basic idea of
 * ​​the Bloom filter. The difference between Bloom Filter and the single hash
 * function Bit-Map is that Bloom Filter uses k hash functions, and each string
 * corresponds to k bits. thus reducing the likelihood of conflict Rate.
 * </p>
 * 
 * <p>
 * 3. Disadvantages of Bloom Filter bloom filter, sacrificing the accuracy of
 * judgment and the convenience of deletion There is a misjudgment, the element
 * to be found may not be in the container, but the value of k positions
 * obtained after hashing is all 1. If the bloom filter stores a blacklist, Then
 * you can store elements that may be misjudged by establishing a whitelist.
 * Difficulty removing. An element placed in the container is mapped to k
 * positions in the bit array, which is 1. When deleting, it cannot be simply
 * set to 0 directly, which may affect the judgment of other elements. break.
 * Counting Bloom Filter can be used
 * </p>
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-05 v3.0.0
 * @since v3.0.0
 */
public class RedisBloomFilter<T> {

    private final SmartLogger log = getLogger(getClass());
    // TODO use ReactiveStringRedisTemplate
    private final StringRedisTemplate redisTemplate;
    private final BloomConfig<T> bloomConfig;

    public RedisBloomFilter(@NotNull StringRedisTemplate redisTemplate, @NotNull BloomConfig<T> bloomConfig) {
        this.redisTemplate = notNullOf(redisTemplate, "redisTemplate");
        this.bloomConfig = notNullOf(bloomConfig, "bloomConfig");
    }

    /**
     * Add value based on given bloom filter configuration.
     * 
     * @param key
     * @param value
     */
    public void bloomAdd(String key, T value) {
        log.debug("bloomAdd {}: {}", key, value);

        int[] offset = bloomConfig.murmurHashOffset(value);
        for (int i : offset) {
            redisTemplate.opsForValue().setBit(key, i, true);
        }
    }

    /**
     * Determines whether a value exists based on the given bloom filter
     * configuration.
     * 
     * @param key
     * @param value
     */
    public boolean bloomExist(String key, T value) {
        log.debug("bloomExist {}: {}", key, value);

        int[] offset = bloomConfig.murmurHashOffset(value);
        for (int i : offset) {
            if (!redisTemplate.opsForValue().getBit(key, i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Expiration bloom filter elements.
     * 
     * @param key
     * @param value
     */
    public void bloomExpire(String key, long expireMs) {
        log.debug("bloomExpire {}", key);
        // In order to allow setting the expiration time, the key needs to be
        // initialized when it does not exist.
        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForValue().setBit(key, 0, true);
        }
        redisTemplate.opsForValue().getOperations().expire(key, expireMs, TimeUnit.SECONDS);
    }

    /**
     * Remove bloom filter elements.
     * 
     * @param key
     * @param value
     */
    public void bloomRemove(String key) {
        log.debug("bloomRemove {}", key);
        redisTemplate.opsForValue().getOperations().delete(key);
    }

    /**
     * {@link BloomConfig}
     * 
     * <p>
     * 1. What is the definition of Bloom-filter?
     * 
     * Bloom filter was proposed by Bloom in 1970. It's actually a long binary
     * vector and a series of random mapping functions. Bloom filters can Used
     * to retrieve whether an element is in a collection. Its advantage is that
     * the space efficiency and query time are far more than the general
     * algorithm, and the disadvantage is that there is a certain misrecognition
     * rate and deletion difficulty. Bloom Filter (BF) is a random data
     * structure with high space efficiency. It uses a bit array to represent a
     * set very concisely, and can judge whether an element belongs to this set.
     * It is a fast probabilistic algorithm for determining whether an element
     * exists in a set. Bloom Filter may make wrong judgments, but it will not
     * miss judgments. That is, the Bloom Filter judgment element If the
     * elements are no longer assembled, it must not be there. If the judgment
     * element exists in the set, there is a certain probability that the
     * judgment is wrong. Therefore, Bloom Filter is not suitable for "zero
     * error" applications. In applications that can tolerate low error rates,
     * Bloom Filter greatly saves space compared to other common algorithms
     * (such as hash, halved search).
     * </p>
     * 
     * <p>
     * 2. What is Bloom-filter principle?
     * 
     * The principle of the Bloom filter is that when an element is added to the
     * set, the element is mapped to K points in a bit array by K hash
     * functions, and they are set to 1. When retrieving, I We only need to see
     * if these points are all 1s to know (approximately) whether there is it in
     * the set: if any of these points have a 0, the checked element must be
     * absent; if they are all 1, the checked element element is likely to be
     * there. This is the basic idea of ​​the Bloom filter. The difference
     * between Bloom Filter and the single hash function Bit-Map is that Bloom
     * Filter uses k hash functions, and each string corresponds to k bits. thus
     * reducing the likelihood of conflict Rate.
     * </p>
     * 
     * <p>
     * 3. Disadvantages of Bloom Filter bloom filter, sacrificing the accuracy
     * of judgment and the convenience of deletion There is a misjudgment, the
     * element to be found may not be in the container, but the value of k
     * positions obtained after hashing is all 1. If the bloom filter stores a
     * blacklist, Then you can store elements that may be misjudged by
     * establishing a whitelist. Difficulty removing. An element placed in the
     * container is mapped to k positions in the bit array, which is 1. When
     * deleting, it cannot be simply set to 0 directly, which may affect the
     * judgment of other elements. break. Counting Bloom Filter can be used
     * </p>
     */
    public static class BloomConfig<T> {
        private final Funnel<T> funnel;
        private final int numHashFunctions;
        private final int bitSize;

        /**
         * Build of {@link BloomConfig} instance.
         * 
         * @param funnel
         * @param expectedInsertions
         *            Estimated insertion volume
         * @param fpp
         *            error tolerance rate
         */
        public BloomConfig(@NotNull Funnel<T> funnel, int expectedInsertions, double fpp) {
            this.funnel = notNullOf(funnel, "funnel");
            this.bitSize = optimalNumOfBits(expectedInsertions, fpp);
            this.numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, bitSize);
        }

        public int[] murmurHashOffset(T value) {
            int[] offset = new int[numHashFunctions];
            long hash64 = Hashing.murmur3_128().hashObject(value, funnel).asLong();
            int hash1 = (int) hash64;
            int hash2 = (int) (hash64 >>> 32);
            for (int i = 1; i <= numHashFunctions; i++) {
                int nextHash = hash1 + i * hash2;
                if (nextHash < 0) {
                    nextHash = ~nextHash;
                }
                offset[i - 1] = nextHash % bitSize;
            }
            return offset;
        }

        /**
         * Calculate the length of the bit array.
         * 
         * @param n
         * @param p
         * @return
         */
        private int optimalNumOfBits(long n, double p) {
            if (p == 0) {
                p = Double.MIN_VALUE;
            }
            return (int) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
        }

        /**
         * Calculate the number of times the hash method is executed.
         * 
         * @param n
         * @param m
         * @return
         */
        private int optimalNumOfHashFunctions(long n, long m) {
            return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
        }
    }

}
