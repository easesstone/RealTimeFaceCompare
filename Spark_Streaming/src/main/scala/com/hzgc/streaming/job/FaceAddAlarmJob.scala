package com.hzgc.streaming.job

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.google.gson.Gson
import com.hzgc.ftpserver.util.FtpUtil
import com.hzgc.hbase.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl
import com.hzgc.jni.FaceFunction
import com.hzgc.rocketmq.util.RocketMQProducer
import com.hzgc.streaming.alarm.AddAlarmMessage
import com.hzgc.streaming.util.PropertiesUtils
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Durations, StreamingContext}

import scala.collection.JavaConverters
import scala.collection.mutable.ArrayBuffer

/**
  * 人脸新增告警实时处理任务（刘善彬）
  */
object FaceAddAlarmJob {
  def main(args: Array[String]): Unit = {
    val deviceUtilI = new DeviceUtilImpl()
    val separator = "ZHONGXIAN"
    val properties = PropertiesUtils.getProperties
    val appName = properties.getProperty("job.addAlarm.appName")
    val master = properties.getProperty("job.addAlarm.master")
    val timeInterval = Durations.seconds(properties.getProperty("job.addAlarm.timeInterval").toLong)
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    val ssc = new StreamingContext(conf, timeInterval)
    val kafkaGroupId = properties.getProperty("kafka.FaceAddAlarmJob.group.id")
    val topics = Set(properties.getProperty("kafka.topic.name"))
    val brokers = properties.getProperty("kafka.metadata.broker.list")
    val kafkaParams = Map(
      "metadata.broker.list" -> brokers,
      "group.id" -> kafkaGroupId
    )
    val kafkaDynamicPhoto = KafkaUtils.createDirectStream[String, Array[Byte], StringDecoder, DefaultDecoder](ssc, kafkaParams, topics)
    val getDeviceID = kafkaDynamicPhoto.map(dPhoto => (dPhoto._1, FtpUtil.getRowKeyMessage(dPhoto._1).get("ipcID"), new String(dPhoto._2, "ISO-8859-1")))
    /**
      * 将从kafka读取的数据根据是否具有识别告警进行过滤
      * 处理数据格式：(dynamicID,deviceID,platID,REC_ObjList,REC_SimList,ADD_ObjList,ADD_DayList,dynamicFeatureStr)
      */
    val filterResult = getDeviceID.map(gdPair => {
      var totalList = JavaConverters.asScalaBufferConverter(ObjectInfoInnerHandlerImpl.getInstance().getTotalList).asScala
      val platID = deviceUtilI.getplatfromID(gdPair._2)
      if (platID != null && platID.length > 0) {
        val alarmRule = deviceUtilI.isWarnTypeBinding(gdPair._2)
        if (alarmRule != null && !alarmRule.isEmpty) {
          val addWarnRule = alarmRule.get(DeviceTable.ADDED)
          if (addWarnRule != null && !addWarnRule.isEmpty) {
            val addIt = addWarnRule.keySet().iterator()
            val resultL = new util.ArrayList[String]()
            var setSim = 0
            while (addIt.hasNext) {
              val addKey = addIt.next()
              setSim = addWarnRule.get(addKey)
            }
            totalList.foreach(listResultElem => {
              val listResultElemArr = listResultElem.split(separator)
              if (addWarnRule.containsKey(listResultElemArr(1))) {
                val simResult = FaceFunction.featureCompare(gdPair._3, listResultElemArr(2))
                if (simResult > setSim) {
                  resultL.add(listResultElem.substring(0, listResultElem.lastIndexOf(separator)))
                }
              }
            })
            (gdPair._1, gdPair._2, platID, JavaConverters.asScalaBufferConverter(resultL).asScala)
          } else {
            println("The device [" + gdPair._2 + "] not dispatched added the type of alarm rules！")
            (null)
          }
        } else {
          println("The device [" + gdPair._2 + "] not dispatched alarm rules！")
          (null)
        }
      } else {
        println("The device [" + gdPair._2 + "] not bind the plat！")
        (null)
      }
    }).filter(filter => filter != null)

    /**
      * 进行告警推送
      * (dynamicID,deviceID,platID,(dynamicID,deviceID,platID,staticID,objType))
      */
    filterResult.foreachRDD(rdd => {
      rdd.foreachPartition(pResult => {
        val gson = new Gson()
        val addAlarmMessage = new AddAlarmMessage()
        val rocketMQProducer = RocketMQProducer.getInstance()
        pResult.foreach(rddElem => {
          if (rddElem._4.size == 0 || rddElem._4 == null) {
            val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            val dateStr = df.format(new Date());
            addAlarmMessage.setAlarmType(DeviceTable.ADDED.toString)
            addAlarmMessage.setDynamicDeviceID(rddElem._2)
            addAlarmMessage.setDynamicID(rddElem._1)
            addAlarmMessage.setAlarmTime(dateStr)
            val strgson = gson.toJson(addAlarmMessage)
            rocketMQProducer.send(rddElem._3, "alarm_" + DeviceTable.ADDED.toString, rddElem._1, strgson.getBytes(), null)

          }
        })
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }


}
