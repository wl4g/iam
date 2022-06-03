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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.accumulators.ListAccumulator;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.operators.StreamMap;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.junit.Test;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link SimpleKafkaSourceTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-01 v3.0.0
 * @since v3.0.0
 * @see https://github.com/apache/flink/blob/release-1.14.4/flink-connectors/flink-connector-kafka/src/test/java/org/apache/flink/connector/kafka/source/KafkaSourceITCase.java
 */
public class SimpleKafkaSourceTests {

    /**
     * Create topic:
     * 
     * <pre>
     * kafka-topics.sh --zookeeper localhost:2181 --create --topic test-topic --partitions 3 --replication-factor 1
     * </pre>
     * 
     * Producer testdata:
     * 
     * <pre>
     * for (( i=1000; i<=9999; i++ )); do echo "mykey-$i:$i" | kafka-console-producer.sh --broker-list localhost:9092 --topic test-topic --property parse.key=true --property key.separator=:; done;
     * </pre>
     * 
     * Consumer testdata:
     * 
     * <pre>
     * kafka-console-consumer.sh --zookeeper localhost:2181 --topic test-topic
     * </pre>
     */
    @SuppressWarnings("serial")
    @Test
    public void testLocalEnvKafkaSourceSinkToPrintForPerPartitionAccumulator() throws Exception {

        KafkaSource<PartitionAndValue> source = KafkaSource.<PartitionAndValue> builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("test-topic")
                .setGroupId("test-group")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setDeserializer(new TestingKafkaRecordDeserializationSchema(true))
                .build();

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStream<PartitionAndValue> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(),
                "testSimpleKafkaSourceRead");

        stream.addSink(new RichSinkFunction<PartitionAndValue>() {
            @Override
            public void open(Configuration parameters) {
                getRuntimeContext().addAccumulator("result", new ListAccumulator<PartitionAndValue>());
            }

            @Override
            public void invoke(PartitionAndValue value, Context context) {
                getRuntimeContext().getAccumulator("result").add(value);
            }
        });

        // Verify that the timestamp and watermark are working fine.
        stream.transform("timestampVerifier", TypeInformation.of(PartitionAndValue.class),
                new WatermarkVerifyingOperator(v -> v));

        stream.addSink(new PrintSinkFunction<>());

        JobExecutionResult result = env.execute();

        List<PartitionAndValue> resultValues = result.getAccumulatorResult("result");

        Map<String, List<Integer>> resultPerPartition = new HashMap<>();

        resultValues.forEach(pv -> resultPerPartition.computeIfAbsent(pv.partition, ignored -> new ArrayList<>()).add(pv.value));

        resultPerPartition.forEach((tp, values) -> {
            int firstExpectedValue = Integer.parseInt(tp.substring(tp.indexOf('-') + 1));
            for (int i = 0; i < values.size(); i++) {
                assertEquals(firstExpectedValue + i, (int) values.get(i),
                        String.format("The %d-th value for partition %s should be %d", i, tp, i));
            }
        });

    }

    @Getter
    @Setter
    @ToString
    private static class PartitionAndValue implements Serializable {
        private static final long serialVersionUID = 4813439951036021779L;
        private String partition;
        private int value;

        public PartitionAndValue() {
        }

        private PartitionAndValue(TopicPartition tp, int value) {
            this.partition = tp.toString();
            this.value = value;
        }
    }

    private static class TestingKafkaRecordDeserializationSchema implements KafkaRecordDeserializationSchema<PartitionAndValue> {
        private static final long serialVersionUID = -3765473065594331694L;
        private transient Deserializer<Integer> deserializer;
        private final boolean enableObjectReuse;
        private final PartitionAndValue reuse = new PartitionAndValue();

        public TestingKafkaRecordDeserializationSchema(boolean enableObjectReuse) {
            this.enableObjectReuse = enableObjectReuse;
        }

        @Override
        public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<PartitionAndValue> collector)
                throws IOException {
            if (deserializer == null) {
                deserializer = new IntegerDeserializer();
            }

            if (enableObjectReuse) {
                reuse.partition = new TopicPartition(record.topic(), record.partition()).toString();
                reuse.value = deserializer.deserialize(record.topic(), record.value());
                collector.collect(reuse);
            } else {
                collector.collect(new PartitionAndValue(new TopicPartition(record.topic(), record.partition()),
                        deserializer.deserialize(record.topic(), record.value())));
            }
        }

        @Override
        public TypeInformation<PartitionAndValue> getProducedType() {
            return TypeInformation.of(PartitionAndValue.class);
        }
    }

    private static class WatermarkVerifyingOperator extends StreamMap<PartitionAndValue, PartitionAndValue> {

        public WatermarkVerifyingOperator(MapFunction<PartitionAndValue, PartitionAndValue> mapper) {
            super(mapper);
        }

        private static final long serialVersionUID = 2868223355944228209L;

        @Override
        public void open() throws Exception {
            getRuntimeContext().addAccumulator("timestamp", new ListAccumulator<Long>());
        }

        @Override
        public void processElement(StreamRecord<PartitionAndValue> element) {
            getRuntimeContext().getAccumulator("timestamp").add(element.getTimestamp());
        }
    }

}
