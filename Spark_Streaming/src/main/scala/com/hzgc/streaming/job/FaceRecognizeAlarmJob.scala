package com.hzgc.streaming.job

import com.google.gson.Gson
import com.hzgc.ftpserver.util.FtpUtil
import com.hzgc.hbase.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl
import com.hzgc.jni.FaceFunction
import com.hzgc.streaming.alarm.{Item, RecognizeAlarmMessage}
import com.hzgc.streaming.util.{ComDataUtils, FilterUtils, PropertiesUtils, Utils}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Durations, StreamingContext}

import scala.collection.mutable.ArrayBuffer

/**
  *
  * 人脸识别告警实时处理任务（刘善彬）（备用）
  * 技术选型：spark core、streaming、sql
  * 1、kafka获取动态抓取人脸图片（获取的数据为特征值）。
  * 2、Hbase获取静态信息库与亏规则库。
  * 3、在设定的时间间隔进行实时比对。
  */
object FaceRecognizeAlarmJob {
  def main(args: Array[String]): Unit = {
    val objectInfoInnerHandlerImpl = ObjectInfoInnerHandlerImpl.getInstance()
    val deviceUtilI = new DeviceUtilImpl()
    val propertiesUtils=new PropertiesUtils()
    //初始化spark的配置对象
    val appName = propertiesUtils.getPropertiesValue("job.recognizeAlarm.appName")
    val master = propertiesUtils.getPropertiesValue("job.recognizeAlarm.master")
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    //初始化StreamingContext对象
    val ssc = new StreamingContext(conf, Durations.seconds(3))
    var list = objectInfoInnerHandlerImpl.searchByPkeys(null)
    var flag = false
    if(flag==false){
      list = objectInfoInnerHandlerImpl.searchByPkeys(null)

    }
    val staticStoreRDD = ssc.sparkContext.parallelize(Utils.javaList2arrayBuffer(list).toList)

    //获取动态人脸照片的DStream，返回类型为：[Tuple2[String, Array[Byte]]]
    val kafkaGroupId = propertiesUtils.getPropertiesValue("kafka.FaceRecognizeAlarmJob.group.id")
    val kafkaDynamicPhoto = ComDataUtils.getKafkaDynamicPhoto(ssc, kafkaGroupId)
    //对提取着特征值失败的进行过滤，将特征值由字节数组转化为String（注意编码方式）
    val kafkaDynamicPhotoFilter = kafkaDynamicPhoto.filter(_._2.length != 0).filter(null != _._2).
      map(dPhoto => (dPhoto._1, new String(dPhoto._2, "ISO-8859-1")))
    //通过kafkaId来获取设备id.(dynamicID,deviceID,dynamicFeatureStr)
    val getDeviceID = kafkaDynamicPhotoFilter.map(dPhotoF => (dPhotoF._1, FtpUtil.getRowKeyMessage(dPhotoF._1).get("ipcID"), dPhotoF._2))
    /**
      * 将从kafka读取的数据根据是否具有识别告警进行过滤
      * 17130NCY0HZ0001-T_00000000000000_170523160015_0000004015_02
      * (dynamicID,deviceID,platID,REC_ObjList,REC_SimList,ADD_ObjList,ADD_DayList,dynamicFeatureStr)
      */
    val filterResult = getDeviceID.map(gdPair => {
      val dynamicID = gdPair._1
      val deviceID = gdPair._2
      val dynamicFeatureStr = gdPair._3
      //获取平台id
      val platID = deviceUtilI.getplatfromID(deviceID)
      //通过设备id获取告警规则
      val isRecognizeWarn = deviceUtilI.isWarnTypeBinding(deviceID).get(DeviceTable.IDENTIFY)
      val isAddWarn = deviceUtilI.isWarnTypeBinding(deviceID).get(DeviceTable.OFFLINE)
      if (null != isRecognizeWarn) {
        var strR1 = ""
        var strR2 = ""
        var strA1 = ""
        var strA2 = ""
        val ks = isRecognizeWarn.keySet()
        val ksAdd = isAddWarn.keySet()
        val it = ks.iterator()
        val itAdd = ksAdd.iterator()
        if (null != isAddWarn) {
          while (it.hasNext) {
            val key = it.next()
            strR1 = strR1 + "_" + key
            strR2 = strR2 + "_" + isRecognizeWarn.get(key.toString)
          }
          while (itAdd.hasNext) {
            val key = itAdd.next()
            strA1 = strA1 + "_" + key
            strA2 = strA2 + "_" + isAddWarn.get(key.toString)
          }
          (dynamicID, deviceID, platID, strR1.substring(1), strR2.substring(1), strA1.substring(1), strA2.substring(1), dynamicFeatureStr, "1")
        } else {
          while (it.hasNext) {
            val key = it.next()
            strR1 = strR1 + "_" + key
            strR2 = strR2 + "_" + isRecognizeWarn.get(key.toString)
          }
          (dynamicID, deviceID, platID, strR1.substring(1), strR2.substring(1), null, null, dynamicFeatureStr, "1")
        }
      }
      else {
        (dynamicID, deviceID, platID, null, null, null, null, dynamicFeatureStr, "0")
      }
    }).filter(filter => filter._9.equals("1")).map(pair => (pair._1, pair._2, pair._3, pair._4, pair._5, pair._6, pair._7, pair._8))

    /**
      * kafka获取的流式数据由一系列的RDD组成
      * (dynamicID,deviceID,platID,REC_ObjList,REC_SimList,ADD_ObjList,ADD_DayList,dynamicFeatureStr)
      */

    filterResult.foreachRDD(rdd => {
      //初始化sqlContext对象及隐式转换
      val sqlContext = new SQLContext(ssc.sparkContext)
      import sqlContext.implicits._
      val splitRDD = staticStoreRDD.map(staticPair => (staticPair.split("ZHONGXIAN")(0), staticPair.split("ZHONGXIAN")(1), staticPair.split("ZHONGXIAN")(2)))
      //将静态数据库映射为staticTable表
      val staticTable = splitRDD.toDF("staticID", "staticObjectType", "staticFeatureStr").registerTempTable("staticTable")
      //将动态数据映射为dynamicTable表
      val dynamicTable = rdd.toDF("dynamicID", "deviceID", "platID", "REC_ObjList", "REC_SimList", "ADD_ObjList", "ADD_DayList", "dynamicFeatureStr").registerTempTable("dynamicTable")
      //通过笛卡尔积将两张表关联
      val joinTable = sqlContext.sql("select * from dynamicTable cross join staticTable").registerTempTable("joinTable")
      //spark sql自定义函数
      sqlContext.udf.register("comp", (a: String, b: String) => FaceFunction.featureCompare(a, b))
      val similarityResult = sqlContext.sql("select dynamicID,deviceID,platID,REC_ObjList,REC_SimList,ADD_ObjList,ADD_DayList,staticID,staticObjectType,comp(dynamicFeatureStr,staticFeatureStr) as similarity from joinTable")
      val simResu2rdd = similarityResult.rdd.map(row => {
        (row.getAs[String]("dynamicID"),
          row.getAs[String]("deviceID"),
          row.getAs[String]("platID"),
          row.getAs[String]("REC_ObjList"),
          row.getAs[String]("REC_SimList"),
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
      val simFilterResult = simResu2rdd.filter(simFilter => FilterUtils.similarityFilterFun(simFilter._5.split("_")(0), simFilter._10))
      /**
        * 过滤出符合比对范围数据
        */

      val rangeFilterResult = simFilterResult.
        filter(rangeFilter => FilterUtils.rangeFilterFun(rangeFilter._4.split("_"), rangeFilter._9))
      /**
        * 时间更新模块:
        * 对结果数据过滤出符合范围的更新数据并进行时间更新
        * 进行识别时间更新，为离线告警的时间的依据 ObjectInfoHandlerImpl  updateObjectInfo
        */
      val timeUpDateRangeFilterResult = rangeFilterResult.
        filter(timeUpDateRangeFilter => FilterUtils.rangeFilterFun(timeUpDateRangeFilter._6.split("_"), timeUpDateRangeFilter._9))
      val timeUpdateResult = timeUpDateRangeFilterResult.map(timeUpdate => (timeUpdate._8))

      timeUpdateResult.foreach(timeUpdateElem => {
        objectInfoInnerHandlerImpl.updateObjectInfoTime(timeUpdateElem)
      })

      /**
        * 对比对结果通过动态照片的唯一标识进行分组
        */
      val groupResult = rangeFilterResult.map(pair => (pair._1, (pair._2, pair._3, pair._4, pair._5, pair._6, pair._7, pair._8, pair._9, pair._10))).groupByKey()

      /**
        * 规则库定义告警类型：
        * 识别告警：0
        * 新增告警：1
        * 离线告警：2
        * 往rocketMQ发送告警数据时，不同的告警类型用如下tag:
        * 识别告警: "alarm_100"
        * 新增告警: "alarm_101"
        * 离线告警: "alarm_102"
        */
      //groupResult.collect().foreach(println)
      groupResult.collect().foreach(persisPair => {
        val ram = new RecognizeAlarmMessage()
        val gson = new Gson()
        ram.setDynamicID(persisPair._1)
        ram.setAlarmType(DeviceTable.IDENTIFY.toString)
        ram.setDynamicDeviceID(FtpUtil.getRowKeyMessage(persisPair._1).get("ipcID"))
        val items = ArrayBuffer[Item]()
        persisPair._2.foreach(gbit => {
          val it = new Item()
          it.setSimilarity(gbit._9.toString)
          it.setStaticID(gbit._7)
          items += it
        })
        ram.setItems(items.toArray)
        val str = gson.toJson(ram)
        println(str)

      })
    })
    ssc.start()
    ssc.awaitTermination()

  }

}
