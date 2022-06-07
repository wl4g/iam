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
package com.wl4g.iam.rcm.analytic.core.model;

import static com.wl4g.infra.common.reflect.ReflectionUtils2.findAllDeclaredFields;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isNative;
import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.flink.table.data.binary.BinaryStringData;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wl4g.iam.rcm.eventbus.common.IamEventBase;
import com.wl4g.iam.rcm.eventbus.common.IamEventBase.EventType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link IamEventAnalyticalModel}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-08 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IamEventAnalyticalModel {

    /**
     * Event time-stamp.
     */
    @JsonProperty(index = 10)
    private long timestamp;

    /**
     * Event type.
     */
    @JsonProperty(index = 20)
    private EventType eventType;

    /**
     * Event source principal name.
     */
    @JsonProperty(index = 30)
    private String principal;

    /**
     * Event remote client IP.
     */
    @JsonProperty(index = 40)
    private String remoteIp;

    /**
     * Event remote client IP coordinates.
     */
    @JsonProperty(index = 50)
    private String coordinates;

    /**
     * Event message.
     */
    @JsonProperty(index = 60)
    private String message;

    /**
     * Event extension attributes.
     */
    private Map<String, String> attributes;

    /**
     * Transform to string values with basic field order index.
     * {@link JsonProperty#index()}.
     * 
     * @return
     */
    public List<Object> toBasicStringDataValues() {
        return ORDERED_FIELDS.stream().map(f -> {
            Object value = getField(f, this, true);
            String stringValue = value.getClass().isEnum() ? ((Enum<?>) value).name() : trimToEmpty(value.toString());
            return new BinaryStringData(stringValue);
        }).collect(toList());
    }

    /**
     * Check if it is a valid field that needs to be persisted.
     * 
     * @param f
     * @return
     */
    protected static boolean needPersistent(Field f) {
        int m = f.getModifiers();
        Class<?> t = f.getType();
        return !isTransient(m) && !isStatic(m) && !isFinal(m) && !isAbstract(m) && !isNative(m)
                && (t.isPrimitive() || t.isEnum() || String.class.isAssignableFrom(t) || Number.class.isAssignableFrom(t));
    }

    /**
     * Construction instance from {@link IamEventBase}
     * 
     * @param event
     * @return
     */
    public static IamEventAnalyticalModel of(@NotNull IamEventBase event) {
        IamEventAnalyticalModel model = new IamEventAnalyticalModel();
        model.setTimestamp(event.getTimestamp());
        model.setEventType(event.getEventType());
        model.setPrincipal(event.getSource().toString());
        model.setMessage(event.getMessage());
        model.setAttributes(event.getAttributes());
        return model;
    }

    /**
     * Sorted field list.
     */
    public static final List<Field> ORDERED_FIELDS = unmodifiableList(
            findAllDeclaredFields(IamEventAnalyticalModel.class, true).stream().filter(f -> needPersistent(f)).collect(toList()));

}
