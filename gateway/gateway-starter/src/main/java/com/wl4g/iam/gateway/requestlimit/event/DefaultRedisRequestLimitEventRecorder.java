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
package com.wl4g.iam.gateway.requestlimit.event;

import static java.lang.String.valueOf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.google.common.eventbus.Subscribe;
import com.wl4g.iam.gateway.requestlimit.config.IamRequestLimiterProperties;
import com.wl4g.infra.common.lang.DateUtils2;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link DefaultRedisRequestLimitEventRecorder}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-19 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class DefaultRedisRequestLimitEventRecorder {
    public static final String LOG_RATELIMIT_HITS_EVENT = "RATELIMIT_HITS_EVENT";
    public static final String LOG_QUOTALIMIT_HITS_EVENT = "QUOTALIMIT_HITS_EVENT";

    private @Autowired IamRequestLimiterProperties requestLimitConfig;
    private @Autowired StringRedisTemplate redisTemplate;

    @Subscribe
    public void onRateLimitHit(RateLimitHitEvent event) {
        String limitKey = valueOf(event.getSource());
        Long incr = null;
        try {
            String prefix = requestLimitConfig.getEventRecorder().getRedis().getRateHitsCumulatorPrefix();
            incr = getHitsCumulator(prefix, event.getRouteId()).increment(limitKey, 1);
        } finally {
            if (requestLimitConfig.getEventRecorder().isLocalLogEnabled() && log.isInfoEnabled()) {
                log.info("{} {}->{}", LOG_RATELIMIT_HITS_EVENT, limitKey, incr);
            }
        }
    }

    @Subscribe
    public void onQuotaLimitHit(QuotaLimitHitEvent event) {
        String limitKey = valueOf(event.getSource());
        Long incr = null;
        try {
            String prefix = requestLimitConfig.getEventRecorder().getRedis().getQuotaHitsCumulatorPrefix();
            incr = getHitsCumulator(prefix, event.getRouteId()).increment(limitKey, 1);
        } finally {
            if (requestLimitConfig.getEventRecorder().isLocalLogEnabled() && log.isInfoEnabled()) {
                log.info("{} {}->{}", LOG_QUOTALIMIT_HITS_EVENT, limitKey, incr);
            }
        }
    }

    private BoundHashOperations<String, Object, Object> getHitsCumulator(String configPrefix, String routeId) {
        String suffix = requestLimitConfig.getEventRecorder().getRedis().getCumulatorSuffixOfDatePattern();
        String hashKey = configPrefix.concat(":").concat(routeId).concat(":").concat(DateUtils2.getDate(suffix));
        if (log.isDebugEnabled()) {
            log.debug("hashkey: {}", hashKey);
        }
        return redisTemplate.boundHashOps(hashKey);
    }

}
