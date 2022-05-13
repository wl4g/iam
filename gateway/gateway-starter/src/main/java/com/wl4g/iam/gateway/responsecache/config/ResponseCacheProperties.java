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
package com.wl4g.iam.gateway.responsecache.config;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_SUFFIX_IAM_GATEWAY_RESPONSECACHE;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link ResponseCacheProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-11 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@Validated
@ToString
public class ResponseCacheProperties {

    /**
     * Preferred to enable request cache match SPEL match expression. Default by
     * '#{true}', which means never match. </br>
     * </br>
     * Tip: The built-in support to get the current routeId, such as:
     * '#{routeId.get().test('my-service-route')}'
     */
    private String preferOpenMatchExpression = "#{true}";

    /**
     * Prefer to enable request cache match rule definitions.
     */
    private List<MatchHttpRequestRule> preferMatchRuleDefinitions = new ArrayList<>();

    /**
     * The name of the header that returns the response cached configuration.
     */
    private String responseCachedHeader = RESPONSE_CACHED_HEADER;

    /**
     * The initial capacity of the temporary buffer size.
     */
    private int tmpBufferInitialCapacity = 1024;

    /**
     * The maximum capacity of the temporary buffer size.
     */
    private int tmpBufferMaxCapacity = 1024 * 1024;

    /**
     * The default cached filter configuration properties.
     */
    private CachedProperties defaultCache = new CachedProperties();

    @Getter
    @Setter
    @Validated
    @ToString
    public static class CachedProperties {

        /**
         * The enabled cached providers.
         */
        private CacheProvider provider = CacheProvider.SimpleCache;

        /**
         * The request to default simple cache configuration properties.
         */
        private SimpleCacheProperties simple = new SimpleCacheProperties();

        /**
         * The request to ehcache configuration properties.
         */
        private EhCacheProperties ehcache = new EhCacheProperties();

        /**
         * The request to redis configuration properties.
         */
        private RedisCacheProperties redis = new RedisCacheProperties();
    }

    @Getter
    @AllArgsConstructor
    public static enum CacheProvider {
        SimpleCache(SimpleCacheProperties.class), EhCache(SimpleCacheProperties.class), RedisCache(RedisCacheProperties.class);
        private final Class<?> providerClass;
    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class SimpleCacheProperties {

        /**
         * If you wish the cache should not exceed this number of entries, the
         * cache will evict recently or infrequently used entries when it does,
         * WARNING: the cache may evict entries before this limit is exceeded -
         * usually when the cache size is close to the limit.
         */
        private long maximumSize = 100_000L;

        /**
         * The expiration interval based on write time, all cache entry reads
         * and writes are updated.
         */
        private long expireAfterAccessMs = Duration.ofSeconds(60).toMillis();

        /**
         * The expiration interval based on access time, all cache entries are
         * updated only by write operations.
         */
        private long expireAfterWriteMs = Duration.ofSeconds(600).toMillis();

        /**
         * The number of concurrent cache operations, that is, the number of
         * underlying cache block/segment locks.
         */
        private int concurrencyLevel = 4;
    }

    public static enum EliminationAlgorithm {
        LRU, LFU, FIFO;
    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class EhCacheProperties {

        /**
         * The cached data elimination algorithm.
         */
        private EliminationAlgorithm eliminationAlg = EliminationAlgorithm.LRU;

        /**
         * The cache name prefix.
         */
        private String cacheNamePrefix = "requestCache";

        /**
         * 
         */
        private long offHeapEntries = 100_000L;

        private DataSize offHeapSize = DataSize.ofMegabytes(128);

    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class RedisCacheProperties {
        private String cachePrefix = CACHE_SUFFIX_IAM_GATEWAY_RESPONSECACHE;
        private long expireMs = 60_000L;
    }

    public static String RESPONSE_CACHED_HEADER = "X-Iscg-Cached";
}
