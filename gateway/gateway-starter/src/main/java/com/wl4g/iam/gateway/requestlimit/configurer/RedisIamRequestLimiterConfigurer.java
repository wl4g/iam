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
import static java.util.Objects.isNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;

import reactor.core.publisher.Mono;

/**
 * {@link RedisIamRequestLimiterConfigurer}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-20 v3.0.0
 * @since v3.0.0
 */
public class RedisIamRequestLimiterConfigurer implements IamRequestLimiterConfigurer {

    private @Autowired IamRequestLimiterProperties requestLimitConfig;
    private @Autowired StringRedisTemplate redisTemplate;

    @Override
    public Mono<LimitStrategy> loadStrategy(String routeId, String limitKey) {
        LimitStrategy config = parseJSON(
                getOperation().get(requestLimitConfig.getLimiter().getPrefix(), getConfigId(routeId, limitKey)),
                LimitStrategy.class);
        return isNull(config) ? Mono.empty() : Mono.just(config);
    }

    private HashOperations<String, String, String> getOperation() {
        return redisTemplate.opsForHash();
    }

    public static String getConfigId(String routeId, String id) {
        return valueOf(routeId).concat(":").concat(id);
    }

}
