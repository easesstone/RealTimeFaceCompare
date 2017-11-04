package com.hzgc.cluster.consumer

import java.util.Properties

import com.hzgc.cluster.util.StreamingUtils
import com.hzgc.ftpserver.producer.{FaceObject, FaceObjectDecoder}
import com.hzgc.jni.FaceFunction
import kafka.serializer.StringDecoder
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Duration, Durations, StreamingContext}

object kafkaToParquet {

  def getItem(parameter: String, properties: Properties): String = {
    val item = properties.getProperty(parameter)
    if (null != item) {
      return item
    } else {
      println("Please check the parameter " + parameter + " is correct!!!")
      System.exit(0)
    }
    null
  }

  case class Picture(ftpurl: String, //图片搜索地址
                     feature: String, ipcid: String, timeslot: String, //feature：图片特征值 ipcid：设备id  timeslot：时间段
                     timestamp: String, pictype: String, date: String, //timestamp:时间戳 pictype：图片类型 date：时间
                     eyeglasses: String, gender: String, haircolor: String, //人脸属性：眼镜、性别、头发颜色
                     hairstyle: String, hat: String, huzi: String, tie: String //人脸属性：发型、帽子、胡子、领带
                    )

  val properties: Properties = StreamingUtils.getProperties
  val appname: String = getItem("job.faceObjectConsumer.appName", properties)
  val master: String = getItem("job.faceObjectConsumer.master", properties)
  val timeInterval: Duration = Durations.seconds(getItem("job.faceObjectConsumer.timeInterval", properties).toLong)
  val brokers: String = getItem("job.faceObjectConsumer.broker.list", properties)
  val kafkaGroupId: String = getItem("job.faceObjectConsumer.group.id", properties)
  val topics = Set(getItem("job.faceObjectConsumer.topic.name", properties))
  val numDstreams: Int = getItem("job.faceObjectConsumer.DStreamNums", properties).toInt
  val repartitionNum: Int = getItem("job.repartition.number", properties).toInt
  val storeAddress: String = getItem("job.storeAddress", properties)

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName(appname).master(master).getOrCreate()
    val ssc = new StreamingContext(spark.sparkContext, timeInterval)
    val kafkaParams = Map(
      "metadata.broker.list" -> brokers,
      "group.id" -> kafkaGroupId
    )
    val putDataToEs = PutDataToEs.getInstance()
    val kafkaDstream = (1 to numDstreams).map(_ =>
      KafkaUtils.createDirectStream[String, FaceObject, StringDecoder, FaceObjectDecoder](ssc, kafkaParams, topics))
    val unionDstream = ssc.union(kafkaDstream).repartition(repartitionNum)
    val kafkaDF = unionDstream.map(p => {
      var status = putDataToEs.putDataToEs(p._1, p._2)
      if (status != 1) {
        println("Put data to es failed!")
      }
      Picture(p._1, FaceFunction.floatArray2string(p._2.getAttribute.getFeature), p._2.getIpcId,
        p._2.getTimeSlot, p._2.getTimeStamp, p._2.getType.name(), p._2.getDate, p._2.getAttribute.getEyeglasses.name(),
        p._2.getAttribute.getGender.name(), p._2.getAttribute.getHairColor.name(), p._2.getAttribute.getHairStyle.name(),
        p._2.getAttribute.getHat.name(), p._2.getAttribute.getHuzi.name(), p._2.getAttribute.getTie.name()
      )
    })
    kafkaDF.foreachRDD(rdd => {
      import spark.implicits._
      rdd.coalesce(1, shuffle = true).toDF().write.mode(SaveMode.Append).parquet(storeAddress)
    })
    ssc.start()
    ssc.awaitTermination()
  }
}
