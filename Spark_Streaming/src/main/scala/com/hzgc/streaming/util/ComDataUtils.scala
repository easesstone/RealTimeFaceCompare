package com.hzgc.streaming.util

import java.util.{Date, Properties}
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.KafkaUtils

/**
  *
  * 获取比对数据工具类（刘善彬 -> 内）
  */
object ComDataUtils extends Serializable {

  /**
    * SparkStreaming从kafka集群读取photo数据
    *
    * @param ssc streaming上下文环境
    * @return 返回InputDStream
    */
  def getKafkaDynamicPhoto(ssc: StreamingContext, kafkaGroupId: String): InputDStream[Tuple2[String, Array[Byte]]] = {
    val propertiesUtils=new PropertiesUtils()
    val topics = Set(propertiesUtils.getPropertiesValue("kafka.topic.name"))
    val brokers = propertiesUtils.getPropertiesValue("kafka.metadata.broker.list")
    val kafkaParams = Map(
      "metadata.broker.list" -> brokers,
      "group.id" -> kafkaGroupId
    )
    val kafkainput = KafkaUtils.createDirectStream[String, Array[Byte], StringDecoder, DefaultDecoder](ssc, kafkaParams, topics)
    kafkainput
  }

  /**
    * 读取静态信息库的数据
    *
    * @param sc spark上下文环境
    * @return RDD[(ImmutableBytesWritable,Result)]
    */
  def getHbaseStaticPhoto(sc: SparkContext): RDD[(ImmutableBytesWritable, Result)] = {
    val propertiesUtils=new PropertiesUtils()
    val hbaseConf = HBaseConfiguration.create()
    val tableName = propertiesUtils.getPropertiesValue("hbase.table.static.name")
    hbaseConf.set("hbase.zookeeper.quorum", propertiesUtils.getPropertiesValue("hbase.zookeeper.quorum"))
    hbaseConf.set("hbase.zookeeper.property.clientPort", propertiesUtils.getPropertiesValue("hbase.zookeeper.property.clientPort"))
    hbaseConf.set(TableInputFormat.INPUT_TABLE, tableName)
    val rdd = sc.newAPIHadoopRDD(hbaseConf, classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result])
    rdd
  }

  def write2Kafka(str: String, platId: String): Unit = {
    val props = new Properties()
    props.put("metadata.broker.list", "172.18.18.107:21005,172.18.18.108:21005,172.18.18.109:21005") // broker 如果有多个,中间使用逗号分隔
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    props.put("request.required.acks", "1")
    val config = new ProducerConfig(props)
    val producer = new Producer[String, String](config)
    val runtime = new Date().toString
    val topic = platId
    val data = new KeyedMessage[String, String](topic, str)
    producer.send(data)
    producer.close()
    println("警推送至MQ")
  }


}
