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
package com.wl4g.iam.rcm.analytic.core;

import static java.time.Duration.ofMillis;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

import java.util.Properties;

import org.apache.commons.cli.ParseException;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.api.connector.source.SourceSplit;
import org.apache.flink.connector.kafka.source.KafkaSourceOptions;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;

import com.wl4g.iam.rcm.eventbus.common.IamEvent;
import com.wl4g.infra.common.cli.CommandLineTool;
import com.wl4g.infra.common.cli.CommandLineTool.CommandLineFacade;

import lombok.Getter;

/**
 * {@link IamFlinkStreamingBase}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-07 v3.0.0
 * @since v3.0.0
 */
@Getter
public abstract class IamFlinkStreamingBase implements Runnable {

    // KAFKA options.
    private String brokers;
    private String topicPattern;
    private String groupId;
    private Long fromOffsetTime;

    // Checkpoint options.
    private CheckpointingMode checkpointMode;
    private Long checkpointMillis;

    // Performance options.
    private Integer parallelism;
    private Integer maxParallelism;
    private Long bufferTimeoutMillis;
    private Long outOfOrdernessMillis;
    private Long idleTimeoutMillis;
    private String partitionDiscoveryIntervalMs;

    // Sink options.
    private Boolean forceEnablePrintSink;

    // Job options.
    private String jobName;

    // Command line.
    protected transient final CommandLineTool.Builder builder;
    protected transient CommandLineFacade line;
    protected transient Properties props;

    protected IamFlinkStreamingBase() {
        this.builder = CommandLineTool.builder()
                // KAFKA options.
                .option("b", "brokers", "localhost:9092", "Connect kafka brokers addresses.")
                .option("t", "topicPattern", "iam_event", "Kafka topic regex pattern.")
                .mustOption("g", "groupId", "Kafka source consumer group id.")
                .longOption("fromOffsetTime", "-1",
                        "Start consumption from the first record with a timestamp greater than or equal to a certain timestamp. if <=0, it will not be setup and keep the default behavior.")
                // Checkpoint options.
                .longOption("checkpointMode", null,
                        "Sets the checkpoint mode, the default is null means not enabled. options: "
                                + asList(CheckpointingMode.values()))
                .longOption("checkpointMillis", "500",
                        "Checkpoint execution interval millis, only valid when checkpointMode is sets.")
                // Performance options.
                .longOption("parallelism", "-1",
                        "The parallelism for operator. if <=0, it will not be setup and keep the default behavior.")
                .longOption("maxParallelism", "-1",
                        "The maximum parallelism for operator. if <=0, it will not be setup and keep the default behavior.")
                .longOption("bufferTimeoutMillis", "-1",
                        "Parallelism for this operator, if <=0, it will not be setup and keep the default behavior.")
                .longOption("outOfOrdernessMillis", "120000", "The maximum millis out-of-orderness watermark generator assumes.")
                .longOption("idleTimeoutMillis", "30000", "The timeout millis for the idleness detection.")
                .longOption("partitionDiscoveryIntervalMs", "30000", "The per millis for discover new partitions interval.")
                // Sink options.
                .longOption("forceEnablePrintSink", "true", "Force override set to stdout print sink function.")
                // Job options.
                .longOption("jobName", "IamKafkaSourceJob", "Flink kafka source streaming job name.");
    }

    /**
     * Parsing command arguments to {@link CommandLineFacade}.
     * 
     * @param args
     * @return
     * @throws ParseException
     */
    protected IamFlinkStreamingBase parse(String[] args) throws ParseException {
        this.line = builder.helpIfEmpty(args).build(args);
        // KAFKA options.
        this.brokers = line.get("brokers");
        this.topicPattern = line.get("topicPattern");
        this.groupId = line.get("groupId");
        this.fromOffsetTime = line.getLong("fromOffsetTime");
        // Checkpoint options.
        this.checkpointMode = line.getEnum("checkpointMode", CheckpointingMode.class);
        this.checkpointMillis = line.getLong("checkpointMillis");
        // Performance options.
        this.parallelism = line.getInteger("parallelism");
        this.maxParallelism = line.getInteger("maxParallelism");
        this.bufferTimeoutMillis = line.getLong("bufferTimeoutMillis");
        this.outOfOrdernessMillis = line.getLong("outOfOrdernessMillis");
        this.idleTimeoutMillis = line.getLong("idleTimeoutMillis");
        this.partitionDiscoveryIntervalMs = line.get("partitionDiscoveryIntervalMs");
        // Sink options.
        this.forceEnablePrintSink = line.getBoolean("forceEnablePrintSink");
        // Job options.
        this.jobName = line.get("jobName");
        return this;
    }

    /**
     * Configuring custom FLINK environment properties.
     * 
     * @return
     */
    protected void customProps(Properties props) {
    }

    /**
     * Create FLINK source.
     * 
     * @return
     */
    protected abstract <T, S extends SourceSplit, E> Source<T, S, E> createSource();

    /**
     * Configuring custom FLINK data stream.
     * 
     * @return
     */
    protected abstract IamFlinkStreamingBase customStream(DataStreamSource<IamEvent> dataStream);

    /**
     * Handling job execution result.
     * 
     * @param result
     */
    protected void handleJobExecutionResult(JobExecutionResult result) {
    }

    @Override
    public void run() {
        this.props = (Properties) System.getProperties().clone();
        // props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,brokers);
        // props.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        // see:https://github.com/apache/flink/blob/release-1.14.4/docs/content/docs/connectors/datastream/kafka.md#consumer-offset-committing
        // props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false");
        // props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"10");
        // props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        // props.setProperty(FlinkKafkaConsumerBase.KEY_DISABLE_METRICS,"true");
        // see:https://github.com/apache/flink/blob/release-1.14.4/docs/content/docs/connectors/datastream/kafka.md#dynamic-partition-discovery
        props.setProperty(KafkaSourceOptions.PARTITION_DISCOVERY_INTERVAL_MS.key(), partitionDiscoveryIntervalMs);

        // Custom properties.
        customProps(props);

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // see:https://github.com/apache/flink/blob/release-1.14.4/docs/content/docs/connectors/datastream/kafka.md#consumer-offset-committing
        if (nonNull(checkpointMode)) {
            env.enableCheckpointing(checkpointMillis, checkpointMode);
        }
        DataStreamSource<IamEvent> dataStream = env.fromSource(createSource(),
                IamEventWatermarks.newWatermarkStrategy(ofMillis(outOfOrdernessMillis), ofMillis(idleTimeoutMillis)),
                jobName.concat("Source"));
        if (parallelism > 0) {
            dataStream.setParallelism(parallelism);
        }
        if (maxParallelism > 0) {
            dataStream.setMaxParallelism(maxParallelism);
        }
        if (bufferTimeoutMillis > 0) {
            dataStream.setBufferTimeout(bufferTimeoutMillis);
        }

        if (nonNull(forceEnablePrintSink) && forceEnablePrintSink) {
            dataStream.addSink(new PrintSinkFunction<>());
        } else {
            customStream(dataStream);
        }

        try {
            handleJobExecutionResult(env.execute(jobName));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
