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
package com.wl4g.iam.gateway.security.sign.event;

import static java.lang.String.valueOf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.google.common.eventbus.Subscribe;
import com.wl4g.iam.gateway.security.config.IamSecurityProperties;
import com.wl4g.iam.gateway.security.config.IamSecurityProperties.RedisEventRecorderProperties;
import com.wl4g.infra.common.lang.DateUtils2;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis-based signature authentication event accumulator, usually used in API
 * gateway billing business scenarios.
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-18 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class DefaultRedisSignAuthingEventRecoder {
    public static final String LOG_SIGN_EVENT_SUCCESS_PREFIX = "SIGN_SUCCESS_EVENT";
    public static final String LOG_SIGN_EVENT_FAILURE_PREFIX = "SIGN_FAILURE_EVENT";

    private @Autowired IamSecurityProperties authingConfig;
    private @Autowired StringRedisTemplate redisTemplate;

    @Subscribe
    public void onSuccess(SignAuthingSuccessEvent event) {
        if (!authingConfig.getSimpleSign().getEventRecorder().getRedis().isEnabled()) {
            return;
        }
        String appId = valueOf(event.getSource());
        Long incr = null;
        try {
            incr = getSuccessCumulator(event).increment(appId, 1);
        } finally {
            if (authingConfig.getSimpleSign().getEventRecorder().isLocalLogEnabled() && log.isInfoEnabled()) {
                log.info("{} {}->{}", LOG_SIGN_EVENT_SUCCESS_PREFIX, appId, incr);
            }
        }
    }

    @Subscribe
    public void onFailure(SignAuthingFailureEvent event) {
        if (!authingConfig.getSimpleSign().getEventRecorder().getRedis().isEnabled()) {
            return;
        }
        String appId = valueOf(event.getSource());
        Long incr = null;
        try {
            incr = getFailureCumulator(event).increment(appId, 1);
        } finally {
            if (authingConfig.getSimpleSign().getEventRecorder().isLocalLogEnabled() && log.isInfoEnabled()) {
                log.info("{} {}->{}", LOG_SIGN_EVENT_FAILURE_PREFIX, appId, incr);
            }
        }
    }

    private BoundHashOperations<String, Object, Object> getSuccessCumulator(BaseSignAuthingFailureEvent event) {
        RedisEventRecorderProperties redis = authingConfig.getSimpleSign().getEventRecorder().getRedis();
        String prefix = redis.getSuccessCumulatorPrefix();
        String suffix = redis.getCumulatorSuffixOfDatePattern();
        String key = prefix.concat(":").concat(event.getRouteId()).concat(":").concat(DateUtils2.getDate(suffix));
        return redisTemplate.boundHashOps(key);
    }

    private BoundHashOperations<String, Object, Object> getFailureCumulator(BaseSignAuthingFailureEvent event) {
        RedisEventRecorderProperties redis = authingConfig.getSimpleSign().getEventRecorder().getRedis();
        String prefix = redis.getFailureCumulatorPrefix();
        String suffix = redis.getCumulatorSuffixOfDatePattern();
        String key = prefix.concat(":").concat(event.getRouteId()).concat(":").concat(DateUtils2.getDate(suffix));
        return redisTemplate.boundHashOps(key);
    }

}
