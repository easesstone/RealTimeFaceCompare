package com.hzgc.cluster.consumer

import java.sql.Timestamp
import java.util.Properties

import com.google.common.base.Stopwatch
import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.collect.expand.processer.{FaceObject, FaceObjectDecoder}
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.ZkClient
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.streaming.{Duration, Durations, StreamingContext}
import org.apache.spark.streaming.dstream.InputDStream

/**
  * job.faceObjectConsumer.appName=FaceObjectConsumer
  * job.faceObjectConsumer.broker.list=172.18.18.100:9092,172.18.18.101:9092,172.18.18.102:9092
  * job.faceObjectConsumer.group.id=FaceObjectConsumerGroup
  * job.faceObjectConsumer.topic.name=feature
  * job.zkDirAndPort=172.18.18.100:2181,172.18.18.101:2181,172.18.18.102:2181
  * job.kafkaToParquet.zkPaths=/parquet
  * job.storeAddress=hdfs://hzgc/user/hive/warehouse/mid_table/
  * scala s 函数的作用
  * //s函数的应用
  * val name="Tom"
  * s"Hello,$name"//Hello,Tom
  * s"1+1=${1+1}"//1+1=2
  *
  * val a = Array(10, 20, 30, 40)
  * val b = a.lastOption
  * println(b)        //Some(40)
  * b match {
  *     case Some(value) => print(value)    // 40
  *     case None => println(None)
  * }
  *
  */
object KafkaToParquet {
  val LOG: Logger = Logger.getLogger(KafkaToParquet.getClass)
  val properties: Properties = PropertiesUtils.getProperties

  case class Picture(ftpurl: String, //图片搜索地址
                     //feature：图片特征值 ipcid：设备id  timeslot：时间段
                     feature: Array[Float], ipcid: String, timeslot: Int,
                     //timestamp:时间戳 pictype：图片类型 date：时间
                     exacttime: Timestamp, searchtype: String, date: String,
                     //人脸属性：眼镜、性别、头发颜色
                     eyeglasses: Int, gender: Int, haircolor: Int,
                     //人脸属性：发型、帽子、胡子、领带
                     hairstyle: Int, hat: Int, huzi: Int, tie: Int,
                    //清晰度评价
                     sharpness: Int
                    )

  def getItem(parameter: String, properties: Properties): String = {
    val item = properties.getProperty(parameter)
    if (null != item) {
      return item
    } else {
      println("Please check the parameter " + parameter + " is correct!!!")
      System.exit(1)
    }
    null
  }

  def main(args: Array[String]): Unit = {
    val appname: String = getItem("job.faceObjectConsumer.appName", properties)  // app 名字
    val brokers: String = getItem("job.faceObjectConsumer.broker.list", properties) // kafka broker lists
    val kafkaGroupId: String = getItem("job.faceObjectConsumer.group.id", properties)  // kafka group
    val topics = Set(getItem("job.faceObjectConsumer.topic.name", properties))   // 需要读取的topic
    val spark = SparkSession.builder().appName(appname).getOrCreate()    // 构造sparkSession 对象
    val kafkaParams = Map(                                         // 封装KafkaParams
      "metadata.broker.list" -> brokers,
      "group.id" -> kafkaGroupId
    )
    val ssc = setupSsc(topics, kafkaParams, spark)   // 构造StreamingContext
    ssc.start()                                  // 启动StreamingContext
    ssc.awaitTermination()
  }

