package com.hzgc.cluster.smallfile

import java.io.File

import org.apache.spark.sql.SparkSession

object SparkSessionSingleton {
    @transient private var instance: SparkSession = _
    val warehouseLocation = new File("spark-warehouse").getAbsolutePath

    def getInstance(): SparkSession = {
        if (instance == null) {
            instance = SparkSession.builder()
                .appName("combine-parquest-demo")
                .master("local[*]")
                .config("spark.sql.parquet.compression.codec", "snappy")
                .config("parquet.block.size", "16777216")
                .config("spark.sql.warehouse.dir", warehouseLocation)
                .enableHiveSupport()
                .getOrCreate()
        }
        instance
    }
}
