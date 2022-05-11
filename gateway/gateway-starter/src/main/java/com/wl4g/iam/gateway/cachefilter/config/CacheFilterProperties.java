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
package com.wl4g.iam.gateway.cachefilter.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link CacheFilterProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-11 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@Validated
@ToString
public class CacheFilterProperties {

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
     * The name of the header that returns the request cached configuration.
     */
    private String requestCachedHeader = REQUEST_CACHED_HEADER;

    private CachedProperties defaultCache = new CachedProperties();

    @Getter
    @Setter
    @Validated
    @ToString
    public static class CachedProperties {

        /**
         * The enabled cached providers.
         */
        private CacheProvider provider = CacheProvider.LocalCache;

        /**
         * The request to local cached configuration properties.
         */
        private LocalCacheProperties local = new LocalCacheProperties();

        /**
         * The request to Redis cached configuration properties.
         */
        private RedisCacheProperties redis = new RedisCacheProperties();
    }

    @Getter
    @AllArgsConstructor
    public static enum CacheProvider {
        LocalCache(LocalCacheProperties.class), RedisCache(RedisCacheProperties.class);
        private final Class<?> providerClass;
    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class LocalCacheProperties {

    }

    @Getter
    @Setter
    @Validated
    @ToString
    public static class RedisCacheProperties {

    }

    public static String REQUEST_CACHED_HEADER = "X-Iscg-Cached";
}
