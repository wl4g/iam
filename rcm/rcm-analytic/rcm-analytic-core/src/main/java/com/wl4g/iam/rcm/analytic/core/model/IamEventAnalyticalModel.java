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
     * Event remote client IP geographic information.
     */
    @JsonProperty(index = 40)
    private IpGeoInformation ipGeoInfo;

    /**
     * Event message.
     */
    @JsonProperty(index = 50)
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
     * IP geographic information.
     *
     * for example:
     * 
     * <pre>
     *  IP2LocationRecord:
     *      IP Address = 1.1.1.1
     *      Country Short = US
     *      Country Long = United States of America
     *      Region = California
     *      City = Los Angeles
     *      ISP = Not_Supported
     *      Latitude = 34.05223
     *      Longitude = -118.24368
     *      Domain = Not_Supported
     *      ZipCode = 90001
     *      TimeZone = -07:00
     *      NetSpeed = Not_Supported
     *      IDDCode = Not_Supported
     *      AreaCode = Not_Supported
     *      WeatherStationCode = Not_Supported
     *      WeatherStationName = Not_Supported
     *      MCC = Not_Supported
     *      MNC = Not_Supported
     *      MobileBrand = Not_Supported
     *      Elevation = 0.0
     *      UsageType = Not_Supported
     *      AddressType = Not_Supported
     *      Category = Not_Supported
     * </pre>
     */
    @Getter
    @Setter
    @ToString
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IpGeoInformation {
        private String ip;
        private String countryShort;
        private String countryLong;
        private String region;
        private String city;
        private String isp;
        private String latitude;
        private String longitude;
        private String domain;
        private String zipCode;
        private String timeZone;
        private String netSpeed;
        private String iddCode;
        private String areaCode;
        private String weatherStationCode;
        private String weatherStationName;
        private String mcc;
        private String mnc;
        private String mobileBrand;
        private String elevation;
        private String usageType;
        private String addressType;
        private String category;
    }

    /**
     * Sorted field list.
     */
    public static final List<Field> ORDERED_FIELDS = unmodifiableList(
            findAllDeclaredFields(IamEventAnalyticalModel.class, true).stream().filter(f -> needPersistent(f)).collect(toList()));

}
