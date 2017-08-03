### 注意事项
```
1，建表前，需要把HBase2Es协处理器的jar 包上传到HDFS 文件系统中。
2，需要在本地进行测试过，然后再加到FI 集群中。
```
java -Djava.ext.dirs=./lib/ -cp hbase2es-1.0.jar com.hzgc.hbase2es.CreateStaticRepoTable
java -Djava.ext.dirs=./lib/ -cp hbase2es-1.0.jar com.hzgc.hbase2es.CreateStaticRepoTable \
conf/es_cluster_config.properties  conf/coprocessor.properties