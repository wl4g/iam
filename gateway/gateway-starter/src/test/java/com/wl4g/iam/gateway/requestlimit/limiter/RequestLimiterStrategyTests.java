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
package com.wl4g.iam.gateway.requestlimit.limiter;

import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

import org.junit.Test;

import com.wl4g.iam.gateway.requestlimit.limiter.IamRequestLimiter.RequestLimiterStrategy;
import com.wl4g.iam.gateway.requestlimit.limiter.RedisRateIamRequestLimiter.RedisRateLimiterStrategy;

/**
 * {@link KeyStrategyTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-26 v3.0.0
 * @since v3.0.0
 */
public class RequestLimiterStrategyTests {

    @Test
    public void testLimiterStrategySerialToJson() {
        RedisRateLimiterStrategy strategy = new RedisRateLimiterStrategy(100, 20, 1);
        System.out.println(toJSONString(strategy));
    }

    @Test
    public void testLimiterStrategyParseFromJson() {
        String json = "{\"privoder\":\"RedisRateLimiter\",\"includeHeaders\":true,\"burstCapacity\":100,\"replenishRate\":20,\"requestedTokens\":1}";
        RequestLimiterStrategy strategy = parseJSON(json, RequestLimiterStrategy.class);
        System.out.println(strategy);
    }

}
