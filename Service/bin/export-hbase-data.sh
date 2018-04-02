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
LOG_FILE=${LOG_DIR}/export-elasticsearch-data.log       ##  log 日记文件

cd ..
declare -r BIGDATA_SERVICE_DIR=`pwd`
declare -r COMMMON_DIR=${BIGDATA_SERVICE_DIR}/common

cd -
COMMON_JARS=`ls ${COMMMON_DIR}/lib | grep .jar | awk '{print "'${COMMMON_DIR}/lib'/"$0}'|tr "\n" ":"`
LIB_JARS=${LIB_JARS}${COMMON_JARS}

#####################################################################
# 函数名: create_hbase_table
# 描述: 建立一张小写名字命名的表格objectinfo
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function export_elastic_dat()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
    java -classpath $CONF_DIR:$LIB_JARS com.hzgc.service.staticrepo.HBaseDataExport   | tee -a  ${LOG_FILE}
}

export_elastic_dat