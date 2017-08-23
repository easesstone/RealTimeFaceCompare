package com.hzgc.streaming.job

import com.google.gson.Gson
import com.hzgc.hbase.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl
import com.hzgc.streaming.alarm.OffLineAlarmMessage
import com.hzgc.streaming.util.{FilterUtils, PropertiesUtils, Utils}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 人脸识别离线告警任务（刘善彬）（备用）
  *
  */

object FaceOffLineAlarmJob {
  def main(args: Array[String]): Unit = {
    val offLineAlarmMessage = new OffLineAlarmMessage()
    val propertiesUtils=new PropertiesUtils()
    val objectInfoInnerHandlerImpl = ObjectInfoInnerHandlerImpl.getInstance()
    val appName = propertiesUtils.getPropertiesValue("job.offLine.appName")
    val master = propertiesUtils.getPropertiesValue("job.offLine.master")
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    val sc = new SparkContext(conf)
    val deviceUtilImpl = new DeviceUtilImpl()
    val staticData = objectInfoInnerHandlerImpl.searchByPkeys()
    val staticDataRDD = sc.parallelize(Utils.javaList2arrayBuffer(staticData).toList)
    val filterRdd = staticDataRDD.map(pair => {
      // println(pair.split("ZHONGXIAN").length)
      (pair.split("ZHONGXIAN")(0), pair.split("ZHONGXIAN")(1), pair.split("ZHONGXIAN")(3))
    }).filter(filter => filter._2 != null)

    val rule = filterRdd.map(elem => {
      val offLineAlarmRule = deviceUtilImpl.getThreshold.get(elem._2)
      //因为规则库不齐全。所以要加一个判断
      var str = ""
      if (offLineAlarmRule != null) {
        val it = offLineAlarmRule.keySet().iterator()
        while (it.hasNext) {
          val key = it.next()
          str = offLineAlarmRule.get(key).toString
        }
        (elem._1, elem._2, elem._3, str)
      } else {
        (elem._1, elem._2, elem._3, null)
      }
    }).filter(filt => filt._4 != null)

    val result = rule.
      map(r => (r._1, r._2, r._3, Utils.timeTransition(r._3), r._4)).
      filter(fi => FilterUtils.dayFilterFun(fi._5, fi._4))
    result.collect().foreach(resultElem => {
      val gson = new Gson()
      //将结果推送至MQ
      offLineAlarmMessage.setAlarmType(DeviceTable.OFFLINE.toString)
      offLineAlarmMessage.setStaticID(resultElem._1)
      offLineAlarmMessage.setUpdateTime(resultElem._3)
      val str = gson.toJson(offLineAlarmMessage)
      println(str)
    })

    sc.stop()


  }

}
