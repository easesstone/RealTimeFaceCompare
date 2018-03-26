#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    create-static-repos.sh
## Description: 静态库建表：objectinfo、srecord
## Author:      lidiliang
## Created:     2017-08-03
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf    ### 项目根目录
LIB_DIR=$DEPLOY_DIR/lib        ## Jar 包目录
LIB_JARS=`ls $LIB_DIR|grep .jar| grep -v elasticsearch-1.0.jar \
| grep -v avro-ipc-1.7.7-tests.jar | grep -v avro-ipc-1.7.7.jar | grep -v spark-network-common_2.10-1.5.1.jar | \
awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`   ## jar包位置以及第三方依赖jar包，绝对路径
LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录
LOG_FILE=${LOG_DIR}/create-table.log       ##  log 日记文件

cd ..
declare -r BIGDATA_SERVICE_DIR=`pwd`
declare -r COMMMON_DIR=${BIGDATA_SERVICE_DIR}/common
declare -r FTP_DIR=${BIGDATA_SERVICE_DIR}/ftp
declare -r SERVICE=${BIGDATA_SERVICE_DIR}/service
declare -r CLUSTER_DIR=${BIGDATA_SERVICE_DIR}/cluster
COMMON_LIB_DIR=${COMMMON_DIR}/lib

cd -
COMMON_JARS=`ls ${COMMMON_DIR}/lib | grep .jar | awk '{print "'${COMMMON_DIR}/lib'/"$0}'|tr "\n" ":"`
LIB_JARS=${LIB_JARS}${COMMON_JARS}

## bigdata cluster path
BIGDATA_CLUSTER_PATH=/opt/hzgc/bigdata
## Phoenix 客户端合并
PHOENIX_PATH=${BIGDATA_CLUSTER_PATH}/Phoenix/phoenix
## bigdata hadoop path
HADOOP_PATH=${BIGDATA_CLUSTER_PATH}/Hadoop/hadoop
## hdfs udf  path
HDFS_UDF_PATH=/user/phoenix/udf/facecomp
## hdfs udf Absolute path
HDFS_UDF_ABSOLUTE_PATH=hdfs://hzgc/${HDFS_UDF_PATH}/${UDF_VERSION}
## udf jar version
UDF_VERSION=service-2.1.0.jar

source /opt/hzgc/env_bigdata.sh


#####################################################################
# 函数名: create_hbase_table
# 描述: 建立一张小写名字命名的表格objectinfo
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function create_hbase_table()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
    java -classpath $CONF_DIR:$LIB_JARS com.hzgc.service.staticrepo.tablecreate.CreateStaticRepoTable   | tee -a  ${LOG_FILE}
}

#####################################################################
# 函数名: create_hbase_table
# 描述: 建立HBase 静态信息库的表格，OBJECTINFO 和SEARCHRECORDT
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function create_static_repo() {
    ## 创建person_table
    source /opt/hzgc/env_bigdata.sh
    ${PHOENIX_PATH}/bin/psql.py  ${DEPLOY_DIR}/sql/staticrepo.sql

    if [ $? == 0 ];then
            echo "===================================="
            echo "创建objectinfo,searchrecord成功"
            echo "===================================="
    else
            echo "========================================================="
            echo "创建objectinfo,searchrecord失败,请看日志查找失败原因......"
            echo "========================================================="
    fi
}

#####################################################################
# 函数名: create_phoenix_udf
# 描述: 静态信息库phoenix 自定义函数添加
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function create_phoenix_udf() {
    ## 判断hdfs上/user/hive/udf目录是否存在
    ${HADOOP_PATH}/bin/hdfs dfs -test -e ${HDFS_UDF_PATH}
    if [ $? -eq 0 ] ;then
        echo "=================================="
        echo "${HDFS_UDF_PATH}已经存在"
        echo "=================================="
    else
        echo "=================================="
        echo "${HDFS_UDF_PATH}不存在,正在创建"
        echo "=================================="
        ${HADOOP_PATH}/bin/hdfs dfs -mkdir -p ${HDFS_UDF_PATH}
        if [ $? == 0 ];then
        echo "=================================="
        echo "创建${HDFS_UDF_PATH}目录成功......"
        echo "=================================="
    else
        echo "====================================================="
        echo "创建${HDFS_UDF_PATH}目录失败,请检查服务是否启动......"
        echo "====================================================="
        fi
    fi

    ## 上传udf到hdfs指定目录
    ${HADOOP_PATH}/bin/hdfs dfs -test -e ${HDFS_UDF_PATH}/${UDF_VERSION}
    if [ $? -eq 0 ] ;then
        echo "=================================="
        echo "${HDFS_UDF_PATH}/${UDF_VERSION}已经存在"
        echo "=================================="
    else
        echo "=================================="
        echo "${HDFS_UDF_PATH}/${UDF_VERSION}不存在,正在上传"
        echo "=================================="
        ${HADOOP_PATH}/bin/hdfs dfs -put ${COMMON_LIB_DIR}/${UDF_VERSION} ${HDFS_UDF_PATH}
        if [ $? == 0 ];then
            echo "===================================="
            echo "上传udf函数成功......"
            echo "===================================="
        else
            echo "===================================="
            echo "上传udf函数失败,请查找失败原因......"
            echo "===================================="
        fi
    fi

    ## 在hive中添加并注册udf函数
    ${PHOENIX_PATH}/bin/psql.py ${DEPLOY_DIR}/sql/phoenix-udf.sql

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
    create_phoenix_udf
    create_hbase_table
    create_static_repo
}

## 脚本主要业务入口
main



