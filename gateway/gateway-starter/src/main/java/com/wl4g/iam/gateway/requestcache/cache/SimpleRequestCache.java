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
package com.wl4g.iam.gateway.requestcache.cache;

import com.google.common.cache.Cache;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * {@link SimpleRequestCache}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-12 v3.0.0
 * @since v3.0.0
 */
@AllArgsConstructor
public class SimpleRequestCache implements RequestCache {

    private final Cache<String, byte[]> memoryCache;

    @Override
    public Object getOriginalCache() {
        return memoryCache;
    }

    @Override
    public Mono<byte[]> get(String key) {
        return Mono.just(memoryCache.getIfPresent(key));
    }

    @Override
    public void put(String key, byte[] value) {
        memoryCache.put(key, value);
    }

    @Override
    public void invalidate(String key) {
        memoryCache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        memoryCache.invalidateAll();
    }

    @Override
    public Mono<Long> size() {
        return Mono.just(memoryCache.size());
    }

    @Override
    public void cleanUp() {
        memoryCache.cleanUp();
    }

}
