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
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJsonNode;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@link IamEvent}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-30 v3.0.0
 * @since v3.0.0
 */
public class IamEvent extends EventObject {

    private static final long serialVersionUID = 3242901223478600427L;

    /**
     * Event model properties.
     */
    private final Map<String, Object> modelProperties;

    /**
     * Construction {@link IamEvent} instance.
     * 
     * @param source
     *            The event source is usually the principal name or IP of the
     *            authentication request, etc.
     * @param message
     */
    public @JsonCreator IamEvent(@NotNull Object source, @Nullable String message) {
        this(currentTimeMillis(), null, source, message);
    }

    /**
     * Construction {@link IamEvent} instance.
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
    public @JsonCreator IamEvent(@Min(0) long timestamp, @Nullable EventType eventType, @NotNull Object source,
            @Nullable String message) {
        super(notNullOf(source, "source"));
        this.modelProperties = new HashMap<>(8);
        setTimestamp(timestamp);
        setEventType(eventType);
        setSource(source);
        setMessage(message);
    }

    /**
     * Gets attributes for time-stamp.
     * 
     * @return
     */
    public long getTimestamp() {
        return doGet("timestamp");
    }

    /**
     * Gets attributes for time-stamp.
     * 
     * @return
     */
    public void setTimestamp(@Min(0) long timestamp) {
        isTrueOf(timestamp > 0, format("timestamp > 0, but is: %s", timestamp));
        modelProperties.put("timestamp", timestamp);
    }

    /**
     * Gets attributes for event type.
     * 
     * @return
     */
    public EventType getEventType() {
        return doGet("eventType");
    }

    /**
     * Sets attributes for event type.
     * 
     * @return
     */
    public void setEventType(EventType eventType) {
        doPut("eventType", isNull(eventType) ? EventType.of(getClass()) : eventType);
    }

    /**
     * Gets attributes for message.
     * 
     * @return
     */
    public String getMessage() {
        return doGet("message");
    }

    /**
     * Sets attributes for message.
     * 
     * @return
     */
    public void setMessage(@Nullable String message) {
        doPut("message", message);
    }

    /**
     * Sets attributes for source.
     * 
     * @return
     */
    public void setSource(@Nullable Object source) {
        this.source = notNullOf("source", "source");
        doPut("source", source);
    }

    /**
     * Getting field to model properties.
     * 
     * @param key
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <T> T doGet(String key) {
        return (T) modelProperties.get(key);
    }

    /**
     * Putting field to model properties.
     * 
     * @param key
     * @param value
     * @return
     */
    protected IamEvent doPut(String key, Object value) {
        modelProperties.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return to(this);
    }

    /**
     * Serialize {@link IamEvent} object to JSON string.
     * 
     * @param event
     * @return
     */
    public static String to(IamEvent event) {
        return toJSONString(event.modelProperties);
    }

    /**
     * Deserialize to {@link IamEvent} object JSON string.
     * 
     * @param json
     *            {@link IamEvent} JSON string.
     * @return
     */
    public static IamEvent from(String json) {
        JsonNode node = parseJsonNode(json, "");
        long timestamp = node.at("/timestamp").asLong();
        EventType eventType = EventType.of(node.at("/eventType").asText());
        Object source = node.at("/source").asText();
        String message = node.at("/message").asText();
        return new IamEvent(timestamp, eventType, source, message);
    }

    @Getter
    @AllArgsConstructor
    public static enum EventType {

        AUTHC_FAILURE(FailureAuthenticationEvent.class),

        // TODO new sub event type?
        AUTHC_FAILURE_WITH_CREDENTIALS(FailureAuthenticationEvent.class),

        AUTHC_FAILURE_WITH_RISK(FailureAuthenticationEvent.class),

        AUTHC_SUCCESS(SuccessAuthenticationEvent.class),

        UNKNOWN(IamEvent.class);

        private final Class<? extends IamEvent> eventClass;

        public static EventType of(Class<? extends IamEvent> eventClass) {
            notNullOf(eventClass, "eventClass");
            for (EventType et : values()) {
                if (et.getEventClass() == eventClass) {
                    return et;
                }
            }
            return UNKNOWN;
        }

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
