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
package com.wl4g.iam.core.cache;

import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

/**
 * Enhanced cache manager implements let shiro use redis caching
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @time 2017年4月13日
 * @since
 */
public interface IamCacheManager extends CacheManager {

    /**
     * Gets {@link IamCache} instance
     *
     * @param name
     * @return
     * @throws CacheException
     */
    IamCache getIamCache(String name) throws CacheException;

}