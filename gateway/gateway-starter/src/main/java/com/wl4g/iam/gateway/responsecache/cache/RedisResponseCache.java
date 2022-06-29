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
package com.wl4g.iam.gateway.responsecache.cache;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.StringUtils2.getBytes;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveHashOperations;

import com.wl4g.iam.gateway.config.ReactiveByteArrayRedisTemplate;
import com.wl4g.iam.gateway.responsecache.config.ResponseCacheProperties.RedisCacheProperties;

import reactor.core.publisher.Mono;

/**
 * {@link RedisResponseCache}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-12 v3.0.0
 * @since v3.0.0
 */
public class RedisResponseCache implements ResponseCache {

    private final ReactiveByteArrayRedisTemplate redisTemplate;
    private final ReactiveHashOperations<byte[], byte[], byte[]> hashOperation;
    private final RedisCacheProperties config;

    public RedisResponseCache(RedisCacheProperties config, ReactiveByteArrayRedisTemplate redisTemplate) {
        this.config = notNullOf(config, "config");
        this.redisTemplate = notNullOf(redisTemplate, "redisTemplate");
        this.hashOperation = redisTemplate.opsForHash();
    }

    @Override
    public Object getOriginalCache() {
        return hashOperation;
    }

    @Override
    public Mono<byte[]> get(String key) {
        return hashOperation.get(getBytes(config.getCachePrefix()), getBytes(key));
    }

    @Override
    public Mono<Boolean> put(String key, byte[] value) {
        return hashOperation.put(getBytes(config.getCachePrefix()), getBytes(key), value)
                .then(redisTemplate.expire(getBytes(key), Duration.ofMillis(config.getExpireMs())));
    }

    @Override
    public Mono<Long> invalidate(String key) {
        return hashOperation.remove(getBytes(config.getCachePrefix()), getBytes(key));
    }

    @Override
    public Mono<Boolean> invalidateAll() {
        return hashOperation.delete(getBytes(config.getCachePrefix()));
    }

    @Override
    public Mono<Long> size() {
        return hashOperation.size(getBytes(config.getCachePrefix()));
    }

    @Override
    public Mono<Boolean> cleanUp() {
        return hashOperation.delete(getBytes(config.getCachePrefix()));
    }

}
