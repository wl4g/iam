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
package com.wl4g.iam.gateway.requestlimit.configurer;

import static java.lang.String.valueOf;

import javax.validation.constraints.NotBlank;

import com.wl4g.iam.gateway.requestlimit.limiter.quota.RedisQuotaRequestLimiterStrategy;
import com.wl4g.iam.gateway.requestlimit.limiter.rate.RedisRateRequestLimiterStrategy;

import reactor.core.publisher.Mono;

/**
 * User-level current limiting configuration interface (data plane), for
 * example, the current limiting configuration information can be loaded
 * according to the currently authenticated principal(rateLimitId).
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-20 v3.0.0
 * @since v3.0.0
 */
public interface LimiterStrategyConfigurer {

    Mono<RedisRateRequestLimiterStrategy> loadRateStrategy(@NotBlank String routeId, @NotBlank String limitKey);

    Mono<RedisQuotaRequestLimiterStrategy> loadQuotaStrategy(@NotBlank String routeId, @NotBlank String limitKey);

    public static String getConfigKey(String routeId, String limitKey) {
        return valueOf(routeId).concat(":").concat(limitKey);
    }

}
