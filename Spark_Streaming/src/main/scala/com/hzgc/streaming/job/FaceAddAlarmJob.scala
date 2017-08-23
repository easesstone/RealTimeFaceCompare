package com.hzgc.streaming.job

import com.google.gson.Gson
import com.hzgc.ftpserver.util.FtpUtil
import com.hzgc.hbase.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl
import com.hzgc.jni.FaceFunction
import com.hzgc.streaming.alarm.AddAlarmMessage
import com.hzgc.streaming.util.{ComDataUtils, FilterUtils, PropertiesUtils, Utils}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Durations, StreamingContext}

import scala.collection.mutable.ArrayBuffer

/**
  * 人脸新增告警实时处理任务（刘善彬）（备用）
  */

object FaceAddAlarmJob {


  def main(args: Array[String]): Unit = {
    val objectInfoInnerHandlerImpl = ObjectInfoInnerHandlerImpl.getInstance()
    //初始化spark的配置对象
    val appName = PropertiesUtils.getPropertiesValue("job.addAlarm.appName")
    val master = PropertiesUtils.getPropertiesValue("job.addAlarm.master")
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    //初始化StreamingContext对象
    val ssc = new StreamingContext(conf, Durations.seconds(3))
    val addAlarmMessage = new AddAlarmMessage()
    val deviceUtilI = new DeviceUtilImpl()
    val utils = new Utils()
    //获取动态人脸照片的DStream，返回类型为：[Tuple2[String, Array[Byte]]]
    val kafkaGroupId = PropertiesUtils.getPropertiesValue("kafka.FaceAddAlarmJob.group.id")
    val kafkaDynamicPhoto = ComDataUtils.getKafkaDynamicPhoto(ssc, kafkaGroupId)
    //对提取着特征值失败的进行过滤，将特征值由字节数组转化为String（注意编码方式）
    val kafkaDynamicPhotoFilter = kafkaDynamicPhoto.filter(_._2.length != 0).filter(null != _._2).
      map(dPhoto => (dPhoto._1, new String(dPhoto._2, "ISO-8859-1")))
    //通过kafkaId来获取设备id.(dynamicID,deviceID,dynamicFeatureStr)
    val getDeviceID = kafkaDynamicPhotoFilter.map(dPhotoF => (dPhotoF._1, FtpUtil.getRowKeyMessage(dPhotoF._1).get("ipcID"), dPhotoF._2))

    //将从kafka读取的数据根据是否具有识别告警进行过滤
    //(dynamicID,deviceID,platID,REC_ObjList,REC_SimList,ADD_ObjList,ADD_DayList,dynamicFeatureStr)
    val filterResult = getDeviceID.map(gdPair => {
      val dynamicID = gdPair._1
      val deviceID = gdPair._2
      val dynamicFeatureStr = gdPair._3
      //获取平台id
      val platID = deviceUtilI.getplatfromID(deviceID)
      //通过设备id获取新增告警规则
      val isAddWarn = deviceUtilI.isWarnTypeBinding(deviceID)
      if (null != isAddWarn.get(DeviceTable.ADDED)) {
        var strA1 = ""
        var strA2 = ""
        val ksAdd = isAddWarn.get(DeviceTable.ADDED).keySet()
        val itAdd = ksAdd.iterator()
        while (itAdd.hasNext) {
          val key = itAdd.next()
          strA1 = strA1 + "_" + key
          strA2 = strA2 + "_" + isAddWarn.get(DeviceTable.ADDED).get(key.toString)
        }

        (dynamicID, deviceID, platID, strA1.substring(1), strA2.substring(1), dynamicFeatureStr, "1")
      }
      else {
        (dynamicID, deviceID, platID, null, null, dynamicFeatureStr, "0")
      }
    }).filter(filter => filter._7.equals("1")).map(pair => (pair._1, pair._2, pair._3, pair._4, pair._5, pair._6))
    /**
      * kafka获取的流式数据由一系列的RDD组成
      * (dynamicID,deviceID,platID,ADD_ObjList,ADD_DayList,dynamicFeatureStr)
      */
    filterResult.foreachRDD(rdd => {
      //初始化sqlContext对象及隐式转换
      val sqlContext = new SQLContext(ssc.sparkContext)
      import sqlContext.implicits._
      val list = objectInfoInnerHandlerImpl.searchByPkeys(null)
      val staticStoreRDD = ssc.sparkContext.parallelize(Utils.javaList2arrayBuffer(list).toList)
      val splitRDD = staticStoreRDD.map(staticPair => (staticPair.split("ZHONGXIAN")(0), staticPair.split("ZHONGXIAN")(1), staticPair.split("ZHONGXIAN")(2)))
      //将静态数据库映射为staticTable表
      val staticTable = splitRDD.toDF("staticID", "staticObjectType", "staticFeatureStr").registerTempTable("staticTable")
      //将动态数据映射为dynamicTable表
      val dynamicTable = rdd.toDF("dynamicID", "deviceID", "platID", "ADD_ObjList", "ADD_DayList", "dynamicFeatureStr").registerTempTable("dynamicTable")
      //通过笛卡尔积将两张表关联
      val joinTable = sqlContext.sql("select * from dynamicTable cross join staticTable").registerTempTable("joinTable")
      //spark sql自定义函数
      sqlContext.udf.register("comp", (a: String, b: String) => FaceFunction.featureCompare(a, b))
      val similarityResult = sqlContext.sql("select dynamicID,deviceID,platID,ADD_ObjList,ADD_DayList,staticID,staticObjectType,comp(dynamicFeatureStr,staticFeatureStr) as similarity from joinTable")
      val simResu2rdd = similarityResult.rdd.map(row => {
        (row.getAs[String]("dynamicID"),
          row.getAs[String]("deviceID"),
          row.getAs[String]("platID"),
          row.getAs[String]("ADD_ObjList"),
          row.getAs[String]("ADD_DayList"),
          row.getAs[String]("staticID"),
          row.getAs[String]("staticObjectType"),
          row.getAs[Float]("similarity")
        )
      })
      /**
        * 根据设定的相似度过滤出符合标准的数据
        */
      val simFilterResult = simResu2rdd.filter(simFilter => FilterUtils.similarityFilterFun(simFilter._5.split("_")(0), simFilter._8))
      /**
        * 过滤出符合比对范围数据
        */
      val rangeFilterResult = simFilterResult.
        filter(rangeFilter => FilterUtils.rangeFilterFun(rangeFilter._4.split("_"), rangeFilter._7))
      //(kafkaid,ipcid,设备id)
      val dynamicRDDList = rdd.map(dynamicRDDPair => dynamicRDDPair._1 + "=" + dynamicRDDPair._2 + "=" + dynamicRDDPair._3).collect()
      val distinctRDDList = rangeFilterResult.map(rangePair => rangePair._1).distinct(4).collect()
      distinctRDDList.foreach(println)
      val resultArray = ArrayBuffer[String]()
      dynamicRDDList.foreach(elem => {
        if (!distinctRDDList.contains(elem.split("=")(0))) {
          resultArray += elem
        }
      })
      /**
        * 将新增告警结果进行推送
        */
      val gson = new Gson()
      resultArray.foreach(re => {
        //进行推送
        println("推送新增告警信息:::::::?????????????????????????" + re)
        val reElem = re.split("=")
        addAlarmMessage.setAlarmType(DeviceTable.ADDED.toString)
        addAlarmMessage.setDynamicDeviceID(reElem(1))
        addAlarmMessage.setDynamicID(reElem(0))
        val str = gson.toJson(addAlarmMessage)
        println(str)
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }

}
