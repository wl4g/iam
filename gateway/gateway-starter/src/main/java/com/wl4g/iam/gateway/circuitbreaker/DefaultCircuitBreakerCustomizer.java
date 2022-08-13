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
package com.wl4g.iam.gateway.circuitbreaker;

import org.springframework.cloud.client.circuitbreaker.Customizer;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link DefaultCircuitBreakerCustomizer}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-16 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public class DefaultCircuitBreakerCustomizer implements Customizer<CircuitBreaker> {

    @Override
    public void customize(CircuitBreaker circuitBreaker) {
        // TODO
        circuitBreaker.getEventPublisher().onSuccess(event -> {
            log.debug("CircuitBreaker success: ", event);
        }).onError(event -> {
            log.warn("CircuitBreaker error: ", event);
        }).onStateTransition(event -> {
            log.warn("CircuitBreaker stateTransition: ", event);
        }).onReset(event -> {
            log.warn("CircuitBreaker reset: ", event);
        }).onIgnoredError(event -> {
            log.warn("CircuitBreaker ignore error: ", event);
        }).onFailureRateExceeded(event -> {
            log.warn("CircuitBreaker failureRateExceeded: ", event);
        }).onCallNotPermitted(event -> {
            log.warn("CircuitBreaker callNotPermitted: ", event);
        }).onSlowCallRateExceeded(event -> {
            log.warn("CircuitBreaker slowCallRateExceeded: ", event);
        });
    }

}
