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

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static java.lang.String.format;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.SystemUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration;

import com.wl4g.iam.gateway.responsecache.config.ResponseCacheProperties.EhCacheProperties;
import com.wl4g.infra.common.io.FileIOUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link EhCacheResponseCacheTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-13 v3.0.0
 * @since v3.0.0
 */
@Getter
@Slf4j
public class EhCacheResponseCache implements ResponseCache, Closeable {

    private final CacheManager cacheManager;
    private final Cache<String, byte[]> originalCache;

    public EhCacheResponseCache(@NotNull EhCacheProperties config, @NotBlank String routeId) {
        notNullOf(config, "config");
        hasTextOf(routeId, "routeId");
        this.cacheManager = buildRouteCacheManager(config, routeId);
        this.originalCache = cacheManager.getCache(config.getCacheNamePrefix().concat("-").concat(routeId), String.class,
                byte[].class);
    }

    @Override
    public Object getOriginalCache() {
        return originalCache;
    }

    @Override
    public Mono<byte[]> get(@NotNull String key) {
        notNullOf(key, "key");
        try {
            return Mono.justOrEmpty(originalCache.get(key));
        } catch (Exception e) {
            log.error(format("Cannot to get response cache of '%s'", key), e);
        }
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> put(@NotNull String key, @NotNull byte[] value) {
        notNullOf(key, "key");
        notNullOf(value, "value");
        try {
            originalCache.put(key, value);
            return Mono.just(true);
        } catch (Exception e) {
            log.error(format("Cannot to put response cache of '%s' -> %s ...", key, ResponseCache.copyHeadToString(value)), e);
        }
        return Mono.just(false);
    }

    @Override
    public Mono<Long> invalidate(String key) {
        notNullOf(key, "key");
        try {
            originalCache.remove(key);
            return Mono.just(1L);
        } catch (Exception e) {
            log.error(format("Cannot to invalidate response cache of '%s'", key), e);
        }
        return Mono.just(0L);
    }

    @Override
    public Mono<Boolean> invalidateAll() {
        originalCache.clear();
        return Mono.just(true);
    }

    @Override
    public Mono<Long> size() {
        return Mono.just(-1L);
    }

    @Override
    public Mono<Boolean> cleanUp() {
        originalCache.clear();
        return Mono.just(true);
    }

    @Override
    public void close() throws IOException {
        cacheManager.close();
    }

    /**
     * Build EhCache cache manager for route.
     * 
     * see:https://www.ehcache.org/documentation/3.10/
     * see:https://github.com/ehcache/ehcache3-samples
     * see:https://github1s.com/ehcache/ehcache3/blob/HEAD/ehcache-impl/src/test/java/org/ehcache/config/builders/PersistentCacheManagerTest.java#L92-L100
     * 
     * @param config
     * @param routeId
     * @return
     */
    public static PersistentCacheManager buildRouteCacheManager(EhCacheProperties config, String routeId) {
        try {
            String prefix = "iscg-responsecache-tmp-";
            File rootDir = new File(SystemUtils.JAVA_IO_TMPDIR, prefix + currentTimeMillis());
            FileIOUtils.forceMkdir(rootDir);
            rootDir.toPath().toFile().deleteOnExit();
            String cacheAlias = config.getCacheNamePrefix().concat("-").concat(routeId);
            return newCacheManagerBuilder().with(new CacheManagerPersistenceConfiguration(rootDir))
                    .withCache(cacheAlias,
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
                                    ResourcePoolsBuilder.newResourcePoolsBuilder()
                                            .heap(config.getOffHeapEntries(), EntryUnit.ENTRIES)
                                            .offheap(config.getOffHeapSize().toBytes(), MemoryUnit.B)
                                            .disk(config.getDiskSize().toBytes(), MemoryUnit.B, true)))
                    .build(true);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
