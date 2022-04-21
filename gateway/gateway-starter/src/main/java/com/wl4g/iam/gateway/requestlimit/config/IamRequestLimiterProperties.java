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
package com.wl4g.iam.gateway.requestlimit.config;

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_EVENT_HITS;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_SUFFIX_IAM_GATEWAY_EVENT_YYYYMMDD;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

import com.wl4g.iam.gateway.requestlimit.configurer.LimitStrategy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link IamRequestLimiterProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-10-13 v1.0.0
 * @since v1.0.0
 */
@Getter
@Setter
@ToString
@Validated
public class IamRequestLimiterProperties {

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
     * HttpStatus to return when limited is true, defaults to TOO_MANY_REQUESTS.
     */
    private String statusCode = HttpStatus.TOO_MANY_REQUESTS.name();

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

    private LimitConfigProperties limitConfig = new LimitConfigProperties();

    private LimitEventRecorderProperties eventRecorderConfig = new LimitEventRecorderProperties();

    /**
     * User-level current limiting configuration interface (data plane), for
     * example, the current limiting configuration information can be loaded
     * according to the currently authenticated principal(rateLimitId).
     */
    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LimitConfigProperties {

        /**
         * Redis tokens rate limiter configuration key prefix.
         */
        private String prefix = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF;

        /**
         * The default configuration of request limiting strategy.
         */
        private LimitStrategy defaultStrategy = new LimitStrategy();
    }

    /**
     * Request limiting event recorder configuration properties.
     */
    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LimitEventRecorderProperties {

        /**
         * Publish eventRecorderConfig bus threads.
         */
        private int publishEventBusThreads = 1;

        /**
         * Based on whether the redis eventRecorderConfig logger enables
         * logging, if it is turned on, it can be used as a downgrade recovery
         * strategy when data is lost due to a catastrophic failure of the
         * persistent accumulator.
         */
        private boolean localLogEnabled = true;

        private RedisLimitEventRecorderProperties redis = new RedisLimitEventRecorderProperties();

        @Getter
        @Setter
        @ToString
        @Validated
        @AllArgsConstructor
        @NoArgsConstructor
        public static class RedisLimitEventRecorderProperties {

            /**
             * Redis eventRecorderConfig recorder hits accumulator key.
             */
            private String hitsCumulatorPrefix = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_EVENT_HITS;

            /**
             * Redis eventRecorderConfig recorder accumulator suffix of date
             * format pattern.
             */
            private String cumulatorSuffixOfDatePattern = CACHE_SUFFIX_IAM_GATEWAY_EVENT_YYYYMMDD;

        }

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
