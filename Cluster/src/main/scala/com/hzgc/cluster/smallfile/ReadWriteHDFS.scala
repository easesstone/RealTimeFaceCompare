package com.hzgc.cluster.smallfile

import java.util

import org.apache.hadoop.fs.{ContentSummary, FileSystem, Path}
import org.apache.log4j.Logger


/**
  * 有关HDFS 的操作
  */
object ReadWriteHDFS {
    private val LOG : Logger = Logger.getLogger(ReadWriteHDFS.getClass)

    /**
      * 删除文件，ture 表示迭代删除目录或者文件
      *
      * @param paths 文件列表
      * @param fs    hdfs文件系统实例
      */
    def del(paths: Array[String], fs: FileSystem): Unit = {
        for (f <- paths) {
            fs.delete(new Path(f), true)
        }
    }

    def getAllFilesTotalSize(parquetFiles: util.ArrayList[String], fs : FileSystem): Double = {
        var sizeM : Double = 0.0
        var i : Int = 0
        var totalSize : Long = 0
        while (i == 0) {
            if (fs.exists(new Path(parquetFiles.get(i)))) {
                totalSize = totalSize + fs.getContentSummary(new Path(parquetFiles.get(i))).getLength
            }
            i = i + 1
        }
        sizeM = totalSize * 1.0 / 1024.0 /1024.0
        sizeM
    }

    /**
      * 循环遍历根目录path下的parquet 文件,
      * @param path  根目录path
      * @param fs    hdfs 文件系统对象
      * @param files 最终保存的文件
      */
    def getParquetFiles(path: Path, fs: FileSystem, files: java.util.ArrayList[String]) {
        if (fs != null && path != null) {
            if (fs.exists(path)) {
                val fileStatusArr = fs.listStatus(path)
                for (fileStatus <- fileStatusArr) {
                    if (fileStatus.isDirectory) {
                        getParquetFiles(fileStatus.getPath, fs, files)
                    } else if (fileStatus.isFile && fileStatus.getPath.toString.endsWith(".parquet") &&
                        !fileStatus.getPath.toString.contains("_temporary/")) {
                        files.add(fileStatus.getPath.toString)
                    }
                }
            }
        }
    }

    /**
      * 根据时间循环遍历根目录path下的parquet 文件,
      * maxSingleSize 到 minSingleSize 之间的文件，过滤掉
      * @param path       根目录path
      * @param fs         hdfs 文件系统对象
      * @param files      最终保存的文件
      */
    def getParquetFilesV2(maxSingleSize : Double, minSingleSize : Double,path: Path,
                          fs: FileSystem, files: java.util.ArrayList[String]) {
        if (fs != null && path != null) {
            if(fs.exists(path)) {
                val fileStatusArr = fs.listStatus(path)
                for (fileStatus <- fileStatusArr) {
                    val finalPathString = fileStatus.getPath.toString
                    if (fileStatus.isDirectory) {
                        getParquetFilesV2(maxSingleSize, minSingleSize, fileStatus.getPath, fs, files)
                    } else if (fileStatus.isFile && finalPathString.endsWith(".parquet")
                        && !fileStatus.getPath.toString.contains("_temporary/")) {
                        val cos : ContentSummary = fs.getContentSummary(new Path(finalPathString))
                        val sizeM : Long = cos.getLength/1024/1024
                        if (sizeM >= minSingleSize && sizeM <= maxSingleSize) {

                        } else {
                            files.add(finalPathString)
                        }
                    }
                }
            } else {
                LOG.info("*************************************************************************************")
                LOG.info("目录: " + path.toString + " 不存在")
                LOG.info("*************************************************************************************")
                System.exit(0)
            }
        }
    }
}

