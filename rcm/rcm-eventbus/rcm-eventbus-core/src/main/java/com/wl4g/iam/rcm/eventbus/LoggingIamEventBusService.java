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
package com.wl4g.iam.rcm.eventbus;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.wl4g.iam.rcm.eventbus.common.IamEvent;

import lombok.AllArgsConstructor;
import lombok.CustomLog;

/**
 * {@link LoggingIamEventBusService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-30 v3.0.0
 * @since v3.0.0
 */
@CustomLog
public class LoggingIamEventBusService implements IamEventBusService<IamEvent> {

    @Override
    public Object getOriginal() {
        return null;
    }

    @Override
    public Future<IamEvent> publish(IamEvent event) {
        log.info("On event: {}", event);
        return new NoneFuture(event);
    }

    @AllArgsConstructor
    static class NoneFuture implements Future<IamEvent> {

        private final IamEvent event;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public IamEvent get() throws InterruptedException, ExecutionException {
            return event;
        }

        @Override
        public IamEvent get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return event;
        }
    }

}
