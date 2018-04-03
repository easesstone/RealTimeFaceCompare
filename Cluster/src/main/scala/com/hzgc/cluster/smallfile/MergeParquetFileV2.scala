package com.hzgc.cluster.smallfile

import java.io.File
import java.sql.Timestamp
import java.util

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.Logger
import org.apache.spark.sql.SaveMode

import scala.collection.mutable.ArrayBuffer

/**
  * 概述：
  *     最终生成的parquet文件，存在的目录大致如下：
  *     /user/hive/warehouse/person_table/date=2018-01-12/ipcid=3K01E84PAU00083
  *     /user/hive/warehouse/person_table/date=2018-01-12/ipcid=DS-2CD2T20FD-I320160122AACH571485690
  *     这样只要进入到每个目录，进行分别处理，每个摄像头的数据分别处理，而不是把一整天的数据
  *     一下子读取进来。
  */
object MergeParquetFileV2 {
    val LOG = Logger.getLogger(MergeParquetFileV2.getClass)

    case class PictureV1(ftpurl: String, //图片搜索地址
                       //feature：图片特征值 ipcid：设备id  timeslot：时间段
                       feature: String, ipcid: String, timeslot: Int,
                       //timestamp:时间戳 pictype：图片类型 date：时间
                       exacttime: Timestamp, searchtype: String, date: String,
                       //人脸属性：眼镜、性别、头发颜色
                       eyeglasses: Int, gender: Int, haircolor: Int,
                       //人脸属性：发型、帽子、胡子、领带
                       hairstyle: Int, hat: Int, huzi: Int, tie: Int
                      )

    case class PictureV2(ftpurl: String, //图片搜索地址
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

    /**
      * 将特征值（String）转换为特征值（float[]）（内）（赵喆）
      *
      * @param feature 传入编码为UTF-8的String
      * @return 返回float[]类型的特征值
      */
    def string2floatArray(feature: String): Array[Float] = {
        if (feature != null && feature.length > 0) {
            val featureFloat = new Array[Float](512)
            val strArr : Array[String] = feature.split(":")
            var i = 0
            while ( i < strArr.length) {
                featureFloat(i) = strArr(i).toFloat
                i = i + 1
            }
            return featureFloat
        }
        new Array[Float](0)
    }

    def main(args: Array[String]): Unit = {
        val start = System.currentTimeMillis
        if (args.length != 5) {
            System.out.print(
                s"""
                   |Usage: MergeParquetFile <hdfsClusterName> <tmpTableHdfsPath> <hisTableHdfsPath> <tableName> <dateString>
                   |<hdfsClusterName> 例如：hdfs://hacluster或者hdfs://hzgc
                   |<tmpTableHdfsPath> 临时表的根目录，需要是绝对路径
                   |<hisTableHdfsPath> 合并后的parquet文件，即总的或者历史文件的根目录，需要是绝对路径
                   |<tableName> 表格名字，最终保存的动态信息库的表格名字
                   |<dateString > 用于表示需要合并的某天的内容
             """.stripMargin)
            System.exit(1)
        }
        // 接收传进来的四个或者五个个参数
        val hdfsClusterName = args(0)
        val hisTableHdfsPath = args(2)
        val dateString = args(4)

        // 初始化SparkSession，SparkSession 是单例模式的
        val sparkSession = SparkSessionSingleton.getInstance
        // 根据sparkSession 得到SparkContext
        val sc = sparkSession.sparkContext
        // 设置hdfs 集群名字
        sc.hadoopConfiguration.set("fs.defaultFS", hdfsClusterName)
        // 获取hsfs 文件系统的实例
        val fs = FileSystem.get(sc.hadoopConfiguration)
        //获取person_table/date=2018-02-01 下的所有文件
        val parquetFiles: util.ArrayList[String] = new util.ArrayList[String]()
        // 最终需要遍历的目录例如：/user/hive/warehouse/person_table/date=2018-02-01
        val finalPath = hisTableHdfsPath + File.separator + "date=" + dateString
        ReadWriteHDFS.getParquetFiles(new Path(finalPath), fs, parquetFiles)

        val numOfFiles = parquetFiles.size()
        // 把parquet 文件的list 转换成数组
        val pathArr : Array[String] = new Array[String](numOfFiles)
        // 如果里面没有文件或者文件个数为1，直接跳过
        var count = 0
        while (count < pathArr.length) {
            pathArr(count) = parquetFiles.get(count)
            count = count + 1
        }

        val personDF = sparkSession.sql("select * from person_table where date='" + dateString + "'")

        import sparkSession.implicits._
        val personDs = personDF.as[PictureV1]

        val resultFilePath = hisTableHdfsPath + "vv" + File.separator + "date=" + dateString

        // 保存文件
        val finalPersonDf = personDs.mapPartitions(persons => {
            val results = ArrayBuffer[PictureV2]()
            while (persons.hasNext) {
                val person = persons.next()
                results += PictureV2(person.ftpurl,string2floatArray(person.feature), person.ipcid,
                    person.timeslot, person.exacttime, person.searchtype,
                    person.date, person.eyeglasses, person.gender, person.haircolor,
                    person.hairstyle, person.hat, person.huzi, person.tie, 0)
            }
            results.iterator
        }).toDF()
        finalPersonDf.coalesce(1).repartition(SmallFileUtils.takePartition(110, 100, pathArr, fs))
            .write.mode(SaveMode.Append).parquet(resultFilePath)

        LOG.info("===========================" + finalPersonDf.count())
        // 删除已经被合并的文件
//        ReadWriteHDFS.delV2(pathArr, fs)

        sparkSession.close()
        LOG.info("*************************************************************************************")
        LOG.info("总共花费的时间是: " + (System.currentTimeMillis() - start))
        LOG.info("*************************************************************************************")
    }
}
