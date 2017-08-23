package com.hzgc.streaming.newjob

import java.util

import com.google.gson.Gson
import com.hzgc.ftpserver.util.FtpUtil
import com.hzgc.hbase.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl
import com.hzgc.jni.FaceFunction
import com.hzgc.rocketmq.util.RocketMQProducer
import com.hzgc.streaming.alarm.{Item, RecognizeAlarmMessage}
import com.hzgc.streaming.util.{FilterUtils, PropertiesUtils}
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
    val appName = PropertiesUtils.getPropertiesValue("job.recognizeAlarm.appName")
    val master = PropertiesUtils.getPropertiesValue("job.recognizeAlarm.master")
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    val ssc = new StreamingContext(conf, Durations.seconds(3))
    //获取动态人脸照片的DStream，返回类型为：[Tuple2[String, Array[Byte]]]
    val kafkaGroupId = PropertiesUtils.getPropertiesValue("kafka.FaceRecognizeAlarmJob.group.id")
    val topics = Set(PropertiesUtils.getPropertiesValue("kafka.topic.name"))
    val brokers = PropertiesUtils.getPropertiesValue("kafka.metadata.broker.list")
    val kafkaParams = Map(
      "metadata.broker.list" -> brokers,
      "group.id" -> kafkaGroupId
    )
    val kafkaDynamicPhoto = KafkaUtils.createDirectStream[String, Array[Byte], StringDecoder, DefaultDecoder](ssc, kafkaParams, topics)
    //对提取着特征值失败的进行过滤，将特征值由字节数组转化为String（注意编码方式）,并通过kafkaId来获取设备id.(dynamicID,deviceID,dynamicFeatureStr)
    val getDeviceID = kafkaDynamicPhoto.map(dPhoto => (dPhoto._1, FtpUtil.getRowKeyMessage(dPhoto._1).get("ipcID"), new String(dPhoto._2, "ISO-8859-1")))
    /**
      * 将从kafka读取的数据根据是否具有识别告警进行过滤
      * 17130NCY0HZ0001-T_00000000000000_170523160015_0000004015_02
      * (dynamicID,deviceID,platID,REC_ObjList,REC_SimList,ADD_ObjList,ADD_DayList,dynamicFeatureStr)
      */

    val filterResult = getDeviceID.map(gdPair => {
      var totalList = JavaConverters.asScalaBufferConverter(ObjectInfoInnerHandlerImpl.getInstance().getTotalList).asScala
//      println("current list:" + totalList.length)
//      println("objectInfoInnerHandlerImpl old:" + ObjectInfoInnerHandlerImpl.getInstance().hashCode())
      //(dynamicID,deviceID,dynamicFeatureStr)
      val platID = deviceUtilI.getplatfromID(gdPair._2)
      if (platID != null && platID.length > 0) {
        //通过设备id获取告警规则
        val alarmRule = deviceUtilI.isWarnTypeBinding(gdPair._2)
        if (alarmRule != null && !alarmRule.isEmpty) {
          val recognizeWarnRule = alarmRule.get(DeviceTable.IDENTIFY)
          if (recognizeWarnRule != null && !recognizeWarnRule.isEmpty) {
            val offLineWarnRule = alarmRule.get(DeviceTable.OFFLINE)
            val recognizeIt = recognizeWarnRule.keySet().iterator()
            var recognizeObjTypeList = ArrayBuffer[String]()
            val resultList = new util.ArrayList[String]()
            var setSim = 0
            while (recognizeIt.hasNext) {
              val recognizeKey = recognizeIt.next()
              recognizeObjTypeList += recognizeKey
              setSim = recognizeWarnRule.get(recognizeKey)
            }
            val listResult = totalList.filter(listFilter => FilterUtils.rangeFilterFun(recognizeObjTypeList.toArray, listFilter.split("ZHONGXIAN")(1)))
            if (offLineWarnRule != null && !offLineWarnRule.isEmpty) {
              var offLineObjTypeList = ""
              val offLineIt = offLineWarnRule.keySet().iterator()
              var setdDays = 0
              while (offLineIt.hasNext) {
                val offLineKey = offLineIt.next()
                offLineObjTypeList = offLineObjTypeList + "_" + offLineKey
                setdDays = offLineWarnRule.get(offLineKey)
              }
              //(dynamicID,deviceID,platID,setSim,offLineObjTypeList,setdDays,dynamicFeatureStr,staticId,objType,staticFeatureStr)
              listResult.foreach(staticStoreListElem => {
                resultList.add(gdPair._1 + "ZHONGXIAN" + gdPair._2 + "ZHONGXIAN" + platID + "ZHONGXIAN" + setSim + "ZHONGXIAN" + offLineObjTypeList.substring(1) + "ZHONGXIAN" + setdDays + "ZHONGXIAN" + gdPair._3 + "ZHONGXIAN" + staticStoreListElem)
              })
            } else {
              println("Grab face photo equipment not dispatched off-line type alarm rules！")
              listResult.foreach(staticStoreListElem => {
                resultList.add(gdPair._1 + "ZHONGXIAN" + gdPair._2 + "ZHONGXIAN" + platID + "ZHONGXIAN" + setSim + "ZHONGXIAN" + null + "ZHONGXIAN" + null + "ZHONGXIAN" + gdPair._3 + "ZHONGXIAN" + staticStoreListElem)
              })
            }
            (JavaConverters.asScalaBufferConverter(resultList).asScala)
          } else {
            println("Grab face photo equipment not dispatched to identify the type of alarm rules！")
            (null)
          }
        } else {
          println("Grab face photo equipment not dispatched alarm rules！")
          (null)
        }
      } else {
        println("The device for grabbing face photos does not bind the platform！")
        (null)
      }
    }).filter(filter => filter != null)
    val computeResult = filterResult.map(eachList => {
      val computeList = new util.ArrayList[String]()
      eachList.foreach(elem => {
        //(dynamicID,deviceID,platID,setSim,offLineObjTypeList,setdDays,dynamicFeatureStr,staticId,objType,staticFeatureStr)
        val elemArray = elem.split("ZHONGXIAN")
        //计算相似度
        val setSim = elem.split("ZHONGXIAN")(3)
        val simResult = FaceFunction.featureCompare(elemArray(6), elemArray(9))
        var computeStr = ""
        if (simResult > setSim.toFloat) {
          computeStr = elemArray(0) + "ZHONGXIAN" + elemArray(1) + "ZHONGXIAN" + elemArray(2) + "ZHONGXIAN" +
            elemArray(3) + "ZHONGXIAN" + elemArray(4) + "ZHONGXIAN" + elemArray(5) + "ZHONGXIAN" +
            elemArray(7) + "ZHONGXIAN" + elemArray(8) + "ZHONGXIAN" + simResult
        } else {
          computeStr = null
        }
        computeList.add(computeStr)
      })
      (JavaConverters.asScalaBufferConverter(computeList).asScala)
    }).map(computeResultList => computeResultList.filter(computeResultListF => computeResultListF != null)).filter(fil => fil.size != 0)

    /**
      * 进行告警推送，识别时间更新操作
      */
    computeResult.foreachRDD(rdd => {
      rdd.foreachPartition(pResult => {
        pResult.foreach(rddElem => {
          val gson = new Gson()
          val recognizeAlarmMessage = new RecognizeAlarmMessage()
          //一条告警信息
          // //(dynamicID,deviceID,platID,setSim,offLineObjTypeList,setdDays,staticId,objType,simResult)
          var dynamicID = ""
          var deviceID = ""
          var platID = ""
          val items = ArrayBuffer[Item]()
          rddElem.foreach(rddElemStr => {
            val item = new Item()
            val recognizeAlarmStr = rddElemStr.split("ZHONGXIAN")
            val offLineObjTypeList = recognizeAlarmStr(4)
            val objType = recognizeAlarmStr(7)
            dynamicID = recognizeAlarmStr(0)
            deviceID = recognizeAlarmStr(1)
            platID = recognizeAlarmStr(2)
            //识别时间更新(更新因为是一条一条的更新，速度目前比较慢，后续需要优化，一次更新一个list)
            if (offLineObjTypeList != null && offLineObjTypeList.length > 0) {
              val offLineObjTypeListArr = recognizeAlarmStr(4).split("_")
              if (FilterUtils.rangeFilterFun(offLineObjTypeListArr, objType)) {
                ObjectInfoInnerHandlerImpl.getInstance().updateObjectInfoTime(recognizeAlarmStr(6))
              }
            }
            item.setStaticID(recognizeAlarmStr(6))
            item.setSimilarity(recognizeAlarmStr(8))
            items += item
          })
          recognizeAlarmMessage.setAlarmType(DeviceTable.IDENTIFY.toString)
          recognizeAlarmMessage.setDynamicDeviceID(deviceID)
          recognizeAlarmMessage.setDynamicID(dynamicID)
          recognizeAlarmMessage.setItems(items.toArray)
          val recognizeAlarmResult = gson.toJson(recognizeAlarmMessage)
          println(recognizeAlarmResult)
          val rocketMQProducer = RocketMQProducer.getInstance()
          rocketMQProducer.send(platID, DeviceTable.IDENTIFY.toString, dynamicID, recognizeAlarmResult.getBytes(), null)
        })

      })
    })
    ssc.start()
    ssc.awaitTermination()
  }
}
