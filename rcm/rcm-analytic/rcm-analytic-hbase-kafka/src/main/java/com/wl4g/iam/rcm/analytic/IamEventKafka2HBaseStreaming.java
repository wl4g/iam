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
package com.wl4g.iam.rcm.analytic;

import static java.time.Duration.ofMillis;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.flink.connector.hbase.sink.HBaseSinkFunction;
import org.apache.flink.connector.hbase2.sink.HBaseDynamicTableSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceOptions;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import com.wl4g.iam.rcm.analytic.core.IamEventKafkaRecordDeserializationSchema;
import com.wl4g.iam.rcm.analytic.core.IamEventWatermarks;
import com.wl4g.iam.rcm.eventbus.event.IamEvent;
import com.wl4g.infra.common.cli.CommandLineTool;
import com.wl4g.infra.common.cli.CommandLineTool.CommandLineFacade;

/**
 * {@link IamEventKafka2HBaseStreaming}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-31 v3.0.0
 * @since v3.0.0
 * @see https://stackoverflow.com/questions/69765972/migrating-from-flinkkafkaconsumer-to-kafkasource-no-windows-executed
 */
public class IamEventKafka2HBaseStreaming {

    public static void main(String[] args) throws Exception {
        CommandLineFacade line = CommandLineTool.builder()
                .option("b", "brokers", "localhost:9092", "Connect kafka brokers addresses.")
                .mustOption("g", "groupId", "Kafka source consumer group id.")
                .option("t", "topicPattern", "iam_event", "Kafka topic regex pattern.")
                .longOption("checkpointMode", null,
                        "Sets the checkpoint mode, the default is null means not enabled. options: "
                                + asList(CheckpointingMode.values()))
                .longOption("checkpointMillis", "500",
                        "Checkpoint execution interval millis, only valid when checkpointMode is sets.")
                .option("f", "fromOffsetTime", "-1",
                        "Start consumption from the first record with a timestamp greater than or equal to a certain timestamp, if <=0, it will not be setup and keep the default behavior.")
                .option("p", "parallelism", "-1",
                        "The parallelism for operator, if <=0, it will not be setup and keep the default behavior.")
                .option("P", "maxParallelism", "-1",
                        "The maximum parallelism for operator, if <=0, it will not be setup and keep the default behavior.")
                .longOption("bufferTimeoutMillis", "-1",
                        "Parallelism for this operator, if <=0, it will not be setup and keep the default behavior.")
                .longOption("outOfOrdernessMillis", "120000", "The maximum millis out-of-orderness watermark generator assumes.")
                .longOption("idleTimeoutMillis", "30000", "The timeout millis for the idleness detection.")
                .longOption("partitionDiscoveryIntervalMs", "30000", "The per millis for discover new partitions interval.")
                .longOption("enablePrintSink", "true", "Override sets to print sink function.")
                .option("N", "jobName", "IamKafkaSourceJob", "Flink kafka source streaming job name.")
                .helpIfEmpty(args)
                .build(args);

        String brokers = line.get("brokers");
        String groupId = line.get("groupId");
        String topicPattern = line.get("topicPattern");
        CheckpointingMode checkpointMode = line.getEnum("checkpointMode", CheckpointingMode.class);
        Long checkpointMillis = line.getLong("checkpointMillis");
        Long fromOffsetTime = line.getLong("fromOffsetTime");
        Integer parallelism = line.getInteger("parallelism");
        Integer maxParallelism = line.getInteger("maxParallelism");
        Long bufferTimeoutMillis = line.getLong("bufferTimeoutMillis");
        Long outOfOrdernessMillis = line.getLong("outOfOrdernessMillis");
        Long idleTimeoutMillis = line.getLong("idleTimeoutMillis");
        String partitionDiscoveryIntervalMs = line.get("partitionDiscoveryIntervalMs");
        Boolean enablePrintSink = line.getBoolean("enablePrintSink");
        String jobName = line.get("jobName");

        Properties props = (Properties) System.getProperties().clone();
        // props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,brokers);
        // props.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        // see:https://github.com/apache/flink/blob/release-1.14.4/docs/content/docs/connectors/datastream/kafka.md#consumer-offset-committing
        // props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false");
        // props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"10");
        // props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        // props.setProperty(FlinkKafkaConsumerBase.KEY_DISABLE_METRICS,"true");
        // see:https://github.com/apache/flink/blob/release-1.14.4/docs/content/docs/connectors/datastream/kafka.md#dynamic-partition-discovery
        props.setProperty(KafkaSourceOptions.PARTITION_DISCOVERY_INTERVAL_MS.key(), partitionDiscoveryIntervalMs);

        // see:https://github.com/apache/flink/blob/release-1.14.4/docs/content/docs/connectors/datastream/kafka.md#starting-offset
        // Start from committed offset, also use EARLIEST as reset strategy if
        // committed offset doesn't exist
        OffsetsInitializer offsets = OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST);
        if (fromOffsetTime > 0) { // By-default
            // Start from the first record whose timestamp is greater than
            // or equals a timestamp.
            offsets = OffsetsInitializer.timestamp(fromOffsetTime);
        }
        KafkaSource<IamEvent> source = KafkaSource.<IamEvent> builder()
                .setBootstrapServers(brokers)
                .setGroupId(groupId)
                .setTopicPattern(Pattern.compile(topicPattern))
                .setStartingOffsets(offsets)
                .setClientIdPrefix(jobName)
                .setProperties(props)
                .setDeserializer(new IamEventKafkaRecordDeserializationSchema())
                .build();

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // see:https://github.com/apache/flink/blob/release-1.14.4/docs/content/docs/connectors/datastream/kafka.md#consumer-offset-committing
        if (nonNull(checkpointMode)) {
            env.enableCheckpointing(checkpointMillis, checkpointMode);
        }
        DataStreamSource<IamEvent> stream = env.fromSource(source,
                IamEventWatermarks.newWatermarkStrategy(ofMillis(outOfOrdernessMillis), ofMillis(idleTimeoutMillis)),
                jobName.concat("Source"));
        if (parallelism > 0) {
            stream.setParallelism(parallelism);
        }
        if (maxParallelism > 0) {
            stream.setMaxParallelism(maxParallelism);
        }
        if (bufferTimeoutMillis > 0) {
            stream.setBufferTimeout(bufferTimeoutMillis);
        }

        if (nonNull(enablePrintSink) && enablePrintSink) {
            stream.addSink(new PrintSinkFunction<>());
        } else {
//            stream.addSink(new HBaseDynamicTableSink(tableName, hbaseTableSchema, hbaseConf, writeOptions, nullStringLiteral));
//            stream.addSink(new HBaseSinkFunction<>(hTableName, conf, mutationConverter, bufferFlushMaxSizeInBytes, bufferFlushMaxMutations, bufferFlushIntervalMillis));
        }

        env.execute(jobName);
    }

}
