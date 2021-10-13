/*
 * Copyright 2017 ~ 2035 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.dataworks.flink.streaming.canal

import java.util.Properties

import scala.beans.BeanProperty

import org.apache.commons.codec.binary.Base64
import org.apache.commons.lang3.StringUtils.isBlank
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010

import com.wl4g.dataworks.core.helper.CrudHbaseHelper
import com.wl4g.dataworks.core.utils.JacksonUtils
import com.wl4g.dataworks.core.utils.Asserts
import com.google.common.annotations.Beta
import java.util.Arrays
import scala.collection.mutable.ArrayBuffer
import java.util.ArrayList

/**
 * Alibaba Canal program for saving consumption Apache Kafka data to HBase based on Flink Streaming.
 *
 * @author <wanglsir@gmail.com, 983708408@qq.com>
 * @date 2021-09-09
 * @see https://github.com/alibaba/canal/wiki/ClientExample
 */
@Beta
object CanalKafka2HbaseStreaming {

  def main(args: Array[String]): Unit = {
    if (args.length < 6) {
      System.err.println(s"""
        |Usage: [OPTIONS]
    		|  <kafkaBrokers>              - Kafka brokers cluster connection string.
    		|  <kafkaZkServers>            - Kafka zookeeper cluster connection string. (@deprecated TODO remove, use Kafka direct consumptino API)
    		|  <kafkaGroupId>              - Kafka consumption properties group ID.
    	  |  <flinkParallelism>          - Flink consumption parallelism. (the develop environment, it is recommended to set to 1, default: 3)
        |  <hbaseZkServers>            - HBase zookeeper servers connection string.
        |  <customConfigJsonBase64>    - The custom configuration description of saving the topics of the consumption Kafka to the HBase tables(Base64 encoded json).
        """.stripMargin)
      System.exit(1)
    }
    // Initialize configuration.
    val Array(kafkaBrokers, kafkaZkServers, kafkaGroupId, flinkParallelism, hbaseZkServers, customConfigJsonBase64) = args
    val configJson = new String(Base64.decodeBase64(customConfigJsonBase64))
    val config = JacksonUtils.fromJson[CanalKafka2HbaseConfig](configJson).asInstanceOf[CanalKafka2HbaseConfig]

    // Check arguments.
    Asserts.notNullElements(config.flinkKafkaCanalTopics, "config.flinkKafkaCanalTopics is requires.")
    // Asserts.notNullElements(config.hbaseRowKeyFields, "config.hbaseRowKeyFields is requires.")

    // Initialize by defaults.
    if (config.kafkaConsumerConfigMap.get("enable.auto.commit").isEmpty) config.kafkaConsumerConfigMap += ("enable.auto.commit" -> "false")
    if (config.kafkaConsumerConfigMap.get("auto.commit.interval.ms").isEmpty) config.kafkaConsumerConfigMap += ("auto.commit.interval.ms" -> "5000")
    if (config.kafkaConsumerConfigMap.get("auto.offset.reset").isEmpty) config.kafkaConsumerConfigMap += ("auto.offset.reset" -> "latest")
    if (config.flinkCheckpointIntervalMs <= 0) config.flinkCheckpointIntervalMs = 5000
    if (config.flinkCheckpointMinPauseBetween <= 0) config.flinkCheckpointMinPauseBetween = 1000
    if (config.flinkCheckpointTimeout <= 0) config.flinkCheckpointTimeout = 60000
    if (config.flinkCheckpointMaxConcurrent <= 0) config.flinkCheckpointMaxConcurrent = 1
    if (config.hbaseBatchPutSize <= 0) config.hbaseBatchPutSize = 32

    // Print version
    val appVersion = Option.apply(getClass.getPackage.getImplementationVersion).orElse(Option.apply("master")).get
    println(getClass.getSimpleName + s"-$appVersion, Startup arguments: kafkaBrokers=$kafkaBrokers, kafkaZkServers=$kafkaZkServers, kafkaGroupId=$kafkaGroupId, flinkParallelism=$flinkParallelism, hbaseZkServers=$hbaseZkServers, config=$configJson")

    // Create flink streaming.
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // Sets streaming the time of stream processing to `EventTime`, and use the time of data occurrence for data processing.
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    // Sets parallesism, In the develop environment, it is recommended to set to 1.
    env.setParallelism(flinkParallelism.toInt)
    // 定期发送
    // env.getConfig.setAutoWatermarkInterval(2000)
    // Sets checkpoints.
    if (!isBlank(config.flinkCheckpointDir)) {
      // Sets address of checkpoint.
      env.setStateBackend(new FsStateBackend(config.flinkCheckpointDir))
      // In order to ensure that the program runs for a long time and prevent
      // interruption from losing the security of data in calculation, checkpoint is required.
      env.enableCheckpointing(config.flinkCheckpointIntervalMs) // milliseconds
      env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
      // Sets the minimum time interval between two checkpoints.
      env.getCheckpointConfig.setMinPauseBetweenCheckpoints(config.flinkCheckpointMinPauseBetween)
      env.getCheckpointConfig.setCheckpointTimeout(config.flinkCheckpointTimeout)
      env.getCheckpointConfig.setMaxConcurrentCheckpoints(config.flinkCheckpointMaxConcurrent)
      // When the program is closed, an extra checkpoint is triggered.
      env.getCheckpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    }

    // Integration KAFKA
    val kafkaProps = new Properties()
    kafkaProps.setProperty("bootstrap.servers", kafkaBrokers)
    // TODO remove, use KAFKA direct consumption API.
    kafkaProps.setProperty("zookeeper.connect", kafkaZkServers)
    kafkaProps.setProperty("group.id", kafkaGroupId)
    kafkaProps.setProperty("enable.auto.commit", config.kafkaConsumerConfigMap.get("enable.auto.commit").get.toString())
    kafkaProps.setProperty("auto.commit.interval.ms", config.kafkaConsumerConfigMap.get("auto.commit.interval.ms").get.toString())
    kafkaProps.setProperty("auto.offset.reset", config.kafkaConsumerConfigMap.get("auto.offset.reset").get.toString()) // latest|earliest|none
    kafkaProps.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kafkaProps.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

    // TODO
    // TODO
    //import collection.JavaConverters._
    val topics: List[String] = config.flinkKafkaCanalTopics.toList
    val kafkaConsumer = new FlinkKafkaConsumer010[String]("cn_south1_canal_db1_table1", new SimpleStringSchema(), kafkaProps)
    import org.apache.flink.streaming.api.scala._
    // import org.apache.flink.api.scala._ // If are read a statically limited data sets.
    val kafkaDStream = env.addSource(kafkaConsumer)

    // Deserialize json data in Kafka to CanalModel.
    val canalDStream = kafkaDStream.map(json => CanalUtil.apply(json))

    // Add the watermark processing of Flink, and the maximum allowable delay time is 2 seconds.
    val watermarkData = canalDStream.assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks[CanalModel] {
      val maxDelayTime = 2000L
      var currentTime: Long = 0L
      override def getCurrentWatermark = new Watermark(currentTime - maxDelayTime)
      override def extractTimestamp(t: CanalModel, l: Long) = {
        currentTime = Math.max(t.crudTime, currentTime)
        currentTime
      }
    })

