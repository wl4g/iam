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
package com.wl4g.iam.gateway.requestlimit.limiter.rate;

import javax.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;

import com.wl4g.iam.gateway.requestlimit.limiter.RequestLimiterStrategy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class RedisRateRequestLimiterStrategy extends RequestLimiterStrategy {

    /**
     * The default token bucket capacity, that is, the total number of
     * concurrency allowed.
     */
    private @Min(0) int burstCapacity = 1;

    /**
     * How many requests per second do you want a user to be allowed to do?
     */
    private @Min(1) int replenishRate = 1;

    /**
     * How many tokens are requested per request?
     */
    private @Min(1) int requestedTokens = 1;
}
