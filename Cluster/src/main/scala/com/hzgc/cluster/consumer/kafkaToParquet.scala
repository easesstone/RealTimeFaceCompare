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

  case class pic(ftpurl: String, eyeglasses: String, feature: String, gender: String, haircolor: String,
                 hairstyle: String, hat: String, huzi: String, tie: String, ipcid: String, timeSlot: String,
                 timestamp: String, pictype: String, date: String) {}

  val properties: Properties = StreamingUtils.getProperties
  val appname: String = properties.getProperty("job.faceObjectConsumer.appName")
  val master: String = properties.getProperty("job.faceObjectConsumer.master")
  val timeInterval: Duration = Durations.seconds(properties.getProperty("job.faceObjectConsumer.timeInterval").toLong)
  val brokers: String = properties.getProperty("job.faceObjectConsumer.broker.list")
  val kafkaGroupId: String = properties.getProperty("job.faceObjectConsumer.group.id")
  val topics = Set(properties.getProperty("job.faceObjectConsumer.topic.name"))
  val numDstreams: Int = properties.getProperty("job.faceObjectConsumer.DStreamNums").toInt
  val repartitionNum: Int = properties.getProperty("job.repartition.number").toInt
  val storeAddress: String = properties.getProperty("job.storeAddress")

  def main(args: Array[String]): Unit = {
    val list = List(appname, master, timeInterval, brokers, kafkaGroupId, topics, numDstreams, repartitionNum, storeAddress)
    list.foreach(p => {
      if (null == p || p.equals("")) {
        println("Please make sure the parameter correct!!!")
      }
    })
    val session = SparkSession.builder().appName(appname).master(master).getOrCreate()
    val str = new StreamingContext(session.sparkContext, timeInterval)
    val kafkaParams = Map(
      "metadata.broker.list" -> brokers,
      "group.id" -> kafkaGroupId
    )
    val putDataToEs = new PutDataToEs
    val kafkaDstream = (1 to numDstreams).map(_ =>
      KafkaUtils.createDirectStream[String, FaceObject, StringDecoder, FaceObjectDecoder](str, kafkaParams, topics))
    val unionDstream = str.union(kafkaDstream).repartition(repartitionNum)
    unionDstream.map(p => {
      var status = putDataToEs.putDataToEs(p._1, p._2)
      if (status == 1) {
        println("Put data to es success!")
      } else {
        println("Put data to es failed!")
      }
    })
    val kafkaDF = unionDstream.map(p => pic(p._1, p._2.getAttribute.getEyeglasses.name(),
      FaceFunction.floatArray2string(p._2.getAttribute.getFeature),
      p._2.getAttribute.getGender.name(), p._2.getAttribute.getHairColor.name(),
      p._2.getAttribute.getHairStyle.name(), p._2.getAttribute.getHat.name(),
      p._2.getAttribute.getHuzi.name(), p._2.getAttribute.getTie.name(),
      p._2.getIpcId, p._2.getTimeSlot, p._2.getTimeStamp, p._2.getType.name(), p._2.getDate))
    kafkaDF.foreachRDD(rdd => {
      import session.implicits._
      rdd.coalesce(3, shuffle = true).toDF().write.mode(SaveMode.Append).parquet(storeAddress)
    })
    str.start()
    str.awaitTermination()
  }
}
