package com.hzgc.cluster.clustering

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date, Properties, UUID}

import com.hzgc.cluster.consumer.PutDataToEs
import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.dubbo.clustering.ClusteringAttribute
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD._
import org.apache.log4j.Logger
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.SparkSession

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object KMeansClustering {

  case class Data(id: Long, time: Timestamp, ipc: String, host: String, spic: String, bpic: String)

  case class CenterData(num: Int, data: Array[Double])

  //exception when write to es
  System.setProperty("es.set.netty.runtime.available.processors", "false")

  val LOG: Logger = Logger.getLogger(KMeansClustering.getClass)
  def main(args: Array[String]) {

    val driverClass = "com.mysql.jdbc.Driver"
    val sqlProper = new Properties()
    val properties = PropertiesUtils.getProperties
    val clusterNum = properties.getProperty("job.clustering.cluster.number")
    val similarityThreshold=properties.getProperty("job.clustering.similarity.Threshold").toDouble
    val center_similarityThreshold=properties.getProperty("job.clustering.similarity.center.Threshold").toDouble
    val appearCount = properties.getProperty("job.clustering.appear.count").toInt
    val iteraterNum = properties.getProperty("job.clustering.iterater.number").toInt
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

    val spark = SparkSession.builder().appName(appName).enableHiveSupport().getOrCreate()
    import spark.implicits._

    val calendar = Calendar.getInstance()

    val currentYearMon = "'" + calendar.get(Calendar.YEAR) + "-%" + (calendar.get(Calendar.MONTH) + 1) + "%'"

    spark.sql("select ftpurl,feature from person_table where date like " + currentYearMon).createOrReplaceTempView("parquetTable")

    val preSql = "(select T1.id, T2.host_name, " + "T2.big_picture_url, T2.small_picture_url, " + "T1.alarm_time " + "from t_alarm_record as T1 inner join t_alarm_record_extra as T2 on T1.id=T2.record_id " + "where T2.static_id IS NULL " + "and DATE_FORMAT(T1.alarm_time,'%Y-%m') like " + currentYearMon + ") as temp"

    sqlProper.setProperty("driver", driverClass)

    val dataSource = spark.read.jdbc(url, preSql, sqlProper)

    dataSource.map(data => {
      println("ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](spicField))
      Data(data.getAs[Long](idField), data.getAs[Timestamp](timeField), data.getAs[String](spicField).substring(1, data.getAs[String](spicField).indexOf("/", 1)), data.getAs[String](hostField), "ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](spicField), "ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](bpicField))
    }).createOrReplaceTempView("mysqlTable")

    //get the region and ipcidlist
    val region_ipc_sql = "(select T1.region_id,GROUP_CONCAT(T2.serial_number) " + "as serial_numbers from t_region_department as T1 inner join " + "(select concat(dep.parent_ids,',',dep.id) as path ,T3.serial_number from " + "t_device as dev left join t_department as dep on dev.department_id = dep.id inner join " + "t_device_extra as T3 on dev.id = T3.device_id ) as T2 on T2.path " + "like concat('%',T1.department_id,'%') group by T1.region_id " + "order by T1.region_id,T2.serial_number ) as test"
    val region_ipc_data = spark.read.jdbc(url, region_ipc_sql, sqlProper).collect()
    val region_ipcMap = mutable.HashMap[Int, String]()
    region_ipc_data.foreach(data => region_ipcMap.put(data.getAs[Int](0), data.getAs[String](1)))
    region_ipcMap.foreach(println(_))

    for (i <- region_ipcMap) {
      val region = i._1
      val ipcList = i._2.split(",")
      val j = 0
      var ipcStr = ""
      for (j <- 0 until ipcList.length) {
        if (j != ipcList.length - 1) {
          ipcStr += "'" + ipcList(j) + "'" + ","
        } else {
          ipcStr += "'" + ipcList(j) + "'"
        }
      }
      var finalStr = ""
      finalStr += "(" + ipcStr + ")"

      val joinData = spark.sql("select distinct T2.*,T1.feature from parquetTable as T1 inner join mysqlTable as T2 on T1.ftpurl=T2.spic where T2.ipc in " + finalStr)
      //prepare data
      val idPointRDD = joinData.rdd.map(data => (data.getAs[String]("spic"), Vectors.dense(data.getAs[mutable.WrappedArray[Float]]("feature").toArray.map(_.toDouble)))).cache()

      //train the model
      println("data count :" + idPointRDD.count())
      val numClusters = Math.sqrt(idPointRDD.count().toDouble).toInt
      val kMeansModel = KMeans.train(idPointRDD.map(data => data._2), numClusters, iteraterNum)

      //predict each point belong to which clustering center and filter by similarityThreshold
      val data_Center = idPointRDD.map(_._2).map(p => (kMeansModel.predict(p), kMeansModel.clusterCenters.apply(kMeansModel.predict(p)), p))
      val data_Center_Feature = data_Center.zip(idPointRDD.map(_._2))
      val point_center_dist = data_Center_Feature.map(data => (data._1._1, cosineMeasure(data._1._2.toArray, data._2.toArray)))
      val viewData = joinData.select("id", "time", "ipc", "host", "spic", "bpic", "feature").rdd
      val predictResult = point_center_dist.zip(viewData).distinct().groupBy(key => key._1._1).mapValues(f => {
        f.toList.filter(data => data._1._2 > similarityThreshold).sortWith((a, b) => a._1._2 > b._1._2)
      }).filter(data => data._2.nonEmpty)


      val keyList = predictResult.map(data => data._1).collect().toList

      //get the top simialarity point of each clustering
      val topPoint_center = predictResult.map(data => (data._1, data._2.head._2.getAs[mutable.WrappedArray[Float]]("feature").toArray.map(_.toDouble)))
      //get the top point of each clustering
      val centerList = new util.ArrayList[CenterData]()

      println(topPoint_center.count())

      topPoint_center.collect().foreach(x => {
        val clusterID = x._1
        val featureData = x._2
        val centerData = CenterData(clusterID, featureData)
        centerList.add(centerData)
      })
      println(centerList.size())

      //compare each two center points and merge it when the similarity is larger than the threshold
      val centerListTmp = new util.ArrayList[CenterData]()
      centerListTmp.addAll(centerList)
      val deleteCenter = new util.ArrayList[Int]()
      val union_center = new util.HashMap[Int, ArrayBuffer[Int]]
      for (k <- 0 until centerListTmp.size()) {
        val first = centerListTmp.get(k)
        if (!deleteCenter.contains(first.num)) {
          val centerSimilarity = ArrayBuffer[Int]()
          val iter = centerList.iterator()
          while (iter.hasNext) {
            val second = iter.next()
            val pairSim = cosineMeasure(first.data, second.data)
            if (pairSim > center_similarityThreshold) {
              deleteCenter.add(second.num)
              centerSimilarity += second.num
              iter.remove()
            }
          }
          union_center.put(first.num, centerSimilarity)
        }
      }

      //union similarity clustering
      predictResult.map(data => (data._1, data._2)).sortByKey()
      val indexedResult = IndexedRDD(predictResult).cache()
      val iter_center = union_center.keySet().iterator()
      var indexed1 = indexedResult
      while (iter_center.hasNext) {
        val key = iter_center.next()
        if (keyList.contains(key)) {
          val value = union_center.get(key)
          if (value.length > 1) {
            val first_list_option = indexed1.get(key).orNull
            if (first_list_option != null && first_list_option.size > 1) {
              for (i <- 1 until value.length) {
                val first_list = first_list_option
                val cluster_tmp = value(i)
                val arrayBuffer = ArrayBuffer[Int]()
                val second_list = indexed1.get(cluster_tmp).orNull
                if (second_list != null && second_list.size > 1) {
                  val topSim = cosineMeasure(first_list.head._2.getAs[mutable.WrappedArray[Float]]("feature").toArray.map(_.toDouble), second_list.apply(1)._2.getAs[mutable.WrappedArray[Float]]("feature").toArray.map(_.toDouble))
                  if (topSim > center_similarityThreshold) {
                    indexed1 = indexed1.put(key, first_list.union(second_list.drop(0)))
                    arrayBuffer += cluster_tmp
                    println(indexed1.count())
                  }
                }
                indexed1 = indexed1.delete(arrayBuffer.toArray)
                println(indexed1.count())
              }
            }
          }
        }
      }
      println(indexed1.count())

      //put all the clustering data to HBase
      val table1List = new util.ArrayList[ClusteringAttribute]()
      val uuidString = UUID.randomUUID().toString
      val lastResult = indexed1.filter(data => data._2.length > appearCount)
      lastResult.map(data => (data._1, data._2.toArray.sortWith((a, b) => a._2.getString(1).split(".")(1) > b._2.getString(1).split(".")(1))))
      lastResult.map(data => {
        val attribute = new ClusteringAttribute()
        attribute.setClusteringId(region + "-" + data._1.toString + "-" + uuidString) //region + "-" + uuidString + "-" + data._1.toString
        attribute.setCount(data._2.length)
        attribute.setLastAppearTime(data._2.head._2.getTimestamp(1).toString)
        attribute.setLastIpcId(data._2.head._2.getAs[String]("ipc"))
        attribute.setFirstAppearTime(data._2.last._2.getTimestamp(1).toString)
        attribute.setFirstIpcId(data._2.last._2.getAs[String]("ipc"))
        attribute.setFtpUrl(data._2.head._2.getAs[String]("spic"))
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
      val rowKey = yearMon + "-" + region
      LOG.info("write clustering info to HBase...")
      PutDataToHBase.putClusteringInfo(rowKey, table1List)

      //put each clustering data to es
      val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val putDataToEs = PutDataToEs.getInstance()
      lastResult.foreach(data => {
        val rowKey = yearMon + "-" + region + "-" + data._1 + "-" + uuidString
        val clusterId = rowKey + "-" + data._1 + "-" + uuidString
        println(clusterId)
        data._2.foreach(p => {
          val date = new Date(p._2.getAs[Timestamp]("time").getTime)
          val dateNew = sdf.format(date)
          val status = putDataToEs.upDateDataToEs(p._2.getAs[String]("spic"), clusterId, dateNew, p._2.getAs[Long]("id").toInt)
          if (status != 200) {
            LOG.info("Put data to es failed! And the failed ftpurl is " + p._2.getAs("spic"))
          }
        })
      })
    }
    spark.stop()
  }

  def cosineMeasure(v1: Array[Double], v2: Array[Double]): Double = {

    val member = v1.zip(v2).map(d => d._1 * d._2).sum
    //求出分母第一个变量值
    val temp1 = math.sqrt(v1.map(num => {
      math.pow(num, 2)
    }).sum)
    //求出分母第二个变量值
    val temp2 = math.sqrt(v2.map(num => {
      math.pow(num, 2)
    }).sum)
    //求出分母
    val denominator = temp1 * temp2
    //进行计算
    0.5 + 0.5 * (member / denominator)
  }
}

