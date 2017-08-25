package com.hzgc.streaming.putdata

import java.io.File

import com.hzgc.hbase.util.HBaseHelper
import com.hzgc.streaming.util.ImageToBytes
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}

object BacthImportData {
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("Parameter error, please check!")
      System.exit(1)
    }
    val conf = new SparkConf().setMaster("yarn-client").setAppName("BacthImportData")
    val sc = new SparkContext(conf)
    val conn = HBaseHelper.getHBaseConnection
    val tableName = TableName.valueOf("objectinfo")
    val cf = Bytes.toBytes("person")
    val platformid = Bytes.toBytes("platformid")
    val tag = Bytes.toBytes("tag")
    val pkey = Bytes.toBytes("pkey")
    val name = Bytes.toBytes("name")
    val idcard = Bytes.toBytes("idcard")
    val sex = Bytes.toBytes("sex")
    val creator = Bytes.toBytes("creator")
    val cphone = Bytes.toBytes("cphone")
    val photo = Bytes.toBytes("photo")
    val feature = Bytes.toBytes("feature")
    val fileRDD = sc.textFile(args(0), 6).map(_.split("ZHONGXIAN")).map(record => {
      val file = new File(record(0))
      if (file.exists()) {
        val photoByte = ImageToBytes.image2byte(record(0))
        val photoName = record(0).split("/")(record(0).split("/").length -1).split(".")(0)
        val table = conn.getTable(tableName)
        val put = new Put(Bytes.toBytes(record(1) + photoName))
        put.addColumn(cf, platformid, Bytes.toBytes(record(2)))
        put.addColumn(cf, tag, Bytes.toBytes("1"))
        put.addColumn(cf, pkey, Bytes.toBytes(record(1)))
        put.addColumn(cf, name, Bytes.toBytes(photoName))
        put.addColumn(cf, idcard, Bytes.toBytes(photoName))
        put.addColumn(cf, sex, Bytes.toBytes("1"))
        put.addColumn(cf, creator, Bytes.toBytes("chenke"))
        put.addColumn(cf, cphone, Bytes.toBytes("1888888"))
        put.addColumn(cf, photo, photoByte)
        if (record(3) != "null") {
          put.addColumn(cf, feature, record(3).getBytes("ISO8859-1"))
        }
        table.put(put)
        table.close()
      }
      true
    }).collect()
  }
}
