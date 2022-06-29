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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.wl4g.iam.gateway.responsecache.config.ResponseCacheProperties.EhCacheProperties;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * {@link EhCacheResponseCacheTests}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-14 v3.0.0
 * @since v3.0.0
 */
public class EhCacheResponseCacheTests {

    @Test
    public void testEhCacheCreateAndPutAndGet() throws IOException {
        try (EhCacheResponseCache cache = new EhCacheResponseCache(new EhCacheProperties(), "my-service-route");) {
            String myvalue1 = "myvalue1";

            Mono<String> result = cache.put("mykey1", myvalue1.getBytes()).then(cache.get("mykey1")).flatMap(
                    v -> Mono.just(new String(v, StandardCharsets.UTF_8)));

            StepVerifier.create(result).expectNext(myvalue1);
        }
    }

}
