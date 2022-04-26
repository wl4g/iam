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

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF_QUOTA;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF_RATE;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_EVENT_HITS;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_TOKEN_QUOTA;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_TOKEN_RATE;
import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_SUFFIX_IAM_GATEWAY_EVENT_YYYYMMDD;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

import com.wl4g.iam.gateway.requestlimit.key.HeaderIamKeyResolver.HeaderKeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.HostIamKeyResolver.HostKeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.IntervalIamKeyResolver.IntervalKeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.PathIamKeyResolver.PathKeyResolverStrategy;
import com.wl4g.iam.gateway.requestlimit.key.PrincipalNameIamKeyResolver;
import com.wl4g.iam.gateway.requestlimit.limiter.RedisQuotaIamRequestLimiter.RedisQuotaLimiterStrategy;
import com.wl4g.iam.gateway.requestlimit.limiter.RedisRateIamRequestLimiter.RedisRateLimiterStrategy;

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
     * HttpStatus to return when defaultLimiter is true, defaults to
     * TOO_MANY_REQUESTS.
     */
    private String statusCode = HttpStatus.TOO_MANY_REQUESTS.name();

    /**
     * The default key resolver configuration properties.
     */
    private KeyResolverProperties defaultKeyResolver = new KeyResolverProperties();

    /**
     * The default defaultLimiter configuration properties.
     */
    private LimiterProperties defaultLimiter = new LimiterProperties();

    /**
     * The global event recorder configuration properties.
     */
    private EventRecorderProperties eventRecorder = new EventRecorderProperties();

    /**
     * The default key resolver configuration properties.
     */
    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KeyResolverProperties {

        /**
         * The key resolver based on request headers.
         */
        private HeaderKeyResolverStrategy header = new HeaderKeyResolverStrategy();

        /**
         * The key resolver based on request host.
         */
        private HostKeyResolverStrategy host = new HostKeyResolverStrategy();

        /**
         * The key resolver based on request interval.
         */
        private IntervalKeyResolverStrategy interval = new IntervalKeyResolverStrategy();

        /**
         * The key resolver based on request path.
         */
        private PathKeyResolverStrategy path = new PathKeyResolverStrategy();

        /**
         * The key resolver based on request principal.
         */
        private PrincipalNameIamKeyResolver principal = new PrincipalNameIamKeyResolver();

    }

    /**
     * The request defaultLimiter configuration properties.
     */
    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LimiterProperties {

        /**
         * The default rate limiting configuration (E.G: when no configuration
         * is specified for principal)
         */
        private RedisRateLimiterStrategyProperties rate = new RedisRateLimiterStrategyProperties();

        /**
         * The default quota limiting configuration (E.G: when no configuration
         * is specified for principal)
         */
        private RedisQuotaLimiterStrategyProperties quota = new RedisQuotaLimiterStrategyProperties();
    }

    /**
     * The request rate defaultLimiter strategy configuration properties.
     */
    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RedisRateLimiterStrategyProperties extends RedisRateLimiterStrategy {

        /**
         * Redis tokens rate defaultLimiter user-level configuration key prefix.
         */
        private String configPrefix = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF_RATE;

        /**
         * Redis tokens rate defaultLimiter user-level token computational key
         * prefix.
         */
        private String tokenPrefix = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_TOKEN_RATE;

        /**
         * The name of the header that returns the burst capacity configuration.
         */
        private String burstCapacityHeader = RATE_BURST_CAPACITY_HEADER;

        /**
         * The name of the header that returns the replenish rate configuration.
         */
        private String replenishRateHeader = RATE_REPLENISH_RATE_HEADER;

        /**
         * The name of the header that returns the requested tokens
         * configuration.
         */
        private String requestedTokensHeader = RATE_REQUESTED_TOKENS_HEADER;

        /**
         * The name of the header that returns number of remaining requests
         * during the current second.
         */
        private String remainingHeader = RATE_REMAINING_HEADER;

        /**
         * Burst Capacity header name.
         */
        public static final String RATE_BURST_CAPACITY_HEADER = "X-RateLimit-Burst-Capacity";

        /**
         * Replenish Rate Limit header name.
         */
        public static final String RATE_REPLENISH_RATE_HEADER = "X-RateLimit-Replenish-Rate";

        /**
         * Requested Tokens header name.
         */
        public static final String RATE_REQUESTED_TOKENS_HEADER = "X-RateLimit-Requested-Tokens";

        /**
         * Remaining Rate Limit header name.
         */
        public static final String RATE_REMAINING_HEADER = "X-RateLimit-Remaining";
    }

    /**
     * The request quota defaultLimiter strategy configuration properties.
     */
    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RedisQuotaLimiterStrategyProperties extends RedisQuotaLimiterStrategy {

        /**
         * The quota limiter user-level configuration key prefix.
         */
        private String configPrefix = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_CONF_QUOTA;

        /**
         * The quota limiter user-level tokens computational key prefix.
         */
        private String tokenPrefix = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_TOKEN_QUOTA;

        /**
         * The name of the header that returns the request capacity
         * configuration.
         */
        private String requestCapacityHeader = QUOTA_REQUEST_CAPACITY_HEADER;

        /**
         * The name of the header that returns number of remaining requests
         * during the current second.
         */
        private String remainingHeader = QUOTA_REMAINING_HEADER;

        /**
         * Request capacity header name.
         */
        public static final String QUOTA_REQUEST_CAPACITY_HEADER = "X-QuotaLimit-Request-Capacity";

        /**
         * Remaining quota Limit header name.
         */
        public static final String QUOTA_REMAINING_HEADER = "X-QuotaLimit-Remaining";
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
    public static class EventRecorderProperties {

        /**
         * Publish eventRecorder bus threads.
         */
        private int publishEventBusThreads = 1;

        /**
         * Based on whether the redis eventRecorder logger enables logging, if
         * it is turned on, it can be used as a downgrade recovery strategy when
         * data is lost due to a catastrophic failure of the persistent
         * accumulator.
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
             * Redis eventRecorder recorder hits accumulator key.
             */
            private String hitsCumulatorPrefix = CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_EVENT_HITS;

            /**
             * Redis eventRecorder recorder accumulator suffix of date format
             * pattern.
             */
            private String cumulatorSuffixOfDatePattern = CACHE_SUFFIX_IAM_GATEWAY_EVENT_YYYYMMDD;

        }

    }

}
