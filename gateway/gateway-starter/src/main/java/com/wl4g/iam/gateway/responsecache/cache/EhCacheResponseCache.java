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
import static org.ehcache.config.units.MemoryUnit.MB;

import java.io.Closeable;
import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import com.wl4g.iam.gateway.responsecache.config.ResponseCacheProperties.EhCacheProperties;

import lombok.Getter;
import reactor.core.publisher.Mono;

/**
 * {@link EhCacheResponseCache}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-13 v3.0.0
 * @since v3.0.0
 */
@Getter
public class EhCacheResponseCache implements ResponseCache, Closeable {

    private final CacheManager cacheManager;
    private final Cache<String, byte[]> originalCache;

    public EhCacheResponseCache(@NotNull EhCacheProperties config, String routeId) {
        notNullOf(config, "config");
        // see:https://www.ehcache.org/documentation/3.10/
        // see:https://github.com/ehcache/ehcache3-samples
        CacheConfigurationBuilder<String, byte[]> ccBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,
                byte[].class,
                ResourcePoolsBuilder.heap(config.getOffHeapEntries()).offheap(config.getOffHeapSize().toMegabytes(), MB));
        //TODO use disk strategy config
        this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(config.getCacheNamePrefix().concat("-").concat(routeId), ccBuilder)
                .build(true);
        this.originalCache = cacheManager.getCache("defaultLocalCache", String.class, byte[].class);
    }

    @Override
    public Object getOriginalCache() {
        return originalCache;
    }

    @Override
    public Mono<byte[]> get(String key) {
        return Mono.justOrEmpty(originalCache.get(key));
    }

    @Override
    public void put(String key, byte[] value) {
        originalCache.put(key, value);
    }

    @Override
    public void invalidate(String key) {
        originalCache.remove(key);
    }

    @Override
    public void invalidateAll() {
        originalCache.clear();
    }

    @Override
    public Mono<Long> size() {
        return Mono.just(-1L);
    }

    @Override
    public void cleanUp() {
        originalCache.clear();
    }

    @Override
    public void close() throws IOException {
        cacheManager.close();
    }
}
