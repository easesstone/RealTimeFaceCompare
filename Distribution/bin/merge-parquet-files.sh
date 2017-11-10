#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    merge-parquet-files.sh
## Description: 合并小文件
## Version:     1.0
## Author:      lidiliang
## Created:     2017-11-06
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf    ### 项目根目录
LIB_DIR=$DEPLOY_DIR/lib        ## Jar 包目录
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`   ## jar 包位置以及第三方依赖jar包，绝对路径
LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录
LOG_FILE=${LOG_DIR}/merge-parquet-files.log        ##  log 日记文件

mkdir -p ${LOG_DIR}

SPARK_STREAMING_KAFKA=1.6.2
RELEASE_VERSION=1.5.0
KAFKA_VERSION=0.8.2.1
SCALA_VERSION=2.11



spark-streaming-kafka

source /opt/hzgc/env_bigdata.sh

if [[ ($# -ne 4)  &&  ($# -ne 5) ]];then
    echo "Usage: sh merget-parquest-files.sh <hdfsClusterName> <tmpTableHdfsPath> <hisTableHdfsPath> <tableName>"
    echo "<hdfsClusterName> 例如：hdfs://hacluster或者hdfs://hzgc "
    echo "<tmpTableHdfsPath> 临时表的根目录，需要是绝对路径"
    echo "<hisTableHdfsPath> 合并后的parquet文件，即总的或者历史文件的根目录，需要是绝对路径"
    echo "<tableName> 表格名字，最终保存的动态信息库的表格名字"
    echo "<dateString> 时间，根据某一天的时间进行合并parquet 文件。"
    exit 1
fi

hdfsClusterName=$1
tmpTableHdfsPath=$2
hisTableHdfsPath=$3
tableName=$4
dateString=""
if [ "$5" != "" ];then
    dateString=$5
fi

#####################################################################
# 函数名: start_consumer
# 描述: 把consumer 消费组启动起来
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function merge_parquet()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
    spark-submit --class com.hzgc.cluster.smallfile.MergeParquetFile \
    --master local[*] \
    --driver-memory 4g \
    --jars ${LIB_DIR}/spark-streaming-kafka_${SCALA_VERSION}-${SPARK_STREAMING_KAFKA}.jar,\
    ${LIB_DIR}/jni-${RELEASE_VERSION}.jar,\
    ${LIB_DIR}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.jar,\
    ${LIB_DIR}/kafka-clients-${KAFKA_VERSION}.jar,\
    ${LIB_DIR}/ftp-${RELEASE_VERSION}.jar,\
    ${LIB_DIR}/util-${RELEASE_VERSION}.jar,\
    ${LIB_DIR}/bigdata-api-${RELEASE_VERSION}.jar,\
    ${LIB_DIR}/hbase-${RELEASE_VERSION}.jar \
    ${LIB_DIR}/streaming-${RELEASE_VERSION}.jar \
    ${hdfsClusterName} ${tmpTableHdfsPath} \
    ${hisTableHdfsPath} ${tableName} ${dateString}>> ${LOG_FILE} 2>&1 &
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
    merge_parquet
}


## 脚本主要业务入口
# 主程序入口
echo "" | tee  -a  $LOG_FILE
echo "*******************************************************************"  | tee  -a  $LOG_FILE
main
echo $?


set -x
