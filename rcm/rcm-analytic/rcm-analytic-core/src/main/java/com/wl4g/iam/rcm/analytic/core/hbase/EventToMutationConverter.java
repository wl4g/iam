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
        Put put = new Put(generateRowKey(record));
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

    protected byte[] generateRowKey(@NotNull IamEventAnalyticalModel record) {
        // Use reversed time strings to avoid data hotspots.
        String dateTime = DateFormatUtils.format(record.getTimestamp(), "SSSssmmHHddMMyy");
        return new StringBuilder().append(dateTime)
                .append(",")
                .append(record.getPrincipal())
                .append(",")
                .append(record.getEventType().name().toLowerCase())
                .toString()
                .getBytes(UTF_8);
    }

}
