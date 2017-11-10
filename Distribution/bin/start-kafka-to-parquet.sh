#!/bin/bash
############################################################################
##Copyright:    HZGOSUN Tech. Co, BigData
##Filename:     start-kafka-to-parquet.sh
##Description:  to start kafkaToParquet
##Version:      1.0
##Author:       chenke
##Created:      2017-11-09
############################################################################
#!/bin/bash
#set -x  ## 用于调试用，不用的时候可以注释掉
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf   ### 配置文件目录
LIB_DIR=$DEPLOY_DIR/lib     ## Jar 包目录
LOG_DIR=${DEPLOY_DIR}/logs                         ## log 日记目录
LOG_FILE=${LOG_DIR}/kafkaToParquet.log             ## log 日记文件

if [ ! -d $lLOG_DIR ];then
    mkdir $LOG_DIR
fi

#判断是否存在客户端
if [ ! -d /opt/hzgc ];then
    echo "$/opt/hzgc does not exit.please check the client exists! "
    exit 1
fi

#判断是否存在配置文件
if [ ! -e $CONF_DIR/es-config.properties ];then
   echo "$CONF_DIR/es-config.properties does not exit!"
   exit 1
fi
if [ ! -e $CONF_DIR/sparkJob.properties ];then
   echo "$CONF_DIR/sparkJob.properties does not exit!"
   exit 1
fi

#更新客户端Spark/spark/conf 下运行任务所需要的配置文件
rm -rf /opt/hzgc/bigdata/Spark/spark/conf/es-config.properties
rm -rf /opt/hzgc/bigdata/Spark/spark/conf/sparkJob.properties
cp $CONF_DIR/es-config.properties /opt/hzgc/bigdata/Spark/spark/conf
cp $CONF_DIR/sparkJob.properties  /opt/hzgc/bigdata/Spark/spark/conf

#判断是否存在jar
if [ ! -e $LIB_DIR/spark-streaming-kafka_2.11-1.6.2.jar ];then
   echo "$LIB_DIR/spark-streaming-kafka_2.11-1.6.2.jar does not exit!"
   exit 1
fi
if [ ! -e $LIB_DIR/jni-1.5.0.jar ];then
   echo "$LIB_DIR/jni-1.5.0.jar does not exit!"
   exit 1
fi
if [ ! -e $LIB_DIR/kafka_2.11-0.8.2.1.jar ];then
   echo "$LIB_DIR/kafka_2.11-0.8.2.1.jar does not exit!"
   exit 1
fi
if [ ! -e $LIB_DIR/kafka-clients-0.8.2.1.jar ];then
   echo "$LIB_DIR/kafka-clients-0.8.2.1.jar does not exit!"
   exit 1
fi
if [ ! -e $LIB_DIR/elasticsearch-1.0.jar ];then
   echo "$LIB_DIR/elasticsearch-1.0.jar does not exit!"
   exit 1
fi
if [ ! -e $LIB_DIR/ftp-1.5.0.jar ];then
   echo "$LIB_DIR/ftp-1.5.0.jar does not exit!"
   exit 1
fi
if [ ! -e $LIB_DIR/util-1.5.0.jar ];then
   echo "$LIB_DIR/util-1.5.0.jar does not exit!"
   exit 1
fi
if [ ! -e $LIB_DIR/bigdata-api-1.5.0.jar ];then
   echo "$LIB_DIR/bigdata-api-1.5.0.jar does not exit!"
   exit 1
fi
if [ ! -e $LIB_DIR/hbase-1.5.0.jar ];then
   echo "$LIB_DIR/hbase-1.5.0.jar does not exit!"
   exit 1
fi

## 存放数据至parquet任务
source /etc/profile
source /opt/hzgc/bigdata/start_bigdata_service/temporary_environment_variable.sh
nohup spark-submit \
--master yarn-client \
--num-executors 3 \
--class com.hzgc.cluster.consumer.kafkaToParquet \
--jars $LIB_DIR/spark-streaming-kafka_2.11-1.6.2.jar,\
$LIB_DIR/jni-1.5.0.jar,\
$LIB_DIR/kafka_2.11-0.8.2.1.jar,\
$LIB_DIR/kafka-clients-0.8.2.1.jar,\
$LIB_DIR/elasticsearch-1.0.jar,\
$LIB_DIR/ftp-1.5.0.jar,\
$LIB_DIR/util-1.5.0.jar,\
$LIB_DIR/bigdata-api-1.5.0.jar,\
$LIB_DIR/hbase-1.5.0.jar \
--files $CONF_DIR/es-config.properties,\
$CONF_DIR/sparkJob.properties \
$LIB_DIR/streaming-1.5.0.jar > $LOG_FILE 2>&1 &
