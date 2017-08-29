package com.hzgc.streaming.job

import java.util
import com.google.gson.Gson
import com.hzgc.hbase.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl
import com.hzgc.rocketmq.util.RocketMQProducer
import com.hzgc.streaming.alarm.OffLineAlarmMessage
import com.hzgc.streaming.util.{FilterUtils, PropertiesUtils, Utils}
import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.JavaConverters

/**
  * 人脸识别离线告警任务（刘善彬）
  *
  */

object FaceOffLineAlarmJob {
  def main(args: Array[String]): Unit = {
    val offLineAlarmMessage = new OffLineAlarmMessage()
    val properties = PropertiesUtils.getProperties
    val appName = properties.getProperty("job.offLine.appName")
    val master = properties.getProperty("job.offLine.master")
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    val sc = new SparkContext(conf)
    val deviceUtilImpl = new DeviceUtilImpl()
    val offLineAlarmRule = deviceUtilImpl.getThreshold
    val separator = "ZHONGXIAN"
    if (offLineAlarmRule != null && !offLineAlarmRule.isEmpty) {
      println("Start offline alarm task data processing ...")
      val objTypeList = new util.ArrayList[String](offLineAlarmRule.keySet())
      val returnResult = ObjectInfoInnerHandlerImpl.getInstance().searchByPkeysUpdateTime(objTypeList)
      if (returnResult != null && !returnResult.isEmpty) {
        val totalData = sc.parallelize(JavaConverters.asScalaBufferConverter(returnResult).asScala)
        val splitResult = totalData.map(totailDataElem => (totailDataElem.split(separator)(0), totailDataElem.split(separator)(1), totailDataElem.split(separator)(2)))
        val getDays = splitResult.map(splitResultElem => {
          val objRole = offLineAlarmRule.get(splitResultElem._2)
          val objRoleIt = objRole.keySet().iterator()
          var days = 0
          while (objRoleIt.hasNext) {
            val key = objRoleIt.next()
            days = objRole.get(key)
          }
          (splitResultElem._1, splitResultElem._2, splitResultElem._3, days)
        })
        val filterResult = getDays.map(getDaysElem => (getDaysElem._1, getDaysElem._2, getDaysElem._3, Utils.timeTransition(getDaysElem._3), getDaysElem._4)).
          filter(filter => FilterUtils.dayFilterFun(filter._5.toString, filter._4))
        //将离线告警信息推送到MQ
        filterResult.foreach(filterResultElem => {
          val rocketMQProducer = RocketMQProducer.getInstance()
          val offLineAlarmMessage = new OffLineAlarmMessage()
          val gson = new Gson()
          offLineAlarmMessage.setAlarmType(DeviceTable.OFFLINE.toString)
          offLineAlarmMessage.setStaticID(filterResultElem._1)
          offLineAlarmMessage.setUpdateTime(filterResultElem._3)
          val alarmStr = gson.toJson(offLineAlarmMessage)
          //离线告警信息推送的时候，平台id为对象类型字符串的前4个字节。
          val platID = filterResultElem._2.substring(0, 4)
          rocketMQProducer.send(platID, "alarm_" + DeviceTable.OFFLINE.toString, filterResultElem._1, alarmStr.getBytes(), null);
        })
      } else {
        println("No data was received from the static repository,the task is not running！")
      }
    } else {
      println("Object types have been dispatched offline alarm,the task is not running！")
    }
    sc.stop()
  }

}
