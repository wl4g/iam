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

import static com.wl4g.iam.common.constant.GatewayIAMConstants.CACHE_PREFIX_IAM_GWTEWAY_REQUESTLIMIT_EVENT_HITS;
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
     * HttpStatus to return when limiter is true, defaults to TOO_MANY_REQUESTS.
     */
    private String statusCode = HttpStatus.TOO_MANY_REQUESTS.name();

    /**
     * The default key resolver configuration properties.
     */
    private DefaultKeyResolverProperties defaultKeyResolver = new DefaultKeyResolverProperties();

    /**
     * The default limiter configuration properties.
     */
    private DefaultLimiterProperties defaultLimiter = new DefaultLimiterProperties();

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
    public static class DefaultKeyResolverProperties {

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
     * The Default limiter configuration properties.
     */
    @Getter
    @Setter
    @ToString
    @Validated
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DefaultLimiterProperties {

        /**
         * The default rate limiting configuration (E.G: when no configuration
         * is specified for principal)
         */
        private RedisRateLimiterStrategy rate = new RedisRateLimiterStrategy();

        /**
         * The default quota limiting configuration (E.G: when no configuration
         * is specified for principal)
         */
        private RedisQuotaLimiterStrategy quota = new RedisQuotaLimiterStrategy();

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
