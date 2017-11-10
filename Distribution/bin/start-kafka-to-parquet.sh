#!/bin/bash
############################################################################
##Copyright:    HZGOSUN Tech. Co, BigData
##Filename:     start-kafka-to-parquet.sh
##Description:  to start kafkaToParquet
##Version:      1.5.0
##Author:       chenke
##Created:      2017-11-09
############################################################################
#!/bin/bash
#set -x  ## 用于调试用，不用的时候可以注释掉
cd `dirname $0`
## bin目录
BIN_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
## 配置文件目录
CONF_DIR=${DEPLOY_DIR}/conf
## Jar 包目录
LIB_DIR=${DEPLOY_DIR}/lib
## log 日记目录
LOG_DIR=${DEPLOY_DIR}/logs
## log 日记文件
LOG_FILE=${LOG_DIR}/kafkaToParquet.log
## bigdata cluster path
BIGDATA_CLUSTER_PATH=/opt/hzgc/bigdata
## module version
MODULE_VERSION=1.5.0
## spark-streaming-kafka version
SPARK_STREAMING_KAFKA=2.11-1.6.2
## kafka clients version
KAFKA_CLIENTS_VERSION=0.8.2.1
## kafka version
KAFKA_VERSION=2.11-${KAFKA_CLIENTS_VERSION}
## etc profile
ETC_PROFILE=/etc/profile
## bigdata_env
BIGDATA_ENV=${BIGDATA_CLUSTER_PATH}/start_bigdata_service/temporary_environment_variable.sh
## spark bin dir
SPARK_HOME=${BIGDATA_CLUSTER_PATH}/Spark/spark/bin
## spark master
SPARK_MASTER_PARAM=yarn-client
## spark num executors
SPARK_EXECUTORS_NUM=3




if [ ! -d ${LOG_DIR} ];then
    mkdir ${LOG_DIR}
fi

#判断是否存在客户端
if [ ! -d ${BIGDATA_CLUSTER_PATH} ];then
    echo "${BIGDATA_CLUSTER_PATH} does not exit.please check the client exists! "
    exit 1
fi

#判断是否存在配置文件
if [ ! -e ${CONF_DIR}/es-config.properties ];then
   echo "${CONF_DIR}/es-config.properties does not exit!"
   exit 1
fi
if [ ! -e ${CONF_DIR}/sparkJob.properties ];then
   echo "${CONF_DIR}/sparkJob.properties does not exit!"
   exit 1
fi

#更新客户端Spark/spark/conf 下运行任务所需要的配置文件
rm -rf ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf/es-config.properties
rm -rf ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf/sparkJob.properties
cp ${CONF_DIR}/es-config.properties /opt/hzgc/bigdata/Spark/spark/conf
cp ${CONF_DIR}/sparkJob.properties  /opt/hzgc/bigdata/Spark/spark/conf

#判断是否存在jar
if [ ! -e ${LIB_DIR}/spark-streaming-kafka_${SPARK_STREAMING_KAFKA}.jar ];then
   echo "${LIB_DIR}/spark-streaming-kafka_${SPARK_STREAMING_KAFKA}.jar does not exit!"
   exit 1
fi
if [ ! -e ${LIB_DIR}/jni-${MODULE_VERSION}.jar ];then
   echo "${LIB_DIR}/jni-${MODULE_VERSION}.jar does not exit!"
   exit 1
fi
if [ ! -e ${LIB_DIR}/kafka_${KAFKA_VERSION}.jar ];then
   echo "${LIB_DIR}/kafka_${KAFKA_VERSION}.jar does not exit!"
   exit 1
fi
if [ ! -e ${LIB_DIR}/kafka-clients-${KAFKA_CLIENTS_VERSION}.jar ];then
   echo "${LIB_DIR}/kafka-clients-${KAFKA_CLIENTS_VERSION}.jar does not exit!"
   exit 1
fi
if [ ! -e ${LIB_DIR}/elasticsearch-${MODULE_VERSION}.jar ];then
   echo "${LIB_DIR}/elasticsearch-${MODULE_VERSION}.jar does not exit!"
   exit 1
fi
if [ ! -e ${LIB_DIR}/ftp-${MODULE_VERSION}.jar ];then
   echo "${LIB_DIR}/ftp-${MODULE_VERSION}.jar does not exit!"
   exit 1
fi
if [ ! -e ${LIB_DIR}/util-${MODULE_VERSION}.jar ];then
   echo "${LIB_DIR}/util-${MODULE_VERSION}.jar does not exit!"
   exit 1
fi
if [ ! -e ${LIB_DIR}/bigdata-api-${MODULE_VERSION}.jar ];then
   echo "${LIB_DIR}/bigdata-api-${MODULE_VERSION}.jar does not exit!"
   exit 1
fi
if [ ! -e ${LIB_DIR}/hbase-${MODULE_VERSION}.jar ];then
   echo "${LIB_DIR}/hbase-${MODULE_VERSION}.jar does not exit!"
   exit 1
fi

## 存放数据至parquet任务
source ${ETC_PROFILE}
source ${BIGDATA_ENV}
nohup ${SPARK_HOME}/spark-submit \
--master ${SPARK_MASTER_PARAM} \
--num-executors 3 \
--class com.hzgc.cluster.consumer.kafkaToParquet \
--jars ${LIB_DIR}/spark-streaming-kafka_${SPARK_STREAMING_KAFKA}.jar,\
${LIB_DIR}/jni-${MODULE_VERSION}.jar,\
${LIB_DIR}/kafka_${KAFKA_VERSION}.jar,\
${LIB_DIR}/kafka-clients-${KAFKA_CLIENTS_VERSION}.jar,\
${LIB_DIR}/elasticsearch-${MODULE_VERSION}.jar,\
${LIB_DIR}/ftp-${MODULE_VERSION}.jar,\
${LIB_DIR}/util-${MODULE_VERSION}.jar,\
${LIB_DIR}/bigdata-api-${MODULE_VERSION}.jar,\
${LIB_DIR}/hbase-${MODULE_VERSION}.jar \
--files ${CONF_DIR}/es-config.properties,\
${CONF_DIR}/sparkJob.properties \
${LIB_DIR}/streaming-${MODULE_VERSION}.jar > ${LOG_FILE} 2>&1 &