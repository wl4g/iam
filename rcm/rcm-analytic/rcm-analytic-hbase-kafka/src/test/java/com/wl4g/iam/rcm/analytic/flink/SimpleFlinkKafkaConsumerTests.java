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
package com.wl4g.iam.rcm.analytic.flink;

import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.Test;

import com.wl4g.infra.common.cli.CommandLineTool;
import com.wl4g.infra.common.cli.CommandLineTool.CommandLineFacade;

/**
 * {@link SimpleFlinkKafkaConsumerTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-03 v3.0.0
 * @since v3.0.0
 */
@SuppressWarnings("deprecation")
public class SimpleFlinkKafkaConsumerTests {

    @Test
    public void testFlinkKafkaConsumer() throws Exception {
        String[] args = { "--groupId", "testFlinkKafkaConsumerGroup" };

        CommandLineFacade line = CommandLineTool.builder()
                .option("b", "brokers", "localhost:9092", "Connect kafka brokers addresses.")
                .mustOption("g", "groupId", "Kafka consumer group id.")
                .option("t", "topicPattern", "test-topic", "Kafka consumer topic regex pattern.")
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
                .option("N", "jobName", "testFlinkKafkaConsumerJob", "Flink kafka consumer job name.")
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
        String jobName = line.get("jobName");

        Properties props = (Properties) System.getProperties().clone();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // //see:https://github.com/apache/flink/blob/release-1.14.4/docs/content/docs/connectors/datastream/kafka.md#consumer-offset-committing
        // props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");
        // props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        props.setProperty(FlinkKafkaConsumerBase.KEY_DISABLE_METRICS, "true");
        // see:https://github.com/apache/flink/blob/release-1.14.4/docs/content.zh/docs/connectors/datastream/kafka.md#kafka-consumer-topic-和分区发现
        props.setProperty(FlinkKafkaConsumerBase.KEY_PARTITION_DISCOVERY_INTERVAL_MILLIS, "30");
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(Pattern.compile(topicPattern),
                new SimpleStringSchema(), props);
        kafkaConsumer.setCommitOffsetsOnCheckpoints(true);
        if (nonNull(fromOffsetTime) && fromOffsetTime > 0) {
            // Start from the first record whose timestamp is greater than
            // or equals a timestamp.
            kafkaConsumer.setStartFromTimestamp(fromOffsetTime);
        } else {
            kafkaConsumer.setStartFromLatest();
            // kafkaConsumer.setStartFromEarliest();
        }
        // see:https://github.com/apache/flink/blob/release-1.14.4/docs/content.zh/docs/connectors/datastream/kafka.md#kafka-consumer-和-时间戳抽取以及-watermark-发送
        kafkaConsumer.assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(ofSeconds(20)));

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStream<String> stream = env.addSource(kafkaConsumer);// Legacy-API
        // stream=env.fromSource(kafkaConsumer,WatermarkStrategy.noWatermarks(),"testSimpleKafkaSourceRead");//New-API
        stream.addSink(new PrintSinkFunction<>());

        // see:https://github.com/apache/flink/blob/release-1.14.4/docs/content/docs/connectors/datastream/kafka.md#consumer-offset-committing
        if (nonNull(checkpointMode)) {
            env.enableCheckpointing(checkpointMillis, checkpointMode);
        }
        if (parallelism > 0) {
            env.setParallelism(parallelism);
        }
        if (maxParallelism > 0) {
            env.setMaxParallelism(maxParallelism);
        }
        if (bufferTimeoutMillis > 0) {
            env.setBufferTimeout(bufferTimeoutMillis);
        }

        System.out.println("Execution ...");
        env.execute(jobName);
    }

}