  private def setupSsc(topics: Set[String], kafkaParams: Map[String, String]
                       , spark: SparkSession)(): StreamingContext = {
    val timeInterval: Duration = Durations.seconds(getItem("job.faceObjectConsumer.timeInterval",
      properties).toLong)                             // 时间间隔
    val storeAddress: String = getItem("job.storeAddress", properties)  // 最终从kafaka 中读取的数据，存储的地址。
    val zkHosts: String = getItem("job.zkDirAndPort", properties)                  //ZK 地址。
    val zKPaths: String = getItem("job.kafkaToParquet.zkPaths", properties)      // offset在ZK 存储的路径。
    val zKClient = new ZkClient(zkHosts)                          // 初始化ZK Client.
    val sc = spark.sparkContext                                // sparkContext
    val ssc = new StreamingContext(sc, timeInterval)              // SparkStreming
    val messages = createCustomDirectKafkaStream(ssc, kafkaParams, zkHosts, zKPaths, topics) // ?????????????
    val kafkaDF = messages.map(faceobject => {
      (Picture(faceobject._1, faceobject._2.getAttribute.getFeature, faceobject._2.getIpcId,
        faceobject._2.getTimeSlot.toInt, Timestamp.valueOf(faceobject._2.getTimeStamp),
        faceobject._2.getType.name(), faceobject._2.getDate,
        faceobject._2.getAttribute.getEyeglasses, faceobject._2.getAttribute.getGender,
        faceobject._2.getAttribute.getHairColor, faceobject._2.getAttribute.getHairStyle,
        faceobject._2.getAttribute.getHat, faceobject._2.getAttribute.getHuzi,
        faceobject._2.getAttribute.getTie, faceobject._2.getAttribute.getSharpness), faceobject._1, faceobject._2)
    })
    kafkaDF.foreachRDD(rdd => {
      import spark.implicits._
      rdd.map(rdd => rdd._1).repartition(1).toDF().write.mode(SaveMode.Append)
        .parquet(storeAddress)
      rdd.foreachPartition(parData => {
        val putDataToEs = PutDataToEs.getInstance()
        parData.foreach(data => {
          val status = putDataToEs.putDataToEs(data._2, data._3)
          if (status != 1) {
            println("Put data to es failed! And the failed ftpurl is " + data._2)
          }
        })
      })
    })
    LOG.info("Put something to the backup...")
    messages.foreachRDD(rdd => saveOffsets(zKClient, zkHosts, zKPaths, rdd))
    ssc
  }

  private def createCustomDirectKafkaStream(ssc: StreamingContext, kafkaParams: Map[String, String], zkHosts: String
                                            , zkPath: String, topics: Set[String]): InputDStream[(String, FaceObject)] = {
    val topic = topics.last             // 取得Topic 的名字。
    val zKClient = new ZkClient(zkHosts)   // 初始化zkClient 。
    val storedOffsets = readOffsets(zKClient, zkHosts, zkPath, topic) // 从zkClient 中获取offset.???????????????????
    LOG.info("storeOffsets" + storedOffsets)
    val kafkaStream = storedOffsets match {
      case None =>
        KafkaUtils.createDirectStream[String, FaceObject, StringDecoder, FaceObjectDecoder](ssc, kafkaParams, topics)
      case Some(fromOffsets) =>
        val messageHandler = (mmd: MessageAndMetadata[String, FaceObject]) => (mmd.key(), mmd.message())
        KafkaUtils.createDirectStream[String, FaceObject, StringDecoder, FaceObjectDecoder
          , (String, FaceObject)](ssc, kafkaParams, fromOffsets, messageHandler)
    }
    kafkaStream
  }

  private def readOffsets(zkClient: ZkClient, zkHosts: String,
                          zkPath: String, topic: String): Option[Map[TopicAndPartition, Long]] = {
    LOG.info("Reading offsets from Zookeeper")
    val stopwatch = new Stopwatch()
    val (offsetsRangesStrOpt, stat) = ZkUtils.readDataMaybeNull(zkClient, zkPath)
    offsetsRangesStrOpt match {
      case Some(offsetsRangesStr) =>
        LOG.info(s"Read offset ranges: $offsetsRangesStr")
        LOG.info(s"Stat: $stat" )
        val offsets = offsetsRangesStr.split(",")
          .map(x => x.split(":") )
          .map {
            case Array(partitionStr, offsetStr) => TopicAndPartition(topic, partitionStr.toInt) -> offsetStr.toLong
          }.toMap
        LOG.info("Done reading offsets from Zookeeper. Took " + stopwatch)
        Some(offsets)
      case None =>
        LOG.info("No offsets found in Zookeeper. Took " + stopwatch)
        None
    }
  }

  private def saveOffsets(zkClient: ZkClient, zkHosts: String, zkPath: String, rdd: RDD[_]): Unit = {
    LOG.info("Saving offsets to Zookeeper")
    val stopwatch = new Stopwatch()
    val offsetsRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
    offsetsRanges.foreach(offsetRange => LOG.debug(s"Using $offsetRange"))
    val offsetsRangesStr = offsetsRanges.map(offsetRange => s"${offsetRange.partition}:${offsetRange.fromOffset}")
      .mkString(",")
    LOG.info("chandan Writing offsets to Zookeeper zkClient=" + zkClient + "  " +
        "zkHosts=" + zkHosts + "zkPath=" + zkPath + "  offsetsRangesStr:" + offsetsRangesStr)
    ZkUtils.updatePersistentPath(zkClient, zkPath, offsetsRangesStr)
    LOG.info("Done updating offsets in Zookeeper. Took " + stopwatch)
  }
}
