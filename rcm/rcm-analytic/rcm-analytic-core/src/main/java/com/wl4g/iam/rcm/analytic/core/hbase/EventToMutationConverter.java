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
package com.wl4g.iam.rcm.analytic.core.hbase;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.StringUtils2.getBytes;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.lang.reflect.Field;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.connector.hbase.sink.HBaseMutationConverter;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;

import com.wl4g.iam.rcm.analytic.core.model.IamEventAnalyticalModel;

/**
 * {@link EventToMutationConverter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-06 v3.0.0
 * @since v3.0.0
 */
public class EventToMutationConverter implements HBaseMutationConverter<IamEventAnalyticalModel> {
    private static final long serialVersionUID = 1L;

    private final byte[] nullStringBytes;

    public EventToMutationConverter() {
        this(new byte[0]);
    }

    public EventToMutationConverter(byte[] nullStringBytes) {
        this.nullStringBytes = notNullOf(nullStringBytes, "nullStringBytes");
    }

    @Override
    public void open() {
    }

    @Override
    public Mutation convertToMutation(@NotNull IamEventAnalyticalModel record) {
        notNullOf(record, "record");
        Put put = new Put(generateRowkey(record));
        for (Field f : IamEventAnalyticalModel.ORDERED_FIELDS) {
            byte[] value = nullStringBytes;
            Object v = getField(f, record, true);
            if (nonNull(v)) {
                value = getBytes(v.toString());
            }
            put.addColumn(getBytes("f1"), getBytes(f.getName()), value);
        }
        return put;
    }

    protected byte[] generateRowkey(@NotNull IamEventAnalyticalModel record) {
        // Use reversed time strings to avoid data hotspots.
        String reverseDate = DateFormatUtils.format(record.getTimestamp(), "SSSssmmHHddMMyy");

        // TODO transform to standard city/region/country name.
        //
        return new StringBuilder().append(reverseDate) // when
                // who
                .append(",")
                .append(record.getPrincipal())
                // what
                .append(",")
                .append(record.getEventType().getCode())
                // where
                .append(",")
                .append(getGeoCityKey(record))
                .append(",")
                .append(getGeoRegionKey(record))
                .append(",")
                .append(getGeoCountryKey(record))
                .toString()
                .getBytes(UTF_8);
    }

    protected String getGeoCityKey(@NotNull IamEventAnalyticalModel record) {
        return fixFieldKey(record.getIpGeoInfo().getCity());
    }

    protected String getGeoRegionKey(@NotNull IamEventAnalyticalModel record) {
        return fixFieldKey(record.getIpGeoInfo().getRegion());
    }

    protected String getGeoCountryKey(@NotNull IamEventAnalyticalModel record) {
        return fixFieldKey(record.getIpGeoInfo().getCountryShort());
    }

    /**
     * Fix for example:
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
     **/
    protected String fixFieldKey(String field) {
        String cleanFieldKey = trimToEmpty(field).toLowerCase().replace(" ", "_");

        // Limit field max length.
        if (cleanFieldKey.length() > DEFAULT_MAX_ROWKEY_FIELD_LENGTH) {
            cleanFieldKey = cleanFieldKey.substring(0, DEFAULT_MAX_ROWKEY_FIELD_LENGTH);
        }
        return cleanFieldKey;
    }

    public static final int DEFAULT_MAX_ROWKEY_FIELD_LENGTH = 24;

}
