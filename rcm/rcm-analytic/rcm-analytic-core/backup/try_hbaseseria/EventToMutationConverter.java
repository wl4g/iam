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
package com.wl4g.iam.rcm.analytic.core.hbase;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;

import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.connector.hbase.sink.HBaseMutationConverter;
import org.apache.flink.connector.hbase.util.HBaseSerde;
import org.apache.flink.connector.hbase.util.HBaseTableSchema;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.binary.BinaryStringData;
import org.apache.flink.types.RowKind;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;

import com.wl4g.iam.rcm.analytic.core.model.IamEventAnalyticalModel;

/**
 * {@link EventToMutationConverter}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-06 v3.0.0
 * @since v3.0.0
 */
public class EventToMutationConverter implements HBaseMutationConverter<IamEventAnalyticalModel> {
    private static final long serialVersionUID = 1L;

    private final String nullStringLiteral;
    private transient HBaseSerde hbaseSerde;

    public EventToMutationConverter(final String nullStringLiteral) {
        this.nullStringLiteral = notNullOf(nullStringLiteral, "nullStringLiteral");
    }

    @Override
    public void open() {
        this.hbaseSerde = new HBaseSerde(createHTableSchema(), nullStringLiteral);
    }

    @Override
    public Mutation convertToMutation(@NotNull IamEventAnalyticalModel record) {
        notNullOf(record, "record");

        List<Object> values = record.toBasicStringDataValues();
        values.add(0, generateRowKey(record));
        return hbaseSerde.createPutMutation(GenericRowData.ofKind(RowKind.INSERT, values.toArray()));
    }

    protected HBaseTableSchema createHTableSchema() {
        HBaseTableSchema schema = new HBaseTableSchema();
        schema.setRowKey("row", String.class);
        for (Field f : IamEventAnalyticalModel.ORDERED_FIELDS) {
            // Class<?> qualifierClazz = f.getType();
            // if (f.isEnumConstant()) { qualifierClazz = String.class; }
            // TODO use config family
            schema.addColumn("f1", f.getName(), String.class);
        }
        return schema;
    }

    protected String generateRowKey(@NotNull IamEventAnalyticalModel record) {
        // Use reversed time strings to avoid data hotspots.
        String dateTime = DateFormatUtils.format(record.getTimestamp(), "SSSssmmHHddMMyy");
        return new StringBuilder().append(dateTime)
                .append(",")
                .append(record.getPrincipal())
                .append(",")
                .append(record.getEventType().name().toLowerCase())
                .toString();
    }

}
