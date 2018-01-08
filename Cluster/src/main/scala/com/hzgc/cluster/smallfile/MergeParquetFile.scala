package com.hzgc.cluster.smallfile

import java.io.File
import java.util

import org.apache.hadoop.fs.{ContentSummary, FileSystem, Path}
import org.apache.log4j.Logger
import org.apache.spark.sql.{SaveMode, SparkSession}

/**
  * 总体：针对临时文件，每15分钟进行一次合并检查（脚本1）。（针对真正的表格，进行合并（脚本2））
  * (注意core-site.xml 和hdfs-site.xml 和hive-site.xml 需要放在resources 目录下，最终需要放到conf 目录下)
  * A， 合并临时temp 目录下的文件
  * 1，遍历tmpTableHdfsPath 目录（临时目录），遍历出所有parquet 文件
  *   （即以.parquet 文件结尾的文件），得到对象files
  * 2，通过files，通过sparkSession 进行加载数据，得到df,并且注册成临时表personTmp
  * 3, 查询加载数据中的时间和设备的对应关系，得到一个dateFrame 对象
  * 4, 遍历hisTableHdfsPath目录，最终表格的根目录，得到时间和设备对应关系set02
  * 5，对比set01 和set02,根据set01 中存在，set02 中不存在的数据，在最终的hive 表格中创建元数据
  * 6，通过api 把数据写入到最终的表格中
  * 7，删除原来的文件
  *
  * B，合并finalTable 中的文件
  * 1，根据时间遍历当前日期的各个设备的数据。
  * 2，重新分区。
  * 3，删除原来的文件。
  *
  * 说明：
  * 用法：
  * I, 配置如下两个文件。
  * resource 中的第一个文件merget-final-table.properties
  * 内容如下：（用来合并最终的表格）
  * hdfs://hzgc
  * /user/hive/warehouse/person_table_demonima
  * person_table_demonima
  * 分别表示 hdfs ha 模式下的名字
  * 最终表格的绝对路径，及合并后的表格的绝对路径，
  * 表格名
  *
  * 第二个文件：person_table_demonima
  * 内容如下：（用来合并临时文件）
  * hdfs://hzgc
  * /user/hive/warehouse/person_parquet_demo00
  * /user/hive/warehouse/person_table_demonima
  * person_table_demonima
  * 用于合并临时目录的小文件
  *
  * II，启动如下两个脚本（做定时启动）
  * schema-merge-final-table.sh  （合并最终的表格，每隔3到5小时合并一次）
  * schema-merge-parquet-file.sh   (定时启动，合并临时文件，每15分钟启动一次)
  * schema-merge-final-table-crash.sh （定时任务，每天凌晨2点执行一次，用于合并前一天的数据，让最终的表格尽可能地少文件）
  * 通过以上内容，每天，每个设备下，最多有2到3个文件，每个文件的大小为256 M
  */
