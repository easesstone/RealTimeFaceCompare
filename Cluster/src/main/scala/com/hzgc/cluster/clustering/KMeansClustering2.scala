package com.hzgc.cluster.clustering

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date, Properties}

import com.hzgc.cluster.clustering.canopy2.{Point, distance}
import com.hzgc.cluster.consumer.PutDataToEs
import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.dubbo.clustering.ClusteringAttribute
import org.apache.log4j.Logger
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.SparkSession
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD

import scala.collection.mutable
import scala.util.Random

object KMeansClustering2 {

  case class Data(id: Long, time: Timestamp, ipc: String, host: String, spic: String, bpic: String)

  val LOG: Logger = Logger.getLogger(KMeansClustering.getClass)
  var numClusters = 26
  val numIterations = 100000
  var clusterIndex: Int = 0
  val T2 = 0.1
  val T1 = 0.3


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

    val spark = SparkSession.builder()
      .appName(appName)
      .enableHiveSupport()
      .master("local[2]")
      .getOrCreate()

    import spark.implicits._

    val calendar = Calendar.getInstance()
    //val currentYearMon = "'" + calendar.get(Calendar.YEAR) + "-%" + (calendar.get(Calendar.MONTH) + 1) + "%'"
    val currentYearMon = "'" + calendar.get(Calendar.YEAR) + "-%" + (calendar.get(Calendar.MONTH)) + "%'"
    // TODO: if parquetTable have no data?
    spark.sql("select ftpurl,feature from person_table where date like " + currentYearMon).createOrReplaceTempView("parquetTable")

    val preSql = "(select T1.id, T2.host_name, T2.big_picture_url, T2.small_picture_url, T1.alarm_time " + "from t_alarm_record as T1 inner join t_alarm_record_extra as T2 on T1.id=T2.record_id " + "where T2.static_id IS NULL " + "and DATE_FORMAT(T1.alarm_time,'%Y-%m') like " + currentYearMon + ") as temp"

