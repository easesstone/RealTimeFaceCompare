package com.hzgc.cluster.clustering

import java.sql.Timestamp
import java.util
import java.util.{Calendar, Properties}

import com.hzgc.cluster.clutering.ClusteringRaw
import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.dubbo.clustering.ClusteringAttribute
import org.apache.log4j.Logger
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Encoders, SparkSession}
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable

object Clustering4 {

  case class Data(id: Long, time: Timestamp, ipc: String, host: String, spic: String, bpic: String)

  case class Clustering(firstUrl: String, dataSet: mutable.Set[String])

  val LOG: Logger = Logger.getLogger(KMeansClustering.getClass)
  val threshold: Double = 0.9
  val timeCount: Int = 15
  val repetitionRate: Double = 0.4


  def main(args: Array[String]) {

    val driverClass = "com.mysql.jdbc.Driver"
    val sqlProper = new Properties()
    val properties = PropertiesUtils.getProperties
    val clusterNum = properties.getProperty("job.clustering.cluster.number")
    val iteraterNum = properties.getProperty("job.clustering.iterater.number")
    val appName = properties.getProperty("job.clustering.appName")
    val url = properties.getProperty("job.clustering.mysql.url")
    val tableName = properties.getProperty("job.clustering.mysql.table")
    val timeField = properties.getProperty("job.clustering.mysql.field.time")
    val ipcField = properties.getProperty("job.clustering.mysql.field.ipc")
    val dataField = properties.getProperty("job.clustering.mysql.field.data")
    val idField = properties.getProperty("job.clustering.mysql.field.id")
    val hostField = properties.getProperty("job.clustering.mysql.field.host")
    val spicField = properties.getProperty("job.clustering.mysql.field.spic")
    val bpicField = properties.getProperty("job.clustering.mysql.field.bpic")
    val partitionNum = properties.getProperty("job.clustering.partiton.number").toInt

    val spark = SparkSession.builder().appName(appName).enableHiveSupport().master("local[*]").getOrCreate()
    import spark.implicits._

    val calendar = Calendar.getInstance()
    val currentYearMon = "'" + calendar.get(Calendar.YEAR) + "-%" + (calendar.get(Calendar.MONTH)) + "%'"

    spark.sql("select ftpurl,feature from person_table where date like " + currentYearMon).createOrReplaceTempView("parquetTable")

    val preSql = "(select T1.id, T2.host_name, T2.big_picture_url, T2.small_picture_url, T1.alarm_time " + "from t_alarm_record as T1 inner join t_alarm_record_extra as T2 on T1.id=T2.record_id " + "where T2.static_id IS NULL " + "and DATE_FORMAT(T1.alarm_time,'%Y-%m') like " + currentYearMon + ") as temp"

    sqlProper.setProperty("driver", driverClass)
    val dataSource = spark.read.jdbc(url, preSql, sqlProper)
    dataSource.map(data => {
      println("ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](spicField))
      Data(data.getAs[Long](idField), data.getAs[Timestamp](timeField), data.getAs[String](spicField).substring(1, data.getAs[String](spicField).indexOf("/", 1)), data.getAs[String](hostField), "ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](spicField), "ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](bpicField))
    }).createOrReplaceTempView("mysqlTable")

    val joinData = spark.sql("select T1.feature, T2.* from parquetTable as T1 inner join mysqlTable as T2 on T1.ftpurl=T2.spic")
    //get the url and feature
    val idPointDF = joinData.map(data => (data.getAs[String]("spic"), data.getAs[mutable.WrappedArray[Float]]("feature").toArray
      .map(_.toDouble))).toDF()
    //zipwithIndex for decrease the computer cost
    val idPointDF1=idPointDF
    val joinPointDs = idPointDF crossJoin (idPointDF1)
    joinPointDs.select("*").show(false)

    /*val joined = zipIdPointDs.cartesian(zipIdPointDs)
    val dataPairs = joined.filter(f => f._1._2 < f._2._2)

    //calculate the cosine similarity of each two data
    val pairSimilarity = dataPairs.map(data => (data._1._2, data._2._2, cosineMeasure(data._1._1._2, data._2._1._2)))

    //filter by the similarity
    val filterSimilarity = pairSimilarity.filter(_._3 > threshold).map(data => (data._1.toString, data._2.toString))

    //count each clutering data number,the first image crashed
    val furlGroup = filterSimilarity.reduceByKey((a, b) => (a + "," + b))
    val numPerUrl = furlGroup.map(data => {
      val key = data._1
      val valList = data._2.split(",").toList
      (key, valList, valList.size)
    })
    val numFilter = numPerUrl.filter(_._3 > timeCount)

    //merge two list
    val joinNumFliter = numFilter.zipWithIndex().cartesian(numFilter.zipWithIndex()).filter(f => f._1._2 < f._2._2).persist(StorageLevel.MEMORY_AND_DISK_SER)
    val unionData = joinNumFliter.map(data => (data._1._1._1, data._2._1._1, data._1._1._2, data._2._1._2, dataSetSimilarity(data._1._1._2, data._2._1._2))).filter(data => data._5 > repetitionRate)
    val lastData = unionData.map(data => {
      val key = data._1
      val unionList = data._3.union(data._4).distinct
      (key, unionList, unionList.size)
    })
    lastData.toDF().printSchema()*/

    spark.stop()
  }

  def cosineMeasure(v1: Array[Double], v2: Array[Double]): Double = {

    val member = v1.zip(v2).map(d => d._1 * d._2).reduce(_ + _).toDouble
    //求出分母第一个变量值
    val temp1 = math.sqrt(v1.map(num => {
      math.pow(num, 2)
    }).reduce(_ + _))
    //求出分母第二个变量值
    val temp2 = math.sqrt(v2.map(num => {
      math.pow(num, 2)
    }).reduce(_ + _))
    //求出分母
    val denominator = temp1 * temp2
    //进行计算
    member / denominator
  }

  def dataSetSimilarity(list1: List[String], list2: List[String]): Double = {
    val union = List.concat(list1, list2).distinct.size
    val intersect = list1.intersect(list2).size
    val minSize = if (list1.size < list2.size) list1.size else list2.size
    intersect / minSize
  }

}
