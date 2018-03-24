package com.hzgc.cluster.smallfile

import org.apache.hadoop.fs.{ContentSummary, FileSystem, Path}

object SmallFileUtils {
    /**
      * 根据输入文件的大小所有文件大小的总和，求取可以分成多少个文件
      * 每个文件的大小可以在maxSingleSize 和 minSingleSize 之间
      */
    def takePartition(maxSingleSize : Double, minSingleSize : Double, pathArr : Array[String], fs : FileSystem) : Int = {
        var i = 0
        var totalSize : Long = 0
        var cons : ContentSummary = null
        while (i < pathArr.length) {
            cons = fs.getContentSummary(new Path(pathArr(i)))
            totalSize = totalSize + cons.getLength
            i = i + 1
        }
        val sizeM : Double = totalSize/1024/1024
        val dev : Double = sizeM / maxSingleSize
        val devInt = dev.toInt
        val devTail = dev - dev.toInt
        var parNum = 0
        if (maxSingleSize * devTail > minSingleSize) {
            parNum = devInt + 1
        } else {
            parNum = devInt
        }
        if(parNum == 0) {
            parNum =1
        }
        parNum
    }
}