    sqlProper.setProperty("driver", driverClass)
    val dataSource = spark.read.jdbc(url, preSql, sqlProper)
    dataSource.map(data => {
      //println("ftp://" + data.getAs[String](hostField) + ":2121/" + data.getAs[String](spicField))
      Data(data.getAs[Long](idField), data.getAs[Timestamp](timeField), data.getAs[String](spicField).substring(1, data.getAs[String](spicField).indexOf("/", 1)), data.getAs[String](hostField), "ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](spicField), "ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](bpicField))
    }).createOrReplaceTempView("mysqlTable")

    val joinData = spark.sql("select T1.feature, T2.* from parquetTable as T1 inner join mysqlTable as T2 on T1.ftpurl=T2.spic")

    val idPointRDD = joinData.rdd.map(
      data => (data.getAs[String]("spic"),
        data.getAs[mutable.WrappedArray[Float]]("feature")
          .toArray
          .map(_.toDouble)))

    /* val idPointRDD = joinData.rdd.map(
      data => (data.getAs[String]("spic"),
        Vectors.dense(
          data.getAs[mutable.WrappedArray[Float]]("feature")
            .toArray
            .map(_.toDouble))))
      .cache()*/

    /*var idPointRDD2 = idPointRDD
    val canopies = mutable.Map[String, mutable.Set[Array[Double]]]()
    val r = Random.shuffle(idPointRDD)
    val C = r.top(1).map(x => (x._1, x._2))
    canopies.foreach(x =>
      canopies(x._1).remove(C))

    var idPointRDD2 = idPointRDD.filter(x => x != C)

    val canopy = mutable.Set[](C)
    canopies(C) = canopy
    for (p <- r.tail) {
      val dist = cosineMeasure(C, p)
      if (dist <= T1) {
        canopy.add(p)
      }
      if (dist < T2) {
        points = points.filter(x => x != p)
      }
    }

    canopies foreach { x =>
      println("Cluster: %s => %s".format(x._1, x._2))
    }*/
    /*  idPointRDD.zip(idPointRDD).foreach(a => {
        val b = cosineMeasure(a._1._2.toArray, a._2._2.toArray)
      })
      println("total:" + sum)
      val count = idPointRDD.count().toInt
      println(count)
      val avgDistance = sum / count
      println("avg:" + avgDistance)*/


    val pairs = idPointRDD.map(data => data._2).cache().collect().toList
    val centers=new util.ArrayList[mutable.WrappedArray[Double]]()
    for (i <- pairs.size) {

    }
    val map_centers = new mutable.HashSet[(String, Array[Double])]
    val raw_center_pairs = pairs.map(v => (v._1, canopy_(v, map_centers, T2))).filter(a => a._2 != null).collect().toList

    val center_pairs = new mutable.HashSet[(String, Array[Double])]

    for (i <- raw_center_pairs.indices) {
      canopy_(raw_center_pairs(i)._2, center_pairs, T2)
    }
    numClusters = center_pairs.toList.size
    println("*************")
    println(numClusters)

    println("total data number:" + idPointRDD.count())


    /*val kMeansModel = KMeans.train(idPointRDD.map(_._2).sample(withReplacement = false, 0.4), numClusters, numIterations)
    val trainMidResult = kMeansModel.predict(idPointRDD.map(_._2))
    val trainResult = trainMidResult.zip(joinData.select("id", "time", "ipc", "host", "spic", "bpic").rdd)
      .groupByKey()
      .sortByKey()
      .map(data => (data._1, data._2.toArray.sortWith((a, b) => a.getTimestamp(1).getTime > b.getTimestamp(1).getTime)))

    val table1List = new util.ArrayList[ClusteringAttribute]()
    trainResult.map(data => {
      val attribute = new ClusteringAttribute()
      attribute.setClusteringId(data._1.toString)
      attribute.setCount(data._2.length)
      attribute.setFirstAppearTime(data._2(0).getTimestamp(1).toString)
      attribute.setFirstIpcId(data._2(0).getAs[String]("ipc"))
      attribute.setLastAppearTime(data._2(data._2.length - 1).getTimestamp(1).toString)
      attribute.setLastIpcId(data._2(data._2.length - 1).getAs[String]("ipc"))
      attribute.setFtpUrl(data._2(data._2.length / 2).getAs[String]("spic"))
      attribute
    }).collect().foreach(data => table1List.add(data))

    val mon = calendar.get(Calendar.MONTH)
    var monStr = ""
    if (mon < 10) {
      monStr = "0" + mon
    } else {
      monStr = String.valueOf(mon)
    }
    val yearMon = calendar.get(Calendar.YEAR) + "-" + monStr
    LOG.info("write clustering info to HBase...")
    PutDataToHBase.putClusteringInfo(yearMon, table1List)

    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val putDataToEs = PutDataToEs.getInstance()
    trainResult.foreach(data => {
      val rowKey = yearMon + "-" + data._1
      println(rowKey)
      data._2.foreach(data => {
        val date = new Date(data.getAs[Timestamp]("time").getTime)
        val dateNew = sdf.format(date)
        val status = putDataToEs.upDateDataToEs(data.getAs[String]("spic"), rowKey, dateNew, data.getAs[Long]("id").toInt)
        if (status != 200) {
          LOG.info("Put data to es failed! And the failed ftpurl is " + data.getAs("spic"))
        }
      })
    })*/
    spark.stop()
  }

  def measure(v1: Array[Double], v2: Array[Double]): Double = {
    var distance = 0.0
    val aa = if (v1.length < v2.length) v1.length else v2.length
    for (i <- 0 until aa) {
      distance += scala.math.pow(v1(i) - v2(i), 2)
    }
    distance
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
    1 - (member / denominator)
  }

  private def getAverageDistance(points: util.ArrayList[Array[Double]]): Double = {
    var sum = 0
    val pointSize = points.size
    val iter1 = points.iterator()
    val points2 = points
    val iter2 = points2.iterator()

    val distanceNumber = pointSize * (pointSize - 1) / 2
    val T2 = sum / distanceNumber / 2 // 平均距离的一半
    return T2
  }

  def canopy_(p0: (String, Array[Double]), pair: mutable.HashSet[(String, Array[Double])], t2: Double) = {
    if (!pair.exists(p => cosineMeasure(p._2, p0._2) <= t2)) {
      pair += p0
      pair
    } else null
  }
}


