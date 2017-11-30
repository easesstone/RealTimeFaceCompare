#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    load-parquet-files.sh
## Description: 加载分区数据到peroson_table
## Version:     1.0
## Author:      qiaokaifeng
## Created:     2017-11-18
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

source /etc/profile
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf    ### 项目根目录
LIB_DIR=$DEPLOY_DIR/lib        ## Jar 包目录
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`   ## jar 包位置以及第三方依赖jar包，绝对路径
LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录
LOG_FILE=${LOG_DIR}/load-parquet-files.log        ##  log 日记文件
SPARK_HOME=/opt/hzgc/bigdata
mkdir -p ${LOG_DIR}

SPARK_STREAMING_KAFKA=1.6.2
RELEASE_VERSION=1.5.0
KAFKA_VERSION=1.0.0
SCALA_VERSION=2.11

hdfsClusterName=$(sed -n '1p' ${CONF_DIR}/load-parquet-files.properties)
hdfsPath=$(sed -n '2p' ${CONF_DIR}/load-parquet-files.properties)
tableName=$(sed -n '3p' ${CONF_DIR}/load-parquet-files.properties)

#####################################################################
# 函数名: start_load_data
# 描述: 加载数据
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function load_parquet()
{
    echo "$LIB_DIR ===================================================== NIMA "
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
${SPARK_HOME}/Spark/spark/bin/spark-submit --class com.hzgc.cluster.loaddata.LoadParquetFile \
    --master local[*] \
    --driver-memory 4g \
    --jars ${LIB_DIR}/spark-streaming-kafka_${SCALA_VERSION}-${SPARK_STREAMING_KAFKA}.jar,\
${LIB_DIR}/jni-${RELEASE_VERSION}.jar,\
${LIB_DIR}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.jar,\
${LIB_DIR}/kafka-clients-${KAFKA_VERSION}.jar,\
${LIB_DIR}/ftp-${RELEASE_VERSION}.jar,\
${LIB_DIR}/util-${RELEASE_VERSION}.jar,\
${LIB_DIR}/bigdata-api-${RELEASE_VERSION}.jar,\
${LIB_DIR}/service-${RELEASE_VERSION}.jar \
${LIB_DIR}/cluster-${RELEASE_VERSION}.jar ${hdfsClusterName} ${hdfsPath} ${tableName} >> ${LOG_FILE} 2>&1 &
}

#####################################################################
# 函数名: main
# 描述: 脚本主要业务入口
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function main()
{
    echo ""  | tee  -a  $LOG_FILE
    echo ""  | tee  -a  $LOG_FILE
    echo "==================================================="  | tee -a $LOG_FILE
    echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE
    load_parquet
}


## 脚本主要业务入口
# 主程序入口
echo "" | tee  -a  $LOG_FILE
echo "*******************************************************************"  | tee  -a  $LOG_FILE
main
echo $?


set -x