object MergeParquetFile {
    val LOG = Logger.getLogger(MergeParquetFile.getClass)
    def main(args: Array[String]): Unit = {
        if (args.length != 4  && args.length != 5) {
            System.out.print(s"""
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
        val tmpTableHdfsPath = args(1)
        val hisTableHdfsPath = args(2)
        val tableName = args(3)

        var dateString = ""
        if (args.length == 5) {
            dateString = args(4)
        }

        // 初始化SparkSession，SparkSession 是单例模式的
        val sparkSession = SparkSessionSingleton.getInstance
        import sparkSession.sql
        // 根据sparkSession 得到SparkContext
        val sc = sparkSession.sparkContext
        // 设置hdfs 集群名字
        sc.hadoopConfiguration.set("fs.defaultFS", hdfsClusterName)
        // 获取hsfs 文件系统的实例
        val fs = FileSystem.get(sc.hadoopConfiguration)

        // 1，遍历tmpTableHdfsPath 目录（临时目录），遍历出所有parquet 文件
        //   （即以.parquet 文件结尾的文件），得到对象files
        val parquetFiles : util.ArrayList[String] = new util.ArrayList[String]()
        if (dateString == null || dateString.equals("")) {
            ReadWriteHDFS.getParquetFiles(new Path(tmpTableHdfsPath),fs, parquetFiles)
        } else {
            ReadWriteHDFS.getParquetFiles(dateString, new Path(tmpTableHdfsPath),fs, parquetFiles)
        }
        var pathArr : Array[String] = new Array[String](parquetFiles.size())
        if (parquetFiles.size() >= 10000) {
            pathArr = new Array[String](10000)
        }
        var count = 0
        while (count < pathArr.length) {
            pathArr(count) = parquetFiles.get(count)
            count = count + 1
        }
        if (parquetFiles.size() == 0) {
            LOG.info("there is no parquet files in mid_table, please check the streaming store application.")
            System.exit(1)
        }

        // 2，通过files，通过sparkSession 进行加载数据，得到df,并且注册成临时表personTmp
        var personDF = sparkSession.read.parquet(pathArr : _*)
        if (dateString == null || dateString.equals("")) {
            personDF.persist()
            if (personDF.count() == 0) {
                LOG.info("there are parquet files, but no data in parquet files, just to delete the files.")
                // 删除临时表格中的文件
                ReadWriteHDFS.delV2(pathArr, fs);
                System.exit(2)
            }
            personDF.printSchema()

            // 3，查询加载数据中的时间和设备的对应关系，得到一个set01 对象
            val tmpPath = hdfsClusterName + "/user/hive/warehouse/dynamic_person_tmp_hzgc"
            personDF.coalesce(1).repartition(takePartition(tmpTableHdfsPath, fs))
                .write.mode(SaveMode.Append)
                .partitionBy("date", "ipcid")
                .parquet(tmpPath)
            val setOfTempTable : util.Set[String] = new util.HashSet[String]()
            ReadWriteHDFS.getPartitions(new Path(tmpPath), fs, setOfTempTable)
            // 删除临时目录
            ReadWriteHDFS.del(Array(tmpPath), fs)

            // 4, 遍历hisTableHdfsPath目录，最终表格的根目录，得到时间和设备对应关系set02
            val setOfFinalTable : util.Set[String] = new util.HashSet[String]()
            ReadWriteHDFS.getPartitions(new Path(hisTableHdfsPath), fs, setOfFinalTable)

            // 5，对比set01 和set02,根据set01 中存在，set02 中不存在的数据，在最终的hive 表格中创建元数据
            val setOfTempTableIt = setOfTempTable.iterator()
            val setOfFinalTableIt = setOfFinalTable.iterator()
            while (setOfTempTableIt.hasNext) {
                val dateOfIpcId = setOfTempTableIt.next()
                val date = dateOfIpcId.split("/")(0).split("=")(1)
                val ipcId = dateOfIpcId.split("/")(1).split("=")(1)
                if (!setOfFinalTable.contains(dateOfIpcId)) {
                    sql("set hive.exec.dynamic.partition=true;")
                    sql("set hive.exec.dynamic.partition.mode=nonstrict;")
                    sql("alter table " + tableName + " add partition(date='" + date + "',ipcid='" + ipcId + "')")
                }
            }
        } else {
            personDF = sql("select * from " + tableName + " where date='" +dateString +"'")
            personDF.persist()
            if (personDF.count() == 0) {
                LOG.info("there are parquet files, but no data in parquet files, just to delete the files.")
                // 删除最终表格中的文件
                ReadWriteHDFS.del(pathArr, fs)
                System.exit(2)
            }
            personDF.printSchema()
        }

        // 6, 根据加载的数据，进行分区，并且把数据存到Hive 的表格中,Hive 表格所处的根目录中
        //personDF.coalesce(1).repartition(takePartition(tmpTableHdfsPath, fs))
        personDF.coalesce(1)
            .write.mode(SaveMode.Append)
            .partitionBy("date", "ipcid")
            .parquet(hisTableHdfsPath)

        // 7,删除原来的文件
        if (dateString == null || dateString.equals("")) {
            // 删除临时表格上的文件
            ReadWriteHDFS.delV2(pathArr, fs)
        } else {
            // 删除最终表格上的文件
            ReadWriteHDFS.del(pathArr, fs)
        }

        // 8, Reflesh spark store crash table data
        if (dateString == null || "".equals(dateString)) {
            sql("REFRESH TABLE " + tmpTableHdfsPath.substring(tmpTableHdfsPath.lastIndexOf("/") + 1))
        }
        sql("REFRESH TABLE " + tableName)

        sparkSession.close()
    }

    /** 根据输入目录计算目录大小，并以128*2M大小计算partition */
    def takePartition(src : String, fs : FileSystem) : Int = {
        val cos : ContentSummary = fs.getContentSummary(new Path(src))
        val sizeM : Long = cos.getLength/1024/1024
        val parNum : Int = sizeM/256 match {
            case 0 => 1
            case _ => (sizeM/256).toInt
        }
        parNum
    }
}


/**
  * 保持SparkSession 是单例模式的
  */
object SparkSessionSingleton {
    @transient private var instance: SparkSession = _
    val warehouseLocation = new File("spark-warehouse").getAbsolutePath
    def getInstance(): SparkSession = {
        if (instance == null) {
            instance = SparkSession.builder()
                .appName("combine-parquest-demo")
                .config("spark.sql.parquet.compression.codec", "snappy")
                .config("spark.sql.warehouse.dir", warehouseLocation)
                .enableHiveSupport()
                .getOrCreate()
        }
        instance
    }
}

/**
  * 有关HDFS 的操作
  */
object ReadWriteHDFS {

    /**
      * 删除文件，ture 表示迭代删除目录或者文件
      * @param paths  文件列表
      * @param fs  hdfs文件系统实例
      */
    def del(paths : Array[String], fs : FileSystem): Unit = {
        for (f <- paths) {
            fs.delete(new Path(f), true)
        }
    }

    /**
      * 删除文件，ture 表示迭代删除目录或者文件
      * @param paths  文件列表
      * @param fs  hdfs文件系统实例
      */
    def delV2(paths : Array[String], fs : FileSystem): Unit = {
        for (f <- paths) {
            fs.delete(new Path(f.substring(0, f.lastIndexOf("/"))), true)
        }
    }

    /**
      * 循环遍历根目录path下的parquet 文件,
      * @param path 根目录path
      * @param fs  hdfs 文件系统对象
      * @param files  最终保存的文件
      */
    def getParquetFiles(path : Path, fs : FileSystem, files : util.ArrayList[String]) {
        if (fs != null && path != null){
            val fileStatusArr = fs.listStatus(path)
            for (fileStatus <- fileStatusArr) {
                if (fileStatus.isDirectory()) {
                    getParquetFiles(fileStatus.getPath, fs, files)
                } else if (fileStatus.isFile && fileStatus.getPath.toString.endsWith(".parquet") &&
                    !fileStatus.getPath.toString.contains("_temporary/")){
                    files.add(fileStatus.getPath.toString)
                }
            }
        }
    }

    /**
      * 根据时间循环遍历根目录path下的parquet 文件,
      * @param dateString 时间
      * @param path 根目录path
      * @param fs  hdfs 文件系统对象
      * @param files  最终保存的文件
      */
    def getParquetFiles(dateString : String, path : Path, fs : FileSystem, files : util.ArrayList[String]) {
        if (fs != null && path != null){
            val fileStatusArr = fs.listStatus(path)
            for (fileStatus <- fileStatusArr) {
                val finalPathString = fileStatus.getPath.toString
                if (fileStatus.isDirectory()) {
                    getParquetFiles(dateString, fileStatus.getPath, fs, files)
                } else if (fileStatus.isFile && finalPathString.endsWith(".parquet")
                    && finalPathString.contains(dateString) && !fileStatus.getPath.toString.contains("_temporary/")){
                    files.add(finalPathString)
                }
            }
        }
    }

    /**
      * 取得时间和设备的对应的分区
      * @param src 根目录
      * @param fs hdfs 文件系统对象
      * @param partitions 用于保存时间和ipcid设备编号的对应关系
      */
    def getPartitions(src : Path, fs : FileSystem, partitions : util.Set[String]) {
        if (fs != null && src != null){
            val fileStatusArr = fs.listStatus(src)
            for (fileStatus <- fileStatusArr) {
                if (fileStatus.isDirectory()) {
                    val path = fileStatus.getPath.toString
                    if (path.contains("ipcid"))
                        partitions.add(path.substring(path.indexOf("date=")))
                    else
                        getPartitions(fileStatus.getPath, fs, partitions)
                } else {

                }
            }
        }
    }
}


