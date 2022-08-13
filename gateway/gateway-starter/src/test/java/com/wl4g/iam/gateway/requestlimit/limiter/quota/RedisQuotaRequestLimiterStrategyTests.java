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
package com.wl4g.iam.gateway.requestlimit.limiter.quota;

import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

import org.junit.jupiter.api.Test;

/**
 * {@link RedisQuotaRequestLimiterStrategyTests}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-17 v3.0.0
 * @since v3.0.0
 */
public class RedisQuotaRequestLimiterStrategyTests {

    @Test
    public void testSerializeStrategy() {
        RedisQuotaRequestLimiterStrategy strategy = new RedisQuotaRequestLimiterStrategy();
        strategy.setRequestCapacity(1000L);
        strategy.setIncludeHeaders(false);
        System.out.println(toJSONString(strategy));
    }

    @Test
    public void testDeserializeStrategy() {
        String json = "{\"requestCapacity\":1000,\"cycleDatePattern\":\"yyyyMMddHH\",\"includeHeaders\":true}";
        System.out.println(json);
        RedisQuotaRequestLimiterStrategy strategy = parseJSON(json, RedisQuotaRequestLimiterStrategy.class);
        System.out.println(strategy.getRequestCapacity());
    }

}
