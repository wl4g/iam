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

import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static java.lang.String.valueOf;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.iam.gateway.requestlimit.limiter.IamRequestLimiter;
import com.wl4g.iam.gateway.requestlimit.limiter.IamRequestLimiter.LimiterStrategy;

import reactor.core.publisher.Mono;

/**
 * {@link RedisLimiterStrategyConfigurer}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-20 v3.0.0
 * @since v3.0.0
 */
public class RedisLimiterStrategyConfigurer implements LimiterStrategyConfigurer {

    private @Autowired IamRequestLimiterProperties requestLimitConfig;
    private @Autowired ReactiveStringRedisTemplate redisTemplate;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IamRequestLimiter, S extends LimiterStrategy> Mono<S> loadStrategy(
            @NotNull T limiter,
            @NotBlank String routeId,
            @NotBlank String limitKey) {
        // TODO
        String prefix = requestLimitConfig.getDefaultLimiter().getRate().getConfigPrefix();
        return getOperation().get(prefix, getConfigKey(routeId, limitKey))
                .map(json -> (S) parseJSON(json, limiter.kind().getStrategyClass()));
    }

    private ReactiveHashOperations<String, String, String> getOperation() {
        return redisTemplate.opsForHash();
    }

    public static String getConfigKey(String routeId, String limitKey) {
        return valueOf(routeId).concat(":").concat(limitKey);
    }

}
