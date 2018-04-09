#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-resident-clustering-job.sh
## Description: to start resident clustering job(启动任务)
## Author:      pengcong
## Created:     2018-03-26
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
CLUSTER_RESIDENT_DATA_DIR=${CLUSTER_DIR}/data
LOG_FILE=${CLUSTER_LOG_DIR}/sparkResidentClusteringJob.log
######## common目录 ########
COMMON_CONF_DIR=${DEPLOY_DIR}/common/conf
COMMON_LIB_DIR=${DEPLOY_DIR}/common/lib
######## service目录 ########
SERVICE_CONF_DIR=${DEPLOY_DIR}/service/conf
SERVICE_LIB_DIR=${DEPLOY_DIR}/service/lib
## bigdata_env
BIGDATA_ENV=/opt/hzgc/env_bigdata.sh
## spark class
SPARK_CLASS_PARAM=com.hzgc.cluster.clustering.ResidentClustering
## bigdata cluster path
BIGDATA_CLUSTER_PATH=/opt/hzgc/bigdata
# spark conf path
SPARK_CONF_PATH=${BIGDATA_CLUSTER_PATH}/Spark/spark/conf
#---------------------------------------------------------------------#
#                              jar版本控制                            #
#---------------------------------------------------------------------#
## module version(模块)
BIGDATA_API_VERSION=`ls ${COMMON_LIB_DIR}| grep ^bigdata-api-[0-9].[0-9].[0-9].jar$`
CLUSTER_VERSION=`ls ${COMMON_LIB_DIR}| grep ^cluster-[0-9].[0-9].[0-9].jar$`
FTP_VERSION=`ls ${COMMON_LIB_DIR}| grep ^ftp-[0-9].[0-9].[0-9].jar$`
JNI_VERSION=`ls ${COMMON_LIB_DIR}| grep ^jni-[0-9].[0-9].[0-9].jar$`
SERVICE_VERSION=`ls ${COMMON_LIB_DIR}| grep ^service-[0-9].[0-9].[0-9].jar$`
UTIL_VERSION=`ls ${COMMON_LIB_DIR}| grep ^util-[0-9].[0-9].[0-9].jar$`

## quote version(引用)
GSON_VERSION=gson-2.8.0.jar
JACKSON_CORE_VERSION=jackson-core-2.8.6.jar
HBASE_SERVER_VERSION=hbase-server-1.2.6.jar
HBASE_CLIENT_VERSION=hbase-client-1.2.6.jar
HBASE_COMMON_VERSION=hbase-common-1.2.6.jar
HBASE_PROTOCOL_VERSION=hbase-protocol-1.2.6.jar
ELASTICSEARCH_VERSION=elasticsearch-1.0.jar
FASTJSON_VERSION=fastjson-1.2.29.jar
METRICS_CORE_VERSION=metrics-core-2.2.0.jar


############ 创建log目录 ###############

if [ ! -d ${CLUSTER_LOG_DIR} ];then
   mkdir ${CLUSTER_LOG_DIR}
fi
############ 创建聚类结果保存 ###############
if [ ! -d ${CLUSTER_RESIDENT_DATA_DIR} ];then
    mkdir ${CLUSTER_RESIDENT_DATA_DIR}
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
else
    echo "copy 文件 es-config.properties 到 spark/conf......"  | tee  -a  $LOG_FILE
    cp ${SERVICE_CONF_DIR}/es-config.properties ${SPARK_CONF_PATH}
    echo "copy完毕......"  | tee  -a  $LOG_FILE
fi
if [ ! -e ${CLUSTER_CONF_DIR}/hive-site.xml ];then
    echo "${CLUSTER_CONF_DIR}/hive-site.xml does not exit!"
    exit 0
else
    echo "copy 文件 hive-site.xml 到 spark/conf......"  | tee  -a  $LOG_FILE
    cp ${CLUSTER_CONF_DIR}/hive-site.xml ${SPARK_CONF_PATH}
    echo "copy完毕......"  | tee  -a  $LOG_FILE
fi
if [ ! -e ${CLUSTER_CONF_DIR}/sparkJob.properties ];then
    echo "${CLUSTER_CONF_DIR}/sparkJob.properties does not exit!"
    exit 0
else
    echo "copy 文件 sparkJob.properties 到 spark/conf......"  | tee  -a  $LOG_FILE
    cp ${CLUSTER_CONF_DIR}/sparkJob.properties ${SPARK_CONF_PATH}
    echo "copy完毕......"  | tee  -a  $LOG_FILE
fi
if [ ! -e ${SERVICE_CONF_DIR}/hbase-site.xml ];then
    echo "${SERVICE_CONF_DIR}/hbase-site.xml does not exit!"
    exit 0
else
    echo "copy 文件 hbase-site.xml 到 spark/conf......"  | tee  -a  $LOG_FILE
    cp ${SERVICE_CONF_DIR}/hbase-site.xml ${SPARK_CONF_PATH}
    echo "copy完毕......"  | tee  -a  $LOG_FILE
fi

################# 判断是否存在jar ###################
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_CLIENT_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_CLIENT_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_COMMON_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_COMMON_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${GSON_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${GSON_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${JACKSON_CORE_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${JACKSON_CORE_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${SERVICE_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${SERVICE_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_SERVER_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_SERVER_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_PROTOCOL_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_PROTOCOL_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${JNI_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${JNI_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${SERVICE_LIB_DIR}/${ELASTICSEARCH_VERSION} ];then
    echo "${SERVICE_LIB_DIR}/${ELASTICSEARCH_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${BIGDATA_API_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${BIGDATA_API_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${UTIL_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${UTIL_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${CLUSTER_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${CLUSTER_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${METRICS_CORE_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${METRICS_CORE_VERSION} does not exit!"
    exit 0
fi

################## 新增告警任务 ###################
source /etc/profile
source ${BIGDATA_ENV}
nohup spark-submit \
--master local[*] \
--driver-memory 4g \
--class ${SPARK_CLASS_PARAM} \
--jars ${CLUSTER_LIB_DIR}/${GSON_VERSION},\
${CLUSTER_LIB_DIR}/${JACKSON_CORE_VERSION},\
${COMMON_LIB_DIR}/${SERVICE_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_SERVER_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_CLIENT_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_COMMON_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_PROTOCOL_VERSION},\
${COMMON_LIB_DIR}/${JNI_VERSION},\
${SERVICE_LIB_DIR}/${ELASTICSEARCH_VERSION},\
${COMMON_LIB_DIR}/${BIGDATA_API_VERSION},\
${COMMON_LIB_DIR}/${UTIL_VERSION},\
${CLUSTER_LIB_DIR}/${METRICS_CORE_VERSION} \
--files ${SERVICE_CONF_DIR}/es-config.properties,\
${SERVICE_CONF_DIR}/hbase-site.xml,\
${CLUSTER_CONF_DIR}/hive-site.xml,\
${CLUSTER_CONF_DIR}/sparkJob.properties \
${COMMON_LIB_DIR}/${CLUSTER_VERSION} > ${LOG_FILE} 2>&1 &
