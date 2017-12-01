#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-face-add-alarm-job.sh
## Description: to start faceAddAlarmJob(启动新增告警任务)
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
CLUSTER_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
######## cluster目录 ########
CLUSTER_CONF_DIR=${CLUSTER_DIR}/conf
CLUSTER_LIB_DIR=${CLUSTER_DIR}/lib
CLUSTER_LOG_DIR=${CLUSTER_DIR}/logs
LOG_FILE=${CLUSTER_LOG_DIR}/sparkFaceAddAlarmJob.log
######## common目录 ########
COMMON_CONF_DIR=${DEPLOY_DIR}/common/conf
COMMON_LIB_DIR=${DEPLOY_DIR}/common/lib
######## ftp目录 #########
FTP_CONF_DIR=${DEPLOY_DIR}/ftp/conf
FTP_LIB_DIR=${DEPLOY_DIR}/ftp/lib
######## service目录 ########
SERVICE_CONF_DIR=${DEPLOY_DIR}/service/conf
SERVICE_LIB_DIR=${DEPLOY_DIR}/service/lib
## bigdata_env
BIGDATA_ENV=/opt/hzgc/env_bigdata.sh
## spark class
SPARK_CLASS_PARAM=com.hzgc.cluster.alarm.FaceAddAlarmJob
## bigdata cluster path
BIGDATA_CLUSTER_PATH=/opt/hzgc/bigdata

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
KAFKA_CLIENTS_VERSION=1.0.0
## kafka version
KAFKA_VERSION=2.11-${KAFKA_CLIENTS_VERSION}
## rocketmq-client or rocketmq-common or rocketmq-remoting version
ROCKETMQ_VERSION=4.1.0
## fast json version
FASTJSON_VERSION=1.2.29
## metrics_core_version
METRICS_CORE_VERSION=2.2.0


if [ ! -d ${CLUSTER_LOG_DIR} ];then
   mkdir ${CLUSTER_LOG_DIR}
fi

############ 判断是否存在大数据集群 ###################
if [ ! -d ${BIGDATA_CLUSTER_PATH} ];then
   echo "${BIGDATA_CLUSTER_PATH} does not exit,please go to the node of the existing bigdata cluster !"
   exit 0
fi

############### 判断是否存在配置文件 ##################
if [ ! -e ${SERVICE_CONF_DIR}/es-config.properties ];then
    echo "${SERVICE_CONF_DIR}/es-config.properties does not exit!"
    exit 0
fi
if [ ! -e ${FTP_CONF_DIR}/rocketmq.properties ];then
    echo "${FTP_CONF_DIR}/rocketmq.properties does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_CONF_DIR}/sparkJob.properties ];then
    echo "${CLUSTER_CONF_DIR}/sparkJob.properties does not exit!"
    exit 0
fi
if [ ! -e ${SERVICE_CONF_DIR}/hbase-site.xml ];then
    echo "${SERVICE_CONF_DIR}/hbase-site.xml does not exit!"
    exit 0
fi
if [ ! -e ${FTP_CONF_DIR}/ftpAddress.properties ];then
    echo "${FTP_CONF_DIR}/ftpAddress.properties does not exit!"
    exit 0
fi

