#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-face-offline-alarm-job.sh
## Description: to start faceOffLineAlarmJob(启动离线告警任务)
## Version:     1.5.0
## Author:      qiaokaifeng
## Created:     2017-11-09
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
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
##  log 日记文件
LOG_FILE=${LOG_DIR}/sparkFaceOfflineAlarmJob.log
## bigdata cluster path
BIGDATA_CLUSTER_PATH=/opt/hzgc/bigdata
## etc profile
ETC_PROFILE=/etc/profile
## bigdata_env
BIGDATA_ENV=/opt/hzgc/env_bigdata.sh
## spark class
SPARK_CLASS_PARAM=com.hzgc.cluster.alarm.FaceOffLineAlarmJob
#---------------------------------------------------------------------#
#                              jar版本控制                            #
#---------------------------------------------------------------------#
## module version
MODULE_VERSION=1.5.0
## elasticsearch module
ELASTICSEARCH_MODULE=1.0
## hbase client or server version
HBASE_VERSION=1.2.6
## ftp core version
FTP_CORE_VERSION=1.1.1
## gson version
GSON_VERSION=2.8.0
## jackson core version
JACKSON_CORE_VERSION=2.8.6
## spark-streaming-kafka version
SPARK_STREAMING_KAFKA=2.11-1.6.2
## kafka clients version
KAFKA_CLIENTS_VERSION=0.8.2.1
## kafka version
KAFKA_VERSION=2.11-${KAFKA_CLIENTS_VERSION}
## rocketmq-client or rocketmq-common or rocketmq-remoting version
ROCKETMQ_VERSION=4.1.0
## fast json version
FASTJSON_VERSION=1.2.29
## metrics_core_version
METRICS_CORE_VERSION=2.2.0


if [ ! -d ${LOG_DIR} ];then
   mkdir ${LOG_DIR}
fi

#判断是否存在大数据集群
if [ ! -d ${BIGDATA_CLUSTER_PATH} ];then
   echo "${BIGDATA_CLUSTER_PATH} does not exit,please go to the node of the existing bigdata cluster !"
   exit 0
fi

#判断是否存在配置文件
if [ ! -e ${CONF_DIR}/es-config.properties ];then
    echo "${CONF_DIR}/es-config.properties does not exit!"
    exit 0
fi
if [ ! -e ${CONF_DIR}/rocketmq.properties ];then
    echo "${CONF_DIR}/rocketmq.properties does not exit!"
    exit 0
fi
if [ ! -e ${CONF_DIR}/sparkJob.properties ];then
    echo "${CONF_DIR}/sparkJob.properties does not exit!"
    exit 0
fi

rm -rf ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf/es-config.properties
rm -rf ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf/rocketmq.properties
rm -rf ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf/sparkJob.properties
rm -rf ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf/ftpAddress.properties
rm -rf ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf/hbase-site.xml
cp ${CONF_DIR}/es-config.properties ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf
cp ${CONF_DIR}/rocketmq.properties  ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf
cp ${CONF_DIR}/sparkJob.properties  ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf
cp ${CONF_DIR}/ftpAddress.properties  ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf
cp ${CONF_DIR}/hbase-site.xml  ${BIGDATA_CLUSTER_PATH}/Spark/spark/conf

## 判断是否存在jar
if [ ! -e ${LIB_DIR}/hbase-client-${HBASE_VERSION}.jar ];then
    echo "${LIB_DIR}/hbase-client-${HBASE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/hbase-common-${HBASE_VERSION}.jar ];then
    echo "$LIB_DIR/hbase-common-${HBASE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/gson-${GSON_VERSION}.jar ];then
    echo "${LIB_DIR}/gson-${GSON_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/jackson-core-${JACKSON_CORE_VERSION}.jar ];then
    echo "${LIB_DIR}/jackson-core-${JACKSON_CORE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/spark-streaming-kafka_${SPARK_STREAMING_KAFKA}.jar ];then
    echo "${LIB_DIR}/spark-streaming-kafka_${SPARK_STREAMING_KAFKA}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/service-${MODULE_VERSION}.jar ];then
    echo "${LIB_DIR}/service-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/hbase-server-${HBASE_VERSION}.jar ];then
    echo "${LIB_DIR}/hbase-server-${HBASE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/hbase-protocol-${HBASE_VERSION}.jar ];then
    echo "${LIB_DIR}/hbase-protocol-${HBASE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/ftpserver-core-${FTP_CORE_VERSION}.jar ];then
    echo "${LIB_DIR}/ftpserver-core-${FTP_CORE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/jni-${MODULE_VERSION}.jar ];then
    echo "${LIB_DIR}/jni-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/kafka_${KAFKA_VERSION}.jar ];then
    echo "${LIB_DIR}/kafka_${KAFKA_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/elasticsearch-${ELASTICSEARCH_MODULE}.jar ];then
    echo "${LIB_DIR}/elasticsearch-${ELASTICSEARCH_MODULE}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/ftp-${MODULE_VERSION}.jar ];then
    echo "${LIB_DIR}/ftp-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e $LIB_DIR/bigdata-api-${MODULE_VERSION}.jar ];then
    echo "$LIB_DIR/bigdata-api-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/rocketmq-client-${ROCKETMQ_VERSION}-incubating.jar ];then
    echo "${LIB_DIR}/rocketmq-client-${ROCKETMQ_VERSION}-incubating.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/rocketmq-common-${ROCKETMQ_VERSION}-incubating.jar ];then
    echo "${LIB_DIR}/rocketmq-common-${ROCKETMQ_VERSION}-incubating.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/rocketmq-remoting-${ROCKETMQ_VERSION}-incubating.jar ];then
    echo "${LIB_DIR}/rocketmq-remoting-${ROCKETMQ_VERSION}-incubating.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/fastjson-${FASTJSON_VERSION}.jar ];then
    echo "${LIB_DIR}/fastjson-${FASTJSON_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/util-${MODULE_VERSION}.jar ];then
    echo "${LIB_DIR}/util-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/kafka-clients-${KAFKA_CLIENTS_VERSION}.jar ];then
    echo "${LIB_DIR}/kafka-clients-${KAFKA_CLIENTS_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/streaming-${MODULE_VERSION}.jar ];then
    echo "${LIB_DIR}/streaming-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${LIB_DIR}/metrics-core-${METRICS_CORE_VERSION}.jar ];then
    echo "${LIB_DIR}/metrics-core-${METRICS_CORE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CONF_DIR}/es-config.properties ];then
    echo "${CONF_DIR}/es-config.properties does not exit!"
    exit 0
