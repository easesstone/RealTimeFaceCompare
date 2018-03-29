package com.hzgc.cluster.clustering

import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date, Properties, UUID}

import com.hzgc.cluster.consumer.PutDataToEs
import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.dubbo.clustering.ClusteringAttribute
import com.hzgc.jni.ClusteringFunction
import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable

object ResidentClustering {

  case class Data(id: Long, time: Timestamp, ipc: String, host: String, spic: String, bpic: String)

  case class DataWithFeature(id: Long, time: Timestamp, ipc: String, host: String, spic: String, bpic: String, feature: Array[Float])

  case class CenterData(num: Int, data: Array[Double])

  //exception when write to es
  System.setProperty("es.set.netty.runtime.available.processors", "false")

  val LOG: Logger = Logger.getLogger(ResidentClustering.getClass)

  def main(args: Array[String]) {

    val driverClass = "com.mysql.jdbc.Driver"
    val sqlProper = new Properties()
    val properties = PropertiesUtils.getProperties
    val appName = properties.getProperty("job.clustering.appName")
    val url = properties.getProperty("job.clustering.mysql.url")
    val timeField = properties.getProperty("job.clustering.mysql.field.time")
    val idField = properties.getProperty("job.clustering.mysql.field.id")
    val hostField = properties.getProperty("job.clustering.mysql.field.host")
    val spicField = properties.getProperty("job.clustering.mysql.field.spic")
    val bpicField = properties.getProperty("job.clustering.mysql.field.bpic")
    val resultPath = properties.getProperty("job.clustering.result.path")
    val similarityThreshold = properties.getProperty("job.clustering.similarity.Threshold").toDouble
    val appearCount = properties.getProperty("job.clustering.appear.count").toInt
    val spark = SparkSession.builder().appName(appName).master("local[*]").enableHiveSupport().getOrCreate()
    val uuidString = UUID.randomUUID().toString
    import spark.implicits._

    val calendar = Calendar.getInstance()
    val mon = calendar.get(Calendar.MONTH)
    val year = calendar.get(Calendar.YEAR)
    val resultFileName = year + "-" + mon + "-" + uuidString + ".txt"
    val currentYearMon = "'" + year + "-%" + mon + "%'"

    spark.udf.register("feature_len", (x: mutable.WrappedArray[Float]) => x.length)
    spark.sql("select ftpurl,feature from person_table where date like " + currentYearMon + "and feature_len(feature)>0").distinct().createOrReplaceTempView("parquetTable")

    val preSql = "(select T1.id, T2.host_name, " + "T2.big_picture_url, T2.small_picture_url, " + "T1.alarm_time " + "from t_alarm_record as T1 inner join t_alarm_record_extra as T2 on T1.id=T2.record_id " + "where T2.static_id IS NULL " + "and DATE_FORMAT(T1.alarm_time,'%Y-%m') like " + currentYearMon + ") as temp"

    sqlProper.setProperty("driver", driverClass)

    val dataSource = spark.read.jdbc(url, preSql, sqlProper)

    dataSource.map(data => {
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
      LOG.info("start clustering region" + finalStr)

      val joinData = spark.sql("select T2.*, T1.feature from parquetTable as T1 inner join mysqlTable as T2 on T1.ftpurl=T2.spic where T2.ipc in " + finalStr)

      //prepare data
      val idPointRDD = joinData.rdd.map(data => DataWithFeature(data.getAs[Long]("id"), data.getAs[Timestamp]("time"), data.getAs[String]("spic").split("/")(3), data.getAs[String]("host"), data.getAs[String]("spic"), data.getAs[String]("bpic"), data.getAs[mutable.WrappedArray[Float]]("feature").toArray)).persist(StorageLevel.MEMORY_AND_DISK_SER)
      val dataSize = idPointRDD.count().toInt
      val points = idPointRDD.collect()
      val features = points.flatMap(data => data.feature)
      val status = ClusteringFunction.clusteringComputer(features, dataSize, similarityThreshold, appearCount, resultFileName, resultPath)

      val putDataToEs = PutDataToEs.getInstance()
      val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val clusterList = new util.ArrayList[ClusteringAttribute]()

      var monStr = ""
      if (mon < 10) {
        monStr = "0" + mon
      } else {
        monStr = String.valueOf(mon)
      }
      val yearMon = year + "-" + monStr
      val rowKey = yearMon + "-" + region
      //get the days of this month
      calendar.set(Calendar.MONTH, mon - 1)
      val totalDay = calendar.getActualMaximum(Calendar.DATE)
      val dateOfMonth = new Array[Int](totalDay)

      if (status == 1) {
        LOG.info("clustering result saved")
        val resident_raw = spark.read.textFile("file:///" + resultPath + File.separator + resultFileName).map(data => data.split(" ")).collect().toList
        for (i <- resident_raw.indices) {
          val dataArr = resident_raw(i)
          val dateArr = new Array[Int](dataArr.length)
          val clusterId = dataArr(1)
          LOG.info("clusterId:" + clusterId)
          val dataList = new util.ArrayList[DataWithFeature]()
          LOG.info("dataArr length:" + dataArr.length)
          for (k <- dataArr.indices) {
            val data = points(dataArr(k).toInt)
            dateArr(k) = data.time.toLocalDateTime.getDayOfMonth
          }
          val distinctDate = dateArr.distinct.sortWith((a, b) => a < b)
          //set 1 if the day number appeared
          for (l <- distinctDate) {
            dateOfMonth(l) = 1
          }

          //get the max consitent time
          var count = 0
          var temp = 0
          for (n <- dateOfMonth) {
            if (n == 1) {
              temp += 1
            } else {
              count = if (count > temp) count else temp
              temp = 0
            }
          }
          if (count >= appearCount) {
            for (j <- dataArr.indices) {
              val fullData = points(dataArr(j).toInt)
              dataList.add(fullData)
              val clusteringId = yearMon + "-" + region + "-" + clusterId + "-" + uuidString
              val date = new Date(fullData.time.getTime)
              val dateNew = sdf.format(date)
              val status = putDataToEs.upDateDataToEs(fullData.spic, clusteringId, dateNew, fullData.id.toInt)
              if (status != 200) {
                LOG.info("Put data to es failed! And the failed ftpUrl is " + fullData.spic)
              } else {
                LOG.info("Put data to es successful! the ftpUrl is " + fullData.spic)
              }
            }
            val attribute = new ClusteringAttribute()
            attribute.setClusteringId(region + "-" + clusterId + "-" + uuidString) //region + "-" + uuidString + "-" + data._1.toString
            attribute.setCount(dataArr.length)
            attribute.setLastAppearTime(dataList.get(0).time.toString)
            attribute.setLastIpcId(dataList.get(0).ipc)
            attribute.setFirstAppearTime(dataList.get(dataList.size() - 1).time.toString)
            attribute.setFirstIpcId(dataList.get(dataList.size() - 1).ipc)
            attribute.setFtpUrl(dataList.get(0).spic)
            clusterList.add(attribute)
          }
        }
        LOG.info("put clustering data to HBase...")
        PutDataToHBase.putClusteringInfo(rowKey, clusterList)
        LOG.info("put clustering data to HBase successful")
      } else {
        LOG.info("clustering failed, please check the parameter of function clusteringComputer")
      }
      LOG.info("end clustering region" + finalStr)
    }
    spark.stop()
  }
}