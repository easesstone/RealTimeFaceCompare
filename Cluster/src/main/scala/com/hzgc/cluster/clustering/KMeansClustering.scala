/*
package com.hzgc.kmeans

import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.util.{FtpFileOpreate, JDBCUtil, PropertiesUtils, SparkSessionSingleton}
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.mllib.clustering.{KMeans, KMeansModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable
import scala.reflect.io.Path

object KMeansClustering {
  val LOG = Logger.getLogger(KMeansClustering.getClass)
  val numClusters = 150
  val numIterations = 100000
  var clusterIndex: Int = 0

  def main(args: Array[String]) {

    val properties = PropertiesUtils.getProperties
    val appName = properties.getProperty("job.clustering.appName")
    val url = properties.getProperty("job.clustering.mysql.url")
    val tableName = properties.getProperty("job.clustering.mysql.table")
    val timeField = properties.getProperty("job.clustering.mysql.field.time")
    val dataField = properties.getProperty("job.clustering.mysql.field.data")
    val idField = properties.getProperty("job.clustering.mysql.field.id")

    val sparkConf = new SparkConf().setAppName(appName)
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    val mysqlData = spark.read.format("jdbc").option("url", url).option("dbtable", tableName)


    //如何按条件读取parquet文件需要修改
    //hdfs路径在本地运行时需要添加端口号
    //通过union方式来读取多个文件
    /* sparkSession.read.parquet("hdfs://hzgc:9000/user/hive/warehouse/person_table/date=2017-12-13")
       .union(sparkSession.read.parquet("hdfs://hzgc:9000/user/hive/warehouse/person_table/date=2017-12-09"))
       .union(sparkSession.read.parquet("hdfs://hzgc:9000/user/hive/warehouse/person_table/date=2017-12-07"))
       .union(sparkSession.read.parquet("hdfs://hzgc:9000/user/hive/warehouse/person_table/date=2017-12-08"))
       .select(ftpUrlColumn, featureColumn)
       .createOrReplaceTempView(tempViewName)*/

    //采用where条件语句，但是这样很明显spark会先扫描person_table下所有文件，然后再根据where中的条件过滤
    /* sparkSession.read.parquet("hdfs://hzgc:9000/user/hive/warehouse/person_table").where("date='2017-12-08'")
       .select(ftpUrlColumn, featureColumn)
       .createOrReplaceTempView(tempViewName)*/
    //通过设置option中basePath 来读取多个文件
    sparkSession.read.option("basePath", basePath).parquet(basePath + "date=2017-12-*") //.where("date='2017-12-08'")
      .select(ftpUrlColumn, featureColumn)
      .createOrReplaceTempView("tempView")
    sparkSession.read.jdbc()

    //udf for feature is null distinct
    sparkSession.udf.register("len", (x: mutable.WrappedArray[Float]) => x.length)
    //get data from DataFrame
    val resultDF = sparkSession.sql("select " + ftpUrlColumn + "," + featureColumn + " from tempView where len(" + featureColumn + ")=" + featureLength)

    val idPointRDD = resultDF.rdd.map(s => (s.getString(0), Vectors.dense(s.get(1).asInstanceOf[mutable.WrappedArray[Float]].toArray.map(_.toDouble)))).cache()
    val startTime = System.nanoTime

    val clusters: KMeansModel = KMeans.train(idPointRDD.map(_._2), numClusters, numIterations)
    val clustersRDD = clusters.predict(idPointRDD.map(_._2))
    val endTime = System.nanoTime
    val idClusterRDD = idPointRDD.map(_._1).zip(clustersRDD)
    //import sparkSession.sql
    import sparkSession.implicits._
    val idClusterDF = idClusterRDD.toDF(ftpUrlColumn, clusterColumn)
    idClusterDF.repartition($"cluster").foreachPartition(
      clusterPartition => {
        clusterPartition.foreach(s => {
          val ftpUrl = s.getAs[String](0)
          val clusterName = s.getAs[Int](1)
          val path: Path = Path(clusterOut + clusterColumn + clusterName)
          val currentPath = path.createDirectory(failIfExists = false)
          val pathStr = currentPath.toAbsolute.toString()
          FtpFileOpreate.downloadFtpFile(ftpUrl, pathStr, ftpUrl.substring(ftpUrl.lastIndexOf("/")))
        })
      }
    )

    println("*****************************************")
    println("聚类时间为：" + (endTime - startTime) / 1000000d)
    //Save and load model
    clusters.save(sc, "target/org/apache/spark/KMeansExample/KMeansModel")
    val sameModel = KMeansModel.load(sc, "target/org/apache/spark/KMeansExample/KMeansModel")
    sparkSession.stop()
  }

  /**
    * run kmeans and print of Sum of Squared Errors and center of every clusters
    *
    **/
  def transData(resultDF: DataFrame): Unit = {
    val parsedData = resultDF.rdd.map(s => Vectors.dense(s.get(1)
      .asInstanceOf[mutable.WrappedArray[Float]]
      .toArray.map(_.toDouble))).cache()
    val clusters: KMeansModel = KMeans.train(parsedData, numClusters, numIterations)
    val WSSSE = clusters.computeCost(parsedData)
    println("Within Set Sum of Squared Errors = " + WSSSE)
    println("Cluster Number:" + clusters.clusterCenters.length)
    println("Cluster Centers Information Overview:")
    clusters.clusterCenters.foreach(x => {
      println("Center Point of Cluster " + clusterIndex + ":")
      println(x)
      clusterIndex += 1
    })
  }

  /**
    * Query Data With Java（JDBC）
    */
  def getDataByJDBC(): Unit = {
    val conn = JDBCUtil.getConnection
    val statement = conn.createStatement()
    //UDF需要打成jar包注册到集群上
    val resultSet = statement.executeQuery("select ftpurl,feature from person_table limit 10")
    if (resultSet != null) {
      while (resultSet.next()) {
        println(resultSet.getString("ftpurl") + ":" + resultSet.getString("feature"))
      }
    }
  }
}

*/