fi
if [ ! -e ${CONF_DIR}/sparkJob.properties ];then
    echo "${CONF_DIR}/sparkJob.properties does not exit!"
    exit 0
fi
if [ ! -e ${CONF_DIR}/hbase-site.xml ];then
    echo "${CONF_DIR}/hbase-site.xml does not exit!"
    exit 0
fi
if [ ! -e ${CONF_DIR}/ftpAddress.properties ];then
    echo "${CONF_DIR}/ftpAddress.properties does not exit!"
    exit 0
fi
if [ ! -e ${CONF_DIR}/rocketmq.properties ];then
    echo "${CONF_DIR}/rocketmq.properties does not exit!"
    exit 0
fi

############### 离线告警任务#################
source ${ETC_PROFILE}
source ${BIGDATA_ENV}
nohup spark-submit \
--master yarn \
--deploy-mode cluster \
--executor-memory 4g \
--executor-cores 2 \
--class ${SPARK_CLASS_PARAM} \
--jars ${LIB_DIR}/gson-${GSON_VERSION}.jar,\
${LIB_DIR}/jackson-core-${JACKSON_CORE_VERSION}.jar,\
${LIB_DIR}/spark-streaming-kafka_${SPARK_STREAMING_KAFKA}.jar,\
${LIB_DIR}/service-${MODULE_VERSION}.jar,\
${LIB_DIR}/hbase-server-${HBASE_VERSION}.jar,\
${LIB_DIR}/hbase-client-${HBASE_VERSION}.jar,\
${LIB_DIR}/hbase-common-${HBASE_VERSION}.jar,\
${LIB_DIR}/hbase-protocol-${HBASE_VERSION}.jar,\
${LIB_DIR}/jni-${MODULE_VERSION}.jar,\
${LIB_DIR}/kafka_${KAFKA_VERSION}.jar,\
${LIB_DIR}/elasticsearch-${ELASTICSEARCH_MODULE}.jar,\
${LIB_DIR}/ftp-${MODULE_VERSION}.jar,\
$LIB_DIR/bigdata-api-${MODULE_VERSION}.jar,\
${LIB_DIR}/ftpserver-core-${FTP_CORE_VERSION}.jar,\
${LIB_DIR}/rocketmq-client-${ROCKETMQ_VERSION}-incubating.jar,\
${LIB_DIR}/rocketmq-common-${ROCKETMQ_VERSION}-incubating.jar,\
${LIB_DIR}/rocketmq-remoting-${ROCKETMQ_VERSION}-incubating.jar,\
${LIB_DIR}/fastjson-${FASTJSON_VERSION}.jar,\
${LIB_DIR}/util-${MODULE_VERSION}.jar,\
${LIB_DIR}/kafka-clients-${KAFKA_CLIENTS_VERSION}.jar,\
${LIB_DIR}/metrics-core-${METRICS_CORE_VERSION}.jar \
--files ${CONF_DIR}/es-config.properties,\
${CONF_DIR}/hbase-site.xml,\
${CONF_DIR}/ftpAddress.properties,\
${CONF_DIR}/sparkJob.properties,\
${CONF_DIR}/rocketmq.properties \
${LIB_DIR}/streaming-${MODULE_VERSION}.jar > ${LOG_FILE} 2>&1 &
