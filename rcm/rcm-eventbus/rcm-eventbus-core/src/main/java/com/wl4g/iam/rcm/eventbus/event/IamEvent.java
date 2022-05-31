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
package com.wl4g.iam.rcm.eventbus.event;

import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static java.lang.String.format;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.shiro.event.Event;

import lombok.Getter;
import lombok.ToString;

/**
 * {@link IamEvent}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-30 v3.0.0
 * @since v3.0.0
 */
@Getter
@ToString
public abstract class IamEvent extends Event {
    private static final long serialVersionUID = 3242901223478600427L;

    private final EventType eventType;
    private final long timstamp;
    private final String message;

    /**
     * Construction {@link IamEvent} instance.
     * 
     * @param source
     *            The event source is usually the principal name or IP of the
     *            authentication request, etc.
     * @param message
     */
    public IamEvent(@NotNull Object source, @NotNull EventType eventType, @Nullable String message) {
        this(source, eventType, currentTimeMillis(), message);
    }

    /**
     * Construction {@link IamEvent} instance.
     * 
     * @param source
     *            The event source is usually the principal name or IP of the
     *            authentication request, etc.
     * @param timestamp
     * @param message
     */
    public IamEvent(@NotNull Object source, @NotNull EventType eventType, @Min(0) long timestamp, @Nullable String message) {
        super(notNullOf(source, "source"));
        this.eventType = notNullOf(eventType, "eventType");
        isTrueOf(timestamp > 0, format("timestamp > 0, but is: %s", timestamp));
        this.timstamp = timestamp;
        this.message = message;
    }

    public static enum EventType {

        AUTHC_FAILURE,

        AUTHC_FAILURE_WITH_CREDENTIALS,

        AUTHC_FAILURE_WITH_RISK,

        AUTHC_SUCCESS;
    }

}
