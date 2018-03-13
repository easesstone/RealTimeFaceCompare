package com.hzgc.cluster.clustering

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date, Properties}

import breeze.linalg.Transpose
import com.hzgc.cluster.clutering.ClusteringRaw
import com.hzgc.cluster.consumer.PutDataToEs
import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.dubbo.clustering.ClusteringAttribute
import org.apache.log4j.Logger
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.distributed.{IndexedRow, IndexedRowMatrix, RowMatrix}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Encoders, SparkSession}
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable

object ClusteringNew {

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

    val spark = SparkSession.builder().appName(appName).master("local[*]").enableHiveSupport().getOrCreate()
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
    val idPointDS = joinData.map(data => (data.getAs[String]("spic"), data.getAs[mutable.WrappedArray[Float]]("feature").toArray
      .map(_.toDouble))).persist(StorageLevel.MEMORY_AND_DISK_SER)

    val idPointDS2 = joinData.map(data => (data.getAs[String]("spic"), Vectors.dense(
      data.getAs[mutable.WrappedArray[Float]]("feature").toArray
        .map(_.toDouble)))).rdd
    val count = joinData.count()
    println(count)
    //use blockMatrix
    val zipIdPointDs = idPointDS2.zipWithIndex()
    val mat = zipIdPointDs.map((data) => new IndexedRow(data._2, data._1._2))
    //transform the mat to block,after tranposed,then change back to IndexedRowMatrix
    val irm = (new IndexedRowMatrix(mat).toBlockMatrix()).cache()
    irm.validate();
    val matB = irm.transpose.toIndexedRowMatrix()
      .columnSimilarities()
      .entries
      .filter(data => data.value > threshold)
      .groupBy(key => key.i)

    matB.foreach(data=>{
      val key=data._1
    })


    //use rowMatrix
    /* val mat = idPointDS2.map(data=>data._2)
    val temp=new IndexedRowMatrix(mat.zipWithIndex().map(case(v,i)=>(i,v))).toBlockMatrix().cache()
    val rowMatrix=new RowMatrix(mat)
    rowMatrix

    val tranRowMatrix =transposeRowMatrix(rowMatrix)
    val columnSimilarity=tranRowMatrix.columnSimilarities().entries
    val temp=columnSimilarity.count()
    println(temp)*/

    /*Transpose(irm)*/

    /*irm.columnSimilarities().entries.groupBy(data=>data.i).sortByKey().foreach(println(_))*/

    /* //zipwithIndex for decrease the computer cost
     val zipIdPointDs = idPointDS.sample(false, 0.2).rdd.zipWithIndex()
     val joined = zipIdPointDs.cartesian(zipIdPointDs)
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
     }).filter(_._3 > timeCount).zipWithIndex().cache()

     //merge two list
     val joinNumFliter = numPerUrl.cartesian(numPerUrl).filter(f => f._1._2 < f._2._2)
     val unionData = joinNumFliter.map(data =>
       (data._1._1._1, data._2._1._1, data._1._1._2, data._2._1._2, dataSetSimilarity(data._1._1._2, data._2._1._2)))
       .filter(data => data._5 > repetitionRate)

     val lastData = unionData.map(data => {
       val key = data._1
       val unionList = data._3 ::: data._4
       (key, unionList, unionList.size)
     })


     val mon = calendar.get(Calendar.MONTH)
     //+1
     var monStr = ""
     if (mon < 10) {
       monStr = "0" + mon
     } else {
       monStr = String.valueOf(mon)
     }
     val yearMon = calendar.get(Calendar.YEAR) + "-" + monStr
     val clusteringRowKey = yearMon + "region"
     var i = 0
     val viewData = joinData.select("id", "time", "ipc", "host", "spic", "bpic")
     val table1List = new util.ArrayList[ClusteringAttribute]()
     lastData.map(data => {
       val clusteringAttribute = new ClusteringAttribute
       clusteringAttribute.setClusteringId(i.toString)
       clusteringAttribute.setCount(data._3)
       clusteringAttribute.setFtpUrl(data._1)
       val dataListDF = data._2.toDF()
       dataListDF.printSchema()
       val fullInfoDf = dataListDF.join(viewData, dataListDF("_1") === viewData("spic"))
       val orderData = fullInfoDf.orderBy(fullInfoDf("time"))
       val idList = new util.ArrayList[Integer]()
       val rowKey = clusteringRowKey + i
       val putDataToEs = PutDataToEs.getInstance()
       val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
       orderData.foreach(
         data => {
           val date = new Date(data.getAs[Timestamp]("time").getTime)
           val dateNew = sdf.format(date)
           val status = putDataToEs.upDateDataToEs(data.getAs[String]("spic"), rowKey, dateNew, data.getAs[Long]("id").toInt)
           if (status != 200) {
             LOG.info("Put data to es failed! And the failed ftpurl is " + data.getAs("spic"))
           }
         }
       )
       val first = orderData.first()
       val last = orderData.orderBy(-orderData("time")).first()
       clusteringAttribute.setFirstAppearTime(first.getAs("time").toString)
       clusteringAttribute.setFirstIpcId(first.getAs("ipc"))
       clusteringAttribute.setLastAppearTime(last.getAs("time").toString)
       clusteringAttribute.setLastIpcId(last.getAs("ipc"))
       i += 1
       clusteringAttribute
     }).foreach(data => table1List.add(data))
     PutDataToHBase.putClusteringInfo(clusteringRowKey, table1List)*/

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

  def transpose(m: Array[Array[Double]]): Array[Array[Double]] = {
    (for {c <- m(0).indices}
    //该行所有元素下标
      yield {
        m.map(_ (c))
      }
      ).toArray //每一行取
  }

  def transposeRowMatrix(m: RowMatrix): RowMatrix = {
    val indexedRM = new IndexedRowMatrix(m.rows.zipWithIndex.map({
      case (row, idx) => new IndexedRow(idx, row)
    }))
    val transposed = indexedRM.toCoordinateMatrix().transpose.toIndexedRowMatrix()
    new RowMatrix(transposed.rows
      .map(idxRow => (idxRow.index, idxRow.vector))
      .sortByKey().map(_._2))
  }

}
