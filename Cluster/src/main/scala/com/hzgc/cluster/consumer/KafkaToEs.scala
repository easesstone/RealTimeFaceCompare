package com.hzgc.cluster.consumer

import java.util.Properties

import com.google.common.base.Stopwatch
import com.hzgc.cluster.util.StreamingUtils
import com.hzgc.ftpserver.producer.{FaceObject, FaceObjectDecoder}
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.ZkClient
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.{Duration, Durations, StreamingContext}

object KafkaToEs {
  val LOG: Logger = Logger.getLogger(KafkaToEs.getClass)
  val properties: Properties = StreamingUtils.getProperties

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
    val appname: String = getItem("job.faceObjectConsumer.appName", properties)
    val brokers: String = getItem("job.faceObjectConsumer.broker.list", properties)
    val kafkaGroupId: String = getItem("job.faceObjectConsumer.group.id", properties)
    val topics = Set(getItem("job.faceObjectConsumer.topic.name", properties))
    val spark = SparkSession.builder().appName(appname).getOrCreate()
    val kafkaParams = Map(
      "metadata.broker.list" -> brokers,
      "group.id" -> kafkaGroupId
    )
    val ssc = setupSsc(topics, kafkaParams, spark)
    ssc.start()
    ssc.awaitTermination()
  }

  private def setupSsc(topics: Set[String], kafkaParams: Map[String, String]
                       , spark: SparkSession)(): StreamingContext = {
    val timeInterval: Duration = Durations.seconds(getItem("job.faceObjectConsumer.timeInterval", properties).toLong)
    val zkHosts: String = getItem("job.zkDirAndPort", properties)
    val zKPaths: String = getItem("job.kafkaToEs.zkPaths", properties)
    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, timeInterval)
    val messages = createCustomDirectKafkaStream(ssc, kafkaParams, zkHosts, zKPaths, topics)
    val kafkaDF = messages.map(faceobject => {
      (faceobject._1, faceobject._2)
    })
    kafkaDF.foreachRDD(rdd => {
      rdd.foreachPartition(parData => {
        val putDataToEs = PutDataToEs.getInstance()
        parData.foreach(data => {
          val status = putDataToEs.putDataToEs(data._1, data._2)
          if (status != 1) {
            LOG.error("Put data to es failed! And the failed ftpurl is " + data._1)
          }
        })
      })
    })
    ssc
  }

  private def createCustomDirectKafkaStream(ssc: StreamingContext, kafkaParams: Map[String, String], zkHosts: String
                                            , zkPath: String, topics: Set[String]): InputDStream[(String, FaceObject)] = {
    val topic = topics.last
    val zKClient = new ZkClient(zkHosts)
    val storedOffsets = readOffsets(zKClient, zkHosts, zkPath, topic)
    val kafkaStream = storedOffsets match {
      case None =>
        KafkaUtils.createDirectStream[String, FaceObject, StringDecoder, FaceObjectDecoder](ssc, kafkaParams, topics)
      case Some(fromOffsets) =>
        val messageHandler = (mmd: MessageAndMetadata[String, FaceObject]) => (mmd.key(), mmd.message())
        KafkaUtils.createDirectStream[String, FaceObject, StringDecoder, FaceObjectDecoder
          , (String, FaceObject)](ssc, kafkaParams, fromOffsets, messageHandler)
    }
    kafkaStream.foreachRDD(rdd => saveOffsets(zKClient, zkHosts, zkPath, rdd))
    kafkaStream
  }

  private def readOffsets(zkClient: ZkClient, zkHosts: String, zkPath: String, topic: String): Option[Map[TopicAndPartition, Long]] = {
    LOG.info("Reading offsets from Zookeeper")
    val stopwatch = new Stopwatch()
    val (offsetsRangesStrOpt, _) = ZkUtils.readDataMaybeNull(zkClient, zkPath)
    offsetsRangesStrOpt match {
      case Some(offsetsRangesStr) =>
        LOG.info(s"Read offset ranges: $offsetsRangesStr")
        val offsets = offsetsRangesStr.split(",")
          .map(x => x.split(":"))
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
    LOG.info("chandan Writing offsets to Zookeeper zkClient=" + zkClient + "  zkHosts=" + zkHosts + "zkPath=" + zkPath + "  offsetsRangesStr:" + offsetsRangesStr)
    ZkUtils.updatePersistentPath(zkClient, zkPath, offsetsRangesStr)
    LOG.info("Done updating offsets in Zookeeper. Took " + stopwatch)
  }
}