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
package com.wl4g.iam.rcm.eventbus.common;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.Exceptions.getStackTraceAsString;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJsonNode;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@link IamEventBase}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-30 v3.0.0
 * @since v3.0.0
 */
@Getter
public abstract class IamEventBase extends EventObject {

    private static final long serialVersionUID = 3242901223478600427L;

    /**
     * Event time-stamp.
     */
    private final long timestamp;

    /**
     * Event type.
     */
    private final EventType eventType;

    /**
     * Event remote client IP.
     */
    private final String remoteIp;

    /**
     * Event remote client IP coordinates.
     */
    private final String coordinates;

    /**
     * Event message.
     */
    private final String message;

    /**
     * Event extension attributes properties.
     */
    private final Map<String, String> attributes = new LinkedHashMap<>(8);

    /**
     * Construction {@link IamEventBase} instance.
     * 
     * @param source
     *            The event source is usually the principal name or IP of the
     *            authentication request, etc.
     * @param eventType
     * @param message
     */
    public IamEventBase(@NotNull EventType eventType, @NotNull Object source, @NotBlank String remoteIp,
            @NotBlank String coordinates, @Nullable String message) {
        this(currentTimeMillis(), eventType, source, remoteIp, coordinates, message);
    }

    /**
     * Construction {@link IamEventBase} instance.
     * 
     * @param source
     *            The event source is usually the principal name or IP of the
     *            authentication request, etc.
     * @param eventType
     * @param ex
     */
    public IamEventBase(@NotNull EventType eventType, @NotNull Object source, @NotBlank String remoteIp,
            @NotBlank String coordinates, @Nullable Throwable ex) {
        this(currentTimeMillis(), eventType, source, remoteIp, coordinates, getStackTraceAsString(ex));
    }

    /**
     * Construction {@link IamEventBase} instance.
     * 
     * @param timestamp
     *            time-stamp
     * @param eventType
     *            event type.
     * @param source
     *            The event source is usually the principal name or IP of the
     *            authentication request, etc.
     * @param message
     */
    public IamEventBase(@Min(0) long timestamp, @Nullable EventType eventType, @NotNull Object source, @NotBlank String remoteIp,
            @NotBlank String coordinates, @Nullable String message) {
        super(notNullOf(source, "source"));
        isTrueOf(timestamp > 0, format("timestamp > 0, but is: %s", timestamp));
        this.timestamp = timestamp;
        this.eventType = notNullOf(eventType, "eventType");
        this.remoteIp = hasTextOf(remoteIp, "remoteIp");
        this.coordinates = hasTextOf(coordinates, "coordinates");
        this.message = message;
    }

    /**
     * Serialize {@link IamEventBase} object to JSON string.
     * 
     * @param event
     * @return
     */
    @Deprecated
    public static String to(IamEventBase event) {
        return toJSONString(event.attributes);
    }

    /**
     * DeSerialize to {@link IamEventBase} object JSON string.
     * 
     * @param json
     *            {@link IamEventBase} JSON string.
     * @return
     */
    @SuppressWarnings("serial")
    @Deprecated
    public static IamEventBase from(String json) {
        JsonNode node = parseJsonNode(json, "");
        long timestamp = node.at("/timestamp").asLong();
        EventType eventType = EventType.of(node.at("/eventType").asText());
        Object source = node.at("/source").asText();
        String remoteIp = node.at("/remoteIp").asText();
        String coordinates = node.at("/coordinates").asText();
        String message = node.at("/message").asText();
        // return parseJSON(json, IamEventBase.class);
        return new IamEventBase(timestamp, eventType, source, remoteIp, coordinates, message) {
        };
    }

    @Getter
    @AllArgsConstructor
    public static enum EventType {
        AUTHC_SUCCESS(0),

        AUTHC_FAILURE(1),

        // AUTHC_FAILURE_WITH_CREDENTIALS,
        // AUTHC_FAILURE_WITH_RISK,

        UNKNOWN(9);

        private final int code;

        public static EventType of(String eventName) {
            for (EventType et : values()) {
                if (equalsIgnoreCase(et.name(), eventName)) {
                    return et;
                }
            }
            return UNKNOWN;
        }

    }

}
