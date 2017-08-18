package com.hzgc.streaming.job

import java.io.FileInputStream
import java.util.Properties

import com.google.gson.Gson
import com.hzgc.ftpserver.util.FtpUtil
import com.hzgc.hbase.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl
import com.hzgc.hbase.util.HBaseHelper
import com.hzgc.jni.FaceFunction
import com.hzgc.rocketmq.util.RocketMQProducer
import com.hzgc.streaming.alarm.{Item, RecognizeAlarmMessage}
import com.hzgc.streaming.util.StreamingUtils
import com.hzgc.util.FileUtil
import kafka.serializer.StringDecoder
import org.apache.rocketmq.client.producer.DefaultMQProducer
import org.apache.rocketmq.common.message.Message
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.JavaConverters
import scala.collection.mutable.ArrayBuffer


object Test {
  val sparkConf: SparkConf = new SparkConf().setAppName("FaceRecognizeAlarmJob").setMaster("yarn-client")
  val ssc = new StreamingContext(sparkConf, Seconds(3))
  case class feature( dynamicID: String,
                      dynamicPlatID: String,
                      dynamicDeviceID: String,
                      dynamicFeature: String,
                      threshold: String,
                      staticID: String,
                      staticObjectType: String,
                      staticFeature: String)
  case class result(dynamicID: String,
                    dynamicPlatID: String,
                    dynamicDeviceID: String,
                    staticID: String,
                    staticObjectType: String,
                    threshold: String,
                    sim: Float)
  case class Json(dynamicID: String,
                    dynamicPlatID: String,
                    dynamicDeviceID: String,
                    staticID: String,
                    staticObjectType: String,
                    sim: String)
  def main(args: Array[String]): Unit = {
    val esClient = new ObjectInfoInnerHandlerImpl()
    val warnClient = new DeviceUtilImpl()
    val separator = "ZHONGXIAN"
    val interrupt = "SHUXIAN"
    val bcSeparator: Broadcast[String] = ssc.sparkContext.broadcast(separator)
    val bcInterrupt: Broadcast[String] = ssc.sparkContext.broadcast(interrupt)
    val args = Array("172.18.18.100:21005", "testface")
    // Usage
    if (args.length < 2) {
      System.err.println(
        s"""
           |Usage: DirectKafkaWordCount <brokers> <topics>
           |  <brokers> is a list of one or more Kafka brokers
           |  <topics> is a list of one or more kafka topics to consume from
           |
        """.stripMargin)
      System.exit(1)
    }
    // 映射 brokers和topic
    val Array(brokers, topics) = args
    // 生成topic列表
    val topicSet = topics.split(",").toSet
    //配置Kafka参数
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)

    //获取Kafka DStream
    val kafkaDstream = KafkaUtils.
      createDirectStream[String, String, StringDecoder, FeatureDecoder](ssc, kafkaParams, topicSet)
    //对Dstream进行转换，将读取到的消息与静态库进行匹配查询，然后关联
    val kafkaFeature = kafkaDstream.map(message => {
      //通过message的key获取ipcID
      val ipcID = FtpUtil.getRowKeyMessage(message._1).get("ipcID")
      //使用StringBuilder来组合一条Kafka消息和底库中符合条件的数据
      val newMessage = new StringBuilder
      //通过设备ID获取平台ID
      val platID = warnClient.getplatfromID(ipcID)
      //通过设备ID获取此设备绑定的布控预案
      val javaObjTyeList = warnClient.isWarnTypeBinding(ipcID)
      if (platID.length > 0 && javaObjTyeList != null) {
        val identifyMap = javaObjTyeList.get(DeviceTable.IDENTIFY)
        if (identifyMap != null && identifyMap.size() > 0) {
          val threshold = StreamingUtils.getSimilarity(identifyMap)
          val javaTypeList = StreamingUtils.getTypeList(identifyMap)
          if (threshold != null && javaTypeList != null) {
            val esResult = esClient.searchByPkeys(javaTypeList)
            val scalaEsResult = JavaConverters.asScalaBufferConverter(esResult).asScala
            scalaEsResult.foreach(result => {
              val tempMessage = bcSeparator.value + message._1 +
                bcSeparator.value + platID +
                bcSeparator.value + ipcID +
                bcSeparator.value + message._2 +
                bcSeparator.value + threshold +
                bcSeparator.value + result +
                bcInterrupt.value
              newMessage.append(tempMessage)
            })
          }
        }
      }
      (ipcID, newMessage.toString())
    }
    )
    kafkaFeature.foreachRDD(rddMessage => {
      rddMessage.foreachPartition(partitionMessage => {
        val hbaseConn = HBaseHelper.getHBaseConnection
        //        val parEsClient = new ObjectInfoHandlerImpl()
        val parWarnClient = new DeviceUtilImpl()
        val parMQProducer = new DefaultMQProducer()
        val fis = new FileInputStream(FileUtil.loadResourceFile("rocketmq.properties"))
        val proper = new Properties()
        proper.load(fis)
        parMQProducer.setProducerGroup(proper.getProperty("group"))
        parMQProducer.setNamesrvAddr(proper.getProperty("address"))
        parMQProducer.start()
        partitionMessage.foreach(message => {
          //          val sqlContext = new org.apache.spark.sql.SQLContext(ssc.sparkContext)
//          import sqlContext.implicits._
          val ipcID = message._1
          val tempMessage = message._2.split(bcInterrupt.value).
            map(_.split(bcSeparator.value)).
            map(f => feature(f(1), f(2), f(3), f(4), f(5), f(6), f(7), f(8))).
            map(record => result(record.dynamicID,
              record.dynamicPlatID,
              record.dynamicDeviceID,
              record.staticID,
              record.staticObjectType,
              record.threshold,
              FaceFunction.featureCompare(record.dynamicFeature, record.staticFeature))).
            filter(record => {
              if (record.sim > Integer.parseInt(record.threshold)) true else false
            }).
            map(record => (record.sim, record)).
            sortBy(x => (x._1, true)).
            map(record => record._2).
            map(json =>
              Json(json.dynamicID, json.dynamicPlatID, json.dynamicDeviceID, json.staticID, json.staticObjectType, json.sim.toString))
          val mqRD = tempMessage.iterator
          val alarmMessage = new RecognizeAlarmMessage()
          val gson = new Gson()
          var dynamicDeviceID = ""
          var dynamicID = ""
          var platID = ""
          val items = ArrayBuffer[Item]()
          alarmMessage.setAlarmType(DeviceTable.IDENTIFY.toString)
          while (mqRD.hasNext) {
            val element = mqRD.next()
            platID = element.dynamicPlatID
            dynamicDeviceID = element.dynamicDeviceID
            dynamicID = element.dynamicID
            val staticID = element.staticID
            val staticObjectType = element.staticObjectType
            val sim = element.sim
            val item = new Item()
            item.setStaticID(staticID)
            item.setSimilarity(sim)
            item.setStaticObjectType(staticObjectType)
            items += item
          }
          alarmMessage.setDynamicDeviceID(dynamicDeviceID)
          alarmMessage.setDynamicID(dynamicID)
          alarmMessage.setItems(items.toArray)
          println("woshidashabi")
          println(alarmMessage.toString)
          println("woshidashabi")
          val mqMessage = new Message(platID, DeviceTable.IDENTIFY.toString, "alarmInfo", gson.toJson(alarmMessage).getBytes())
          parMQProducer.send(mqMessage)
        })
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }
}
