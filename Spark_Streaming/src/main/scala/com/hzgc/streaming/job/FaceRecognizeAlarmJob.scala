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
import com.hzgc.streaming.alarm.{Item, RecognizeAlarmMessage}
import com.hzgc.streaming.util.{FilterUtils, PropertiesUtils, Utils}
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Durations, StreamingContext}

import scala.collection.JavaConverters
import scala.collection.mutable.ArrayBuffer

/**
  *
  * 人脸识别告警实时处理任务（刘善彬）
  * 技术选型：spark core、streaming、sql
  * 1、kafka获取动态抓取人脸图片（获取的数据为特征值）。
  * 2、Hbase获取静态信息库与亏规则库。
  * 3、在设定的时间间隔进行实时比对。
  */
object FaceRecognizeAlarmJob {
  def main(args: Array[String]): Unit = {
    val deviceUtilI = new DeviceUtilImpl()
    val separator = "ZHONGXIAN"
    val properties = PropertiesUtils.getProperties
    val appName = properties.getProperty("job.recognizeAlarm.appName")
    val master = properties.getProperty("job.recognizeAlarm.master")
    val itemNum = properties.getProperty("job.recognizeAlarm.items.num").toInt
    val timeInterval = Durations.seconds(properties.getProperty("job.recognizeAlarm.timeInterval").toLong)
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    val ssc = new StreamingContext(conf, timeInterval)
    val kafkaGroupId = properties.getProperty("kafka.FaceRecognizeAlarmJob.group.id")
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
          val recognizeWarnRule = alarmRule.get(DeviceTable.IDENTIFY)
          if (recognizeWarnRule != null && !recognizeWarnRule.isEmpty) {
            val offLineWarnRule = alarmRule.get(DeviceTable.OFFLINE)
            val recognizeIt = recognizeWarnRule.keySet().iterator()
            val resultList = new util.ArrayList[String]()
            var setSim = 0
            while (recognizeIt.hasNext) {
              val recognizeKey = recognizeIt.next()
              setSim = recognizeWarnRule.get(recognizeKey)
            }
            if (offLineWarnRule != null && !offLineWarnRule.isEmpty) {
              val offLineObjTypeList = new StringBuffer()
              val offLineIt = offLineWarnRule.keySet().iterator()
              var setdDays = 0
              while (offLineIt.hasNext) {
                val offLineKey = offLineIt.next()
                offLineObjTypeList.append("_").append(offLineKey)
                setdDays = offLineWarnRule.get(offLineKey)
              }
              totalList.foreach(staticStoreListElem => {
                val staticStoreListElemArr = staticStoreListElem.split(separator)
                if (recognizeWarnRule.containsKey(staticStoreListElemArr(1))) {
                  val simResult = FaceFunction.featureCompare(gdPair._3, staticStoreListElemArr(2))
                  if (simResult > setSim) {
                    //(dynamicID,deviceID,platID,offLineObjTypeList,setdDays,staticID,staticObj,simResult)
                    val compStr = new StringBuilder()
                    compStr.append(gdPair._1).append(separator).append( gdPair._2).append(separator).append( platID).append(separator).append(offLineObjTypeList.toString.substring(1)).append(separator).append( setdDays).append(separator).append(staticStoreListElem.substring(0, staticStoreListElem.lastIndexOf(separator))).append(separator).append(simResult)
                    resultList.add(compStr.toString())
                  }
                }
              })
            } else {
              totalList.foreach(staticStoreListElem => {
                val staticStoreListElemArr = staticStoreListElem.split(separator)
                if (recognizeWarnRule.containsKey(staticStoreListElemArr(1))) {
                  val simResult = FaceFunction.featureCompare(gdPair._3, staticStoreListElemArr(2))
                  if (simResult > setSim) {
                    val compStr = new StringBuilder()
                    compStr.append(gdPair._1).append(separator).append( gdPair._2).append(separator).append( platID).append(separator).append(null.toString).append(separator).append(null.toString).append(separator).append(staticStoreListElem.substring(0, staticStoreListElem.lastIndexOf(separator))).append(separator).append(simResult)
                    resultList.add(compStr.toString())
                  }
                }
              })
              println("The device [" + gdPair._2 + "] not dispatched offLine type of alarm rules！")
            }
            (JavaConverters.asScalaBufferConverter(resultList).asScala)
          } else {
            println("The device [" + gdPair._2 + "] not dispatched to recognize the type of alarm rules！")
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
      * 进行告警推送，识别时间更新操作
      */
    filterResult.foreachRDD(rdd => {
      rdd.foreachPartition(pResult => {
        val rocketMQProducer = RocketMQProducer.getInstance()
        val gson = new Gson()
        val recognizeAlarmMessage = new RecognizeAlarmMessage()
        val timeUpdateInstance = ObjectInfoInnerHandlerImpl.getInstance()
        pResult.foreach(rddElem => {
          //(dynamicID,deviceID,platID,offLineObjTypeList,setdDays,staticID,staticObj,simResult)
          if (rddElem != null && rddElem.size > 0) {
            var dynamicID = ""
            var deviceID = ""
            var platID = ""
            val items = ArrayBuffer[Item]()
            val timeUpdateItems = ArrayBuffer[String]()
            rddElem.foreach(rddElemStr => {
              val item = new Item()
              val recognizeAlarmStr = rddElemStr.split(separator)
              val offLineObjTypeList = recognizeAlarmStr(3)
              val objType = recognizeAlarmStr(6)
              dynamicID = recognizeAlarmStr(0)
              deviceID = recognizeAlarmStr(1)
              platID = recognizeAlarmStr(2)
              /**
                * 识别时间更新
                * 将符合更新条件的数据加入list里面
                */
              if (offLineObjTypeList != null && offLineObjTypeList.length > 0) {
                if (FilterUtils.rangeFilterFun(offLineObjTypeList.split("_"), objType)) {
                  timeUpdateItems += recognizeAlarmStr(5)
                }
              }
              item.setStaticID(recognizeAlarmStr(5))
              item.setSimilarity(recognizeAlarmStr(7))
              items += item
            })
            val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            val dateStr = df.format(new Date());
            recognizeAlarmMessage.setAlarmType(DeviceTable.IDENTIFY.toString)
            recognizeAlarmMessage.setDynamicDeviceID(deviceID)
            recognizeAlarmMessage.setDynamicID(dynamicID)
            recognizeAlarmMessage.setAlarmTime(dateStr)
            //对每一条告警消息识别到的静态信息库照片按照相似度进行排序（降序）
            val itemsSortWith = items.sortWith { case (caseItem1, caseItem2) => caseItem1.getSimilarity > caseItem2.getSimilarity }.take(itemNum)
            recognizeAlarmMessage.setItems(itemsSortWith.toArray)
            val recognizeAlarmResult = gson.toJson(recognizeAlarmMessage)
            if (!timeUpdateItems.isEmpty && timeUpdateItems != null) {
              timeUpdateInstance.updateObjectInfoTime(Utils.arrayBuffer2javaList(timeUpdateItems.toArray))
            }
            rocketMQProducer.send(platID, "alarm_" + DeviceTable.IDENTIFY.toString, dynamicID, recognizeAlarmResult.getBytes(), null)
          }
        })
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }
}
