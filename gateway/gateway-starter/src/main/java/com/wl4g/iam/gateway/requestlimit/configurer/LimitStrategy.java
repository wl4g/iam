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

import static java.util.Arrays.asList;

import java.util.List;

import javax.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;

import com.wl4g.infra.common.web.WebUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link LimitStrategy}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-21 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class LimitStrategy {

    private TokenRateLimitStrategy rate = new TokenRateLimitStrategy();
    private QuotaLimitStrategy quota = new QuotaLimitStrategy();

    @Getter
    @Setter
    @ToString
    @Validated
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenRateLimitStrategy {

        /**
         * Limit algorithm based on token bucket, The default token bucket
         * capacity, that is, the total number of concurrency allowed.
         */
        private @Min(0) Integer burstCapacity = 1;

        /**
         * Limit algorithm based on token bucket, How many requests per second
         * do you want a user to be allowed to do?
         */
        private @Min(1) Integer replenishRate = 1;

        /**
         * Limit algorithm based on token bucket, How many tokens are requested
         * per request?
         */
        private @Min(1) Integer requestedTokens = 1;

        /**
         * The date pattern of the key get by rate limiting according to the
         * date interval.
         */
        private String intervalKeyResolverDatePattern = "yyyyMMdd";

        /**
         * The according to the list of header names of the request header
         * current limiter, it can usually be used to obtain the actual IP after
         * being forwarded by the proxy to limit the current, or it can be
         * flexibly used for other purposes.
         */
        private List<String> headerKeyResolverNames = asList(WebUtils.HEADER_REAL_IP);
    }

    @Getter
    @Setter
    @ToString
    @Validated
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotaLimitStrategy {

        /**
         * The number of total maximum allowed requests capacity.
         */
        private @Min(0) Long requestCapacity = 1000L;

        private String intervalKeyResolverDatePattern;
    }

}
