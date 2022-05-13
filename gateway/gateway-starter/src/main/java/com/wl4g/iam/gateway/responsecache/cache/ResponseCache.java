/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License,  byte[]ersion 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.wl4g.iam.gateway.responsecache.cache;

import java.util.concurrent.Callable;

import reactor.core.publisher.Mono;

/**
 * {@link ResponseCache}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-12 v3.0.0
 * @since v3.0.0
 */
public interface ResponseCache {

    Object getOriginalCache();

    /**
     * Returns the value associated with {@code key} in this cache, or
     * {@code null} if there is no cached value for {@code key}.
     *
     * @since 11.0
     */
    Mono<byte[]> get(String key);

    /**
     * Associates {@code value} with {@code key} in this cache. If the cache
     * previously contained a value associated with {@code key}, the old value
     * is replaced by {@code value}.
     *
     * <p>
     * Prefer {@link #get(Object, Callable)} when using the conventional "if
     * cached, return; otherwise create, cache and return" pattern.
     *
     * @since 11.0
     */
    void put(String key, byte[] value);

    /** Discards any cached value for key {@code key}. */
    void invalidate(String key);

    /**
     * Discards all entries in the cache.
     */
    void invalidateAll();

    /** Returns the approximate number of entries in this cache. */
    Mono<Long> size();

    /**
     * Performs any pending maintenance operations needed by the cache. Exactly
     * which activities are performed -- if any -- is implementation-dependent.
     */
    void cleanUp();
}
