package com.hzgc.cluster.smallfile


object MapAndMapPartitionDemo {
    def main(args: Array[String]): Unit = {
        val sparkSession  = SparkSessionSingleton.getInstance;
        val sc = sparkSession.sparkContext
        val a = sc.parallelize(1 to 9, 3)
        def mapDoubleFunc(a : Int) : (Int,Int) = {
            (a,a*2)
        }
        val mapResult = a.map(a => (a, 2))

        println(mapResult.collect().mkString)
//        val a = sc.parallelize(1 to 9, 3)
//        def doubleFunc(iter: Iterator[Int]) : Iterator[(Int,Int)] = {
//            var res = List[(Int,Int)]()
//            while (iter.hasNext)
//            {
//                val cur = iter.next;
//                res .::= (cur,cur*2)
//            }
//            res.iterator
//        }
//        val result = a.mapPartitions(doubleFunc)
//        println(result.collect().mkString)
    }
}