################# 判断是否存在jar ###################
if [ ! -e ${CLUSTER_LIB_DIR}/hbase-client-${HBASE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/hbase-client-${HBASE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/hbase-common-${HBASE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/hbase-common-${HBASE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/gson-${GSON_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/gson-${GSON_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/jackson-core-${JACKSON_CORE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/jackson-core-${JACKSON_CORE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/spark-streaming-kafka_${SPARK_STREAMING_KAFKA}.jar ];then
    echo "${CLUSTER_LIB_DIR}/spark-streaming-kafka_${SPARK_STREAMING_KAFKA}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/service-${MODULE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/service-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/hbase-server-${HBASE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/hbase-server-${HBASE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/hbase-protocol-${HBASE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/hbase-protocol-${HBASE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/ftpserver-core-${FTP_CORE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/ftpserver-core-${FTP_CORE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/jni-${MODULE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/jni-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/kafka_${KAFKA_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/kafka_${KAFKA_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/elasticsearch-${ELASTICSEARCH_MODULE}.jar ];then
    echo "${CLUSTER_LIB_DIR}/elasticsearch-${ELASTICSEARCH_MODULE}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/ftp-${MODULE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/ftp-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/bigdata-api-${MODULE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/bigdata-api-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/rocketmq-client-${ROCKETMQ_VERSION}-incubating.jar ];then
    echo "${CLUSTER_LIB_DIR}/rocketmq-client-${ROCKETMQ_VERSION}-incubating.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/rocketmq-common-${ROCKETMQ_VERSION}-incubating.jar ];then
    echo "${CLUSTER_LIB_DIR}/rocketmq-common-${ROCKETMQ_VERSION}-incubating.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/rocketmq-remoting-${ROCKETMQ_VERSION}-incubating.jar ];then
    echo "${CLUSTER_LIB_DIR}/rocketmq-remoting-${ROCKETMQ_VERSION}-incubating.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/fastjson-${FASTJSON_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/fastjson-${FASTJSON_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/util-${MODULE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/util-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/kafka-clients-${KAFKA_CLIENTS_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/kafka-clients-${KAFKA_CLIENTS_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/cluster-${MODULE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/cluster-${MODULE_VERSION}.jar does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/metrics-core-${METRICS_CORE_VERSION}.jar ];then
    echo "${CLUSTER_LIB_DIR}/metrics-core-${METRICS_CORE_VERSION}.jar does not exit!"
    exit 0
fi

################## 新增告警任务 ###################
source /etc/profile
source ${BIGDATA_ENV}
nohup spark-submit \
--master yarn \
--deploy-mode cluster \
--executor-memory 4g \
--executor-cores 2 \
--num-executors 4 \
--class ${SPARK_CLASS_PARAM} \
--jars ${CLUSTER_LIB_DIR}/gson-${GSON_VERSION}.jar,\
${CLUSTER_LIB_DIR}/jackson-core-${JACKSON_CORE_VERSION}.jar,\
${CLUSTER_LIB_DIR}/spark-streaming-kafka_${SPARK_STREAMING_KAFKA}.jar,\
${CLUSTER_LIB_DIR}/service-${MODULE_VERSION}.jar,\
${CLUSTER_LIB_DIR}/hbase-server-${HBASE_VERSION}.jar,\
${CLUSTER_LIB_DIR}/hbase-client-${HBASE_VERSION}.jar,\
${CLUSTER_LIB_DIR}/hbase-common-${HBASE_VERSION}.jar,\
${CLUSTER_LIB_DIR}/hbase-protocol-${HBASE_VERSION}.jar,\
${CLUSTER_LIB_DIR}/jni-${MODULE_VERSION}.jar,\
${CLUSTER_LIB_DIR}/kafka_${KAFKA_VERSION}.jar,\
${CLUSTER_LIB_DIR}/elasticsearch-${ELASTICSEARCH_MODULE}.jar,\
${CLUSTER_LIB_DIR}/ftp-${MODULE_VERSION}.jar,\
${CLUSTER_LIB_DIR}/bigdata-api-${MODULE_VERSION}.jar,\
${CLUSTER_LIB_DIR}/ftpserver-core-${FTP_CORE_VERSION}.jar,\
${CLUSTER_LIB_DIR}/rocketmq-client-${ROCKETMQ_VERSION}-incubating.jar,\
${CLUSTER_LIB_DIR}/rocketmq-common-${ROCKETMQ_VERSION}-incubating.jar,\
${CLUSTER_LIB_DIR}/rocketmq-remoting-${ROCKETMQ_VERSION}-incubating.jar,\
${CLUSTER_LIB_DIR}/fastjson-${FASTJSON_VERSION}.jar,\
${CLUSTER_LIB_DIR}/util-${MODULE_VERSION}.jar,\
${CLUSTER_LIB_DIR}/kafka-clients-${KAFKA_CLIENTS_VERSION}.jar,\
${CLUSTER_LIB_DIR}/metrics-core-${METRICS_CORE_VERSION}.jar \
--files ${SERVICE_CONF_DIR}/es-config.properties,\
${SERVICE_CONF_DIR}/hbase-site.xml,\
${FTP_CONF_DIR}/ftpAddress.properties,\
${CLUSTER_CONF_DIR}/sparkJob.properties,\
${FTP_CONF_DIR}/rocketmq.properties \
${CLUSTER_LIB_DIR}/cluster-${MODULE_VERSION}.jar > ${LOG_FILE} 2>&1 &
