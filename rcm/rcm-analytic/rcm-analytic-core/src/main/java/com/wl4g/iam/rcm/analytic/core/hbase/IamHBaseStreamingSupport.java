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

import static java.lang.String.format;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.cli.ParseException;
import org.apache.flink.connector.hbase.sink.HBaseSinkFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.NamespaceExistException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;

import com.wl4g.iam.rcm.analytic.core.IamFlinkStreamingBase;
import com.wl4g.iam.rcm.analytic.core.model.IamEventAnalyticalModel;

import lombok.CustomLog;
import lombok.Getter;

/**
 * {@link IamHBaseStreamingSupport}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-07 v3.0.0
 * @since v3.0.0
 */
@Getter
@CustomLog
public abstract class IamHBaseStreamingSupport extends IamFlinkStreamingBase {

    private String hbaseZkAddrs;
    private String hTableNamespace;
    private String hTableName;
    private Long bufferFlushMaxSizeInBytes;
    private Long bufferFlushMaxRows;
    private Long bufferFlushIntervalMillis;

    protected IamHBaseStreamingSupport() {
        super();
        // Sink options.
        builder.mustLongOption("hbaseZkAddrs", "HBase zookeeper quorum addresses.")
                .longOption("hTableNamespace", "iam", "Sink to HBase table namespace.")
                .longOption("hTableName", "t_iam_event", "Sink to HBase table name.")
                .longOption("bufferFlushMaxSizeInBytes", "8192",
                        "Sink to HBase write flush max buffer size. if <=0, it will not be setup and keep the default behavior.")
                .longOption("bufferFlushMaxRows", "128",
                        "Sink to HBase write flush max size. if <=0, it will not be setup and keep the default behavior.")
                .longOption("bufferFlushIntervalMillis", "5000",
                        "Sink to HBase write flush time interval. if <=0, it will not be setup and keep the default behavior.");
    }

    @Override
    protected IamFlinkStreamingBase parse(String[] args) throws ParseException {
        super.parse(args);
        // Sink options.
        this.hbaseZkAddrs = line.get("hbaseZkAddrs");
        this.hTableNamespace = line.get("hTableNamespace");
        this.hTableName = line.get("hTableName");
        this.bufferFlushMaxSizeInBytes = line.getLong("bufferFlushMaxSizeInBytes");
        this.bufferFlushMaxRows = line.getLong("bufferFlushMaxRows");
        this.bufferFlushIntervalMillis = line.getLong("bufferFlushIntervalMillis");
        return this;
    }

    @Override
    protected IamFlinkStreamingBase customStream(DataStreamSource<IamEventAnalyticalModel> dataStream) {
        Configuration conf = HBaseConfiguration.create();
        // Clients prefer to use this configuration.
        conf.set(HConstants.CLIENT_ZOOKEEPER_QUORUM, hbaseZkAddrs, getClass().getSimpleName());
        // fix-see:org.apache.flink.connector.hbase.sink.HBaseSinkFunction#prepareRuntimeConfiguration()#L169
        conf.set(HConstants.ZOOKEEPER_QUORUM, hbaseZkAddrs, getClass().getSimpleName());

        createHTableIfNecessary(conf, hTableNamespace, hTableName, 1);

        // add HTable sink
        EventToMutationConverter converter = new EventToMutationConverter();

        // Note: If the not full table name, an error like this will be
        // reported: RetriesExhaustedWithDetailsException: Failed 1 action: 1
        // time, servers with issues: null
        String fullTableName = hTableNamespace.concat(":").concat(hTableName);
        dataStream.addSink(new HBaseSinkFunction<>(fullTableName, conf, converter, bufferFlushMaxSizeInBytes, bufferFlushMaxRows,
                bufferFlushIntervalMillis));
        return this;
    }

    /**
     * Create HTable if necessary.
     * 
     * @param conf
     * @param hTableNamespace
     * @param hTableName
     * @param numberRegions
     */
    public static void createHTableIfNecessary(Configuration conf, String hTableNamespace, String hTableName, int numberRegions) {
        try (Connection conn = ConnectionFactory.createConnection(conf);) {
            Admin admin = conn.getAdmin();
            try {
                admin.createNamespace(NamespaceDescriptor.create(hTableNamespace).build());
            } catch (NamespaceExistException e) {
                log.info("Found existing htable namespace for '{}'", hTableNamespace);
            }
            TableName table = TableName.valueOf(hTableNamespace, hTableName);
            if (!admin.tableExists(table)) {
                TableDescriptor tabDesc = TableDescriptorBuilder.newBuilder(table)
                        .setColumnFamily(ColumnFamilyDescriptorBuilder.of("f1"))
                        // .setCompactionEnabled(true)
                        // .setMergeEnabled(true)
                        // .setSplitEnabled(true)
                        .build();
                if (numberRegions > 1) {
                    admin.createTable(tabDesc, Arrays.copyOfRange(SPLIT_KEYS, 0, numberRegions - 1));
                } else {
                    admin.createTable(tabDesc);
                }
            } else {
                log.info("Found that HTable: {} already existing.", table);
            }
        } catch (IOException e) {
            throw new IllegalStateException(format("Failed to create htable for '%s:%s'", hTableNamespace, hTableName), e);
        }
    }

    public static final byte[][] SPLIT_KEYS = { { 'b' }, { 'c' }, { 'd' }, { 'e' }, { 'f' }, { 'g' }, { 'h' }, { 'i' }, { 'j' },
            { 'k' }, { 'l' }, { 'm' }, { 'n' }, { 'o' }, { 'p' }, { 'q' }, { 'r' }, { 's' }, { 't' }, { 'u' }, { 'v' }, { 'w' },
            { 'x' }, { 'y' }, { 'z' } };

}
