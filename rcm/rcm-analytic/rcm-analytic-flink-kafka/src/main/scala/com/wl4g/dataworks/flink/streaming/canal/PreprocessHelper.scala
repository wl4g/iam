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

import com.wl4g.dataworks.flink.streaming.canal.CanalKafka2HbaseStreaming.CanalModel
import com.wl4g.dataworks.core.utils.JacksonUtils
import org.apache.flink.streaming.api.scala.DataStream
import com.wl4g.dataworks.flink.streaming.canal.CanalKafka2HbaseStreaming.HBaseModel

/**
 * Flink streaming preprocess task helper.
 *
 * @author <wanglsir@gmail.com, 983708408@qq.com>
 * @date 2021-09-09
 */
object PreprocessHelper {
  private val defaultFamilyName = "info"

  def process(watermarkData: DataStream[CanalModel]) = {
    // Transform to the HBaseModel.
    import org.apache.flink.streaming.api.scala._
    watermarkData.flatMap {
      canal =>
        val opType = canal.crudType
        // Generate the corresponding HBase table name in the format of 'mysql.dbname.tablename'.
        val hTableName = s"mysql.${canal.dbName}.${canal.tableName}"
        // TODO
        val colNameValuePairsList = parseToColumnKVWrapper(canal.columnValues)

        // Gets the primary key of MySQL table and the value of the first column.
        val rowkey = colNameValuePairsList(0).colValue
        canal.crudType match {
          case "INSERT" => // INSERT operation, to convert all column values to HBaseModel
            colNameValuePairsList.map {
              result => HBaseModel(opType, hTableName, defaultFamilyName, rowkey, result.colName, result.colValue)
            }
          case "UPDATE" => // UPDATE operation, Only columns with isValid=true are converted to HBaseModel
            colNameValuePairsList.filter(_.isValid).map {
              result => HBaseModel(opType, hTableName, defaultFamilyName, rowkey, result.colName, result.colValue)
            }
          case "DELETE" => // DELETE operation, Only a list of HBaseModel of delete is generated.
            List(HBaseModel(opType, hTableName, defaultFamilyName, rowkey, "", ""))
        }
    }
  }

  private def parseToColumnKVWrapper(columnValues: String) = {
    import collection.JavaConverters._
    // Create a mutable set that maps column name and column value pairs.
    val colNameValuePairList = scala.collection.mutable.ListBuffer[ColumnKVWrapper]()
    val jsonNode = JacksonUtils.readTree(columnValues)
    jsonNode.elements().asScala.foreach(json => {
      // e.g: {"columnName":"commodityId","columnValue":"100","isValid":false}
      colNameValuePairList += ColumnKVWrapper(
        json.at("/columnName").asText,
        json.at("/columnValue").asText,
        json.at("/isValid").asBoolean)
    })
    colNameValuePairList.toList
  }

  // This case class is used to packaging the column names in columnValues.
  case class ColumnKVWrapper(
    colName:  String,
    colValue: String,
    isValid:  Boolean)

}