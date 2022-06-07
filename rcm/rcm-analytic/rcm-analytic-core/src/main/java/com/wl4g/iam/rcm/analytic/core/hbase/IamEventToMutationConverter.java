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

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findAllDeclaredFields;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isNative;
import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;
import static java.util.Objects.isNull;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.validation.constraints.NotNull;

import org.apache.flink.connector.hbase.sink.HBaseMutationConverter;
import org.apache.flink.connector.hbase.util.HBaseSerde;
import org.apache.flink.connector.hbase.util.HBaseTableSchema;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.types.RowKind;
import org.apache.hadoop.hbase.client.Mutation;

import com.wl4g.iam.rcm.eventbus.event.IamEvent;

/**
 * {@link IamEventToMutationConverter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-06 v3.0.0
 * @since v3.0.0
 */
public class IamEventToMutationConverter implements HBaseMutationConverter<IamEvent> {
    private static final long serialVersionUID = 1L;

    private final String nullStringLiteral;
    private transient ConcurrentMap<Class<?>, HBaseSerde> hbaseSerdeCache = new ConcurrentHashMap<>(8);

    public IamEventToMutationConverter(final String nullStringLiteral) {
        this.nullStringLiteral = notNullOf(nullStringLiteral, "nullStringLiteral");
    }

    @Override
    public void open() {
    }

    @Override
    public Mutation convertToMutation(@NotNull IamEvent record) {
        notNullOf(record, "record");
        // TODO sort fields
        GenericRowData row = GenericRowData.of(RowKind.INSERT, record.getTimestamp(), record.getEventType(), record.getSource(),
                record.getMessage());
        return getOrCreateHBaseSerde(record).createPutMutation(row);
    }

    protected HBaseSerde getOrCreateHBaseSerde(IamEvent record) {
        final Class<?> key = record.getClass();
        HBaseSerde serde = hbaseSerdeCache.get(key);
        if (isNull(serde)) {
            synchronized (this) {
                serde = hbaseSerdeCache.get(key);
                if (isNull(serde)) {
                    hbaseSerdeCache.put(key, serde = new HBaseSerde(createHTableSchema(record), nullStringLiteral));
                }
            }
        }
        return serde;
    }

    protected HBaseTableSchema createHTableSchema(IamEvent record) {
        HBaseTableSchema schema = new HBaseTableSchema();
        for (Field f : findAllDeclaredFields(record.getClass(), true)) {
            if (checkPersistent(f)) {
                schema.addColumn("f1", f.getName(), f.getType());
            }
        }
        return schema;
    }

    protected boolean checkPersistent(Field f) {
        int m = f.getModifiers();
        return !isTransient(m) && !isStatic(m) && !isFinal(m) && !isAbstract(m) && !isNative(m);
    }

}
