package com.hzgc.cluster.consumer

import java.sql.Timestamp
import java.util.Properties

import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.dubbo.feature.FaceAttribute
import kafka.utils.{ZKGroupTopicDirs, ZkUtils}
import org.apache.kafka.common.TopicPartition
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils}

import scala.xml.dtd.PEReference

object KafkaToParquetV1 {
    val LOG : Logger = Logger.getLogger(KafkaToParquetV1.getClass)
    val properties : Properties = PropertiesUtils.getProperties
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
                       sharpness: Int)
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
        val zkUrl = "172.18.18.100:2181"
        val sessionTimeOut = 60000  //会话超时时间，单位为毫秒。默认是30 000ms
        val connetionTimeOut = 120000 //连接创建超时时间，单位为毫秒。此参数表明如果在这个时间段内还是无法和ZooKeeper建立链接，那么就放弃连接，直接抛出异常。
        val zkClientAndConnection = ZkUtils.createZkClientAndConnection(zkUrl, sessionTimeOut, connetionTimeOut) //
        val zkUtils = new ZkUtils(zkClientAndConnection._1, zkClientAndConnection._2, false)
        def readOffSets(topics : Seq[String], groupId : String) : Map[TopicPartition, Long] = {
            val topicPartOffsetMap = collection.mutable.HashMap.empty[TopicPartition, Long]
            val partitonMap = zkUtils.getPartitionsForTopics(topics)
            partitonMap.foreach(topicPartitions => {
                val zkGroupTopicDirs = new ZKGroupTopicDirs(groupId, topicPartitions._1)
                topicPartitions._2.foreach(partition => {
                    val offsetPath = zkGroupTopicDirs.consumerOffsetDir + "/" + partition
                    try {
                        val offsetStatTuple = zkUtils.readData(offsetPath)
                        if (offsetStatTuple != null) {
                            LOG.info("retrieving offset details - topic: {}, partition: {}, offset: {}, node path: {}",
                                Seq[AnyRef](topicPartitions._1, partition.toString, offsetStatTuple._1, offsetPath): _*)
                            topicPartOffsetMap.put(new TopicPartition(topicPartitions._1, Integer.valueOf(partition)),offsetStatTuple._1.toLong)
                        }
                    } catch {
                        case e : Exception => {
                            LOG.warn("retrieving offset details - no previous node exists:" + " {}, topic: {}, partition: {}, node path: {}",
                                Seq[AnyRef](e.getMessage, topicPartitions._1, partition.toString, offsetPath): _*)
                            topicPartOffsetMap.put(new TopicPartition(topicPartitions._1, Integer.valueOf(partition)), 0L)
                        }
                    }
                })
            })
            topicPartOffsetMap.toMap
        }
        val topicsSeq : Seq[String] = Seq[String]("feature")
        val groupId = "FaceObjectConsumerGroup"
        val fromOffsets = readOffSets(topicsSeq, groupId)
        val spark = SparkSession.builder().appName("demo Streming").getOrCreate()
        val sc = spark.sparkContext
        val ssc = new StreamingContext(sc, 15)
        val inputDStream = KafkaUtils.createDirectStream(ssc, PEReference, ConsumerStrategies.Subscribe[String,FaceAttribute](topics, kafkaParams, fromOffsets))
    }






//    def saveOffsets(topicName : String, groupId : String, offsetRanges : Array[OffsetRange],
//                    hbaseTableName : String, batchTime : org.apache.spark.streaming.Time) = {
//        val hbaseConf = HBaseConfiguration.create()
//        hbaseConf.addResource("hbase-site.xml")
//        val conn = ConnectionFactory.createConnection(hbaseConf)
//        val table = conn.getTable(TableName.valueOf(hbaseTableName))
//        val rowkey = topicName + ":" + groupId + ":" + String.valueOf(batchTime.milliseconds)
//        val put = new Put(rowkey.getBytes());
//        for (offset <- offsetRanges) {
//            put.addColumn(Bytes.toBytes("offsets"), Bytes.toBytes(offset.partition.toString),
//                Bytes.toBytes(offset.untilOffset.toString))
//        }
//        table.put(put)
//        conn.close()
//    }
//
//    def getLastCommitedOffsets(topicName : String, groupId : String, hbaseTableName : String,
//                               zkQuorum : String, zkRootDir : String, sessionTimeout : Int, connectionTimeOut : Int):Map[TopicPartition, Long] = {
//        val hbaseConf = HBaseConfiguration.create()
//        val zkUrl = zkQuorum+"/"+zkRootDir
//        val zkClientAndConnection = ZkUtils.createZkClientAndConnection(zkUrl,
//            sessionTimeout,connectionTimeOut)
//        val zkUtils = new ZkUtils(zkClientAndConnection._1, zkClientAndConnection._2,false)
//        val zKNumberOfPartitionsForTopic = zkUtils.getPartitionsForTopics(Seq(TOPIC_NAME
//        )).get(topicName).toList.head.size
//        zkClientAndConnection._1.close()
//        zkClientAndConnection._2.close()
//
//        //Connect to HBase to retrieve last committed offsets
//        val conn = ConnectionFactory.createConnection(hbaseConf)
//        val table = conn.getTable(TableName.valueOf(hbaseTableName))
//        val startRow = topic + ":" + GROUP_ID + ":" +
//            String.valueOf(System.currentTimeMillis())
//        val stopRow = TOPIC_NAME + ":" + GROUP_ID + ":" + 0
//        val scan = new Scan()
//        val scanner = table.getScanner(scan.setStartRow(startRow.getBytes).setStopRow(
//            stopRow.getBytes).setReversed(true))
//        val result = scanner.next()
//        var hbaseNumberOfPartitionsForTopic = 0 //Set the number of partitions discovered for a topic in HBase to 0
//        if (result != null){
//            //If the result from hbase scanner is not null, set number of partitions from hbase
//            to the  number of cells
//            hbaseNumberOfPartitionsForTopic = result.listCells().size()
//        }
//    }

}
