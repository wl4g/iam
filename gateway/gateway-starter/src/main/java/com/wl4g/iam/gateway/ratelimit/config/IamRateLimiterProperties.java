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
package com.wl4g.iam.gateway.ratelimit.config;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_RATELIMIT_REDIS_RECORDER_HITS;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_SUFFIX_IAM_GATEWAY_REDIS_RECORDER;

import javax.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link IamRateLimiterProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-10-13 v1.0.0
 * @since v1.0.0
 */
@Getter
@Setter
@ToString
@Validated
public class IamRateLimiterProperties {

    /**
     * Switch to deny requests if the Key Resolver returns an empty key,
     * defaults to true.
     */
    private boolean denyEmptyKey = true;

    /**
     * HttpStatus to return when denyEmptyKey is true, defaults to FORBIDDEN.
     */
    private String emptyKeyStatusCode = HttpStatus.FORBIDDEN.name();

    /**
     * Whether or not to include headers containing rate limiter information,
     * defaults to true.
     */
    private boolean includeHeaders = true;

    /**
     * The name of the header that returns number of remaining requests during
     * the current second.
     */
    private String remainingHeader = REMAINING_HEADER;

    /** The name of the header that returns the replenish rate configuration. */
    private String replenishRateHeader = REPLENISH_RATE_HEADER;

    /** The name of the header that returns the burst capacity configuration. */
    private String burstCapacityHeader = BURST_CAPACITY_HEADER;

    /**
     * The name of the header that returns the requested tokens configuration.
     */
    private String requestedTokensHeader = REQUESTED_TOKENS_HEADER;

    private TokenRateLimitProperties token = new TokenRateLimitProperties();

    private RedisRateLimitEventProperties event = new RedisRateLimitEventProperties();

    /**
     * Token bucket-based current limiting algorithm configuration
     */
    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenRateLimitProperties {
        private @Min(1) Integer replenishRate = 1;
        private @Min(0) Integer burstCapacity = 1;
        private @Min(1) Integer requestedTokens = 1;
    }

    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RedisRateLimitEventProperties {

        /**
         * Publish event bus threads.
         */
        private int publishEventBusThreads = 1;

        /**
         * Based on whether the redis event logger enables logging, if it is
         * turned on, it can be used as a downgrade recovery strategy when data
         * is lost due to a catastrophic failure of the persistent accumulator.
         */
        private boolean redisEventRecoderLogEnabled = true;

        /**
         * Redis event recorder hits accumulator key.
         */
        private String redisEventRecoderHitsCumulatorPrefix = CACHE_PREFIX_IAM_GWTEWAY_RATELIMIT_REDIS_RECORDER_HITS;

        /**
         * Redis event recorder accumulator suffix of date format pattern.
         */
        private String redisEventRecoderCumulatorSuffixOfDatePattern = CACHE_SUFFIX_IAM_GATEWAY_REDIS_RECORDER;

    }

    /**
     * Remaining Rate Limit header name.
     */
    public static final String REMAINING_HEADER = "X-RateLimit-Remaining";

    /**
     * Replenish Rate Limit header name.
     */
    public static final String REPLENISH_RATE_HEADER = "X-RateLimit-Replenish-Rate";

    /**
     * Burst Capacity header name.
     */
    public static final String BURST_CAPACITY_HEADER = "X-RateLimit-Burst-Capacity";

    /**
     * Requested Tokens header name.
     */
    public static final String REQUESTED_TOKENS_HEADER = "X-RateLimit-Requested-Tokens";

}