    val hbaseDStream: DataStream[HBaseModel] = PreprocessHelper.process(watermarkData)
    hbaseDStream.print()
    hbaseDStream.addSink(op => op.crudType match { // 根据操作的类型来进行判断，从而执行对应的操作
      case "DELETE" => CrudHbaseHelper.deleteData(op.tableName, op.rowkey, op.familyName)
      case _        => CrudHbaseHelper.putData(op.tableName, op.rowkey, op.familyName, op.columnName, op.columnValue)
    })

    env.execute(getClass.getSimpleName)
  }

  object CanalUtil {
    def apply(json: String): CanalModel = {
      val jsonNode = JacksonUtils.readTree(json)
      CanalModel(
        jsonNode.at("/emptyCount").asLong,
        jsonNode.at("/binlogFileName").asText,
        jsonNode.at("/binlogFileOffset").asLong,
        jsonNode.at("/dbName").asText,
        jsonNode.at("/tableName").asText,
        jsonNode.at("/columnValues").asText,
        jsonNode.at("/crudType").asText,
        jsonNode.at("/crudTime").asLong)
    }
  }

  case class CanalModel(
    @BeanProperty emptyCount:       Long, // Operation count.
    @BeanProperty binlogFileName:   String,
    @BeanProperty binlogFileOffset: Long,
    @BeanProperty dbName:           String,
    @BeanProperty tableName:        String,
    @BeanProperty columnValues:     String,
    @BeanProperty crudType:         String, // INSERT|DELETE|UPDATE
    @BeanProperty crudTime:         Long) // MySQL CRUD TIMESTAMP

  /*
   * Preprocess the canal sample class into the HbaseModel sample class,
   * mainly to encapsulate the parameters necessary for the HBaseUtils operation.
   */
  case class HBaseModel(
    @BeanProperty crudType:    String,
    @BeanProperty tableName:   String, // binlog_dbname_tabname
    @BeanProperty rowkey:      String,
    @BeanProperty familyName:  String,
    @BeanProperty columnName:  String,
    @BeanProperty columnValue: String) // The first column of data in binlog

  /**
   * FLINK consumption Canal KAFKA arguments configuration definition. </br>
   * <pre>
   * kafkaConsumerConfigMap: KAFKA consumer configuration Properties.
   * flinkCheckpointDir:     FLINK streaming consumption check-pointing directory. like hdfs://emr-header-1:8082/flink-checkpoints.
   * hbaseRowKeyFields:      Generating the RowKey prefix of HBase, that is, the list of field names of JSON data consumed from Kafka is separated by commas. The suffix is the automatically added timestamp (if there is a 'timestamp' field in JSON, use it, otherwise use the current timestamp). For example, if the prefix is specified as 'uid,type', the rowkey is:' uid,type,yyyyMMddHHmmssSSS'.
   * hbaseFamily2ColumnsMap: Saving to HBase table column-family to columns mapping.
   * </pre>
   */
  case class CanalKafka2HbaseConfig(
    @BeanProperty var kafkaConsumerConfigMap:         Map[String, Object], // Optional
    @BeanProperty var flinkKafkaCanalTopics:          ArrayBuffer[String], // Requires
    @BeanProperty var flinkCheckpointDir:             String, // Optional
    @BeanProperty var flinkCheckpointIntervalMs:      Long, // Optional
    @BeanProperty var flinkCheckpointMinPauseBetween: Long, // Optional
    @BeanProperty var flinkCheckpointTimeout:         Long, // Optional
    @BeanProperty var flinkCheckpointMaxConcurrent:   Int, // Optional
    @BeanProperty var hbaseBatchPutSize:              Int, // Optional
    // @BeanProperty var hbaseRowKeyFields:              Array[String], // Requires
    @BeanProperty var hbaseFamily2ColumnsMap: Map[String, Map[String, String]]) // Optional

}