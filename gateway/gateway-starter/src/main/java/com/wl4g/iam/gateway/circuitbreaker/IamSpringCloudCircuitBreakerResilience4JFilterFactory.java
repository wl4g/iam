/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.iam.gateway.circuitbreaker;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.gateway.support.ServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.ResponseStatusException;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import reactor.core.publisher.Mono;

/**
 * @author Ryan Baxter
 */
@SuppressWarnings("rawtypes")
public class IamSpringCloudCircuitBreakerResilience4JFilterFactory extends IamSpringCloudCircuitBreakerFilterFactory {

    public IamSpringCloudCircuitBreakerResilience4JFilterFactory(ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory,
            ObjectProvider<DispatcherHandler> dispatcherHandlerProvider) {
        super(reactiveCircuitBreakerFactory, dispatcherHandlerProvider);
    }

    @Override
    protected Mono<Void> handleErrorWithoutFallback(Throwable t) {
        if (java.util.concurrent.TimeoutException.class.isInstance(t)) {
            return Mono.error(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, t.getMessage(), t));
        }
        if (CallNotPermittedException.class.isInstance(t)) {
            return Mono.error(new ServiceUnavailableException());
        }
        return Mono.error(t);
    }

}
