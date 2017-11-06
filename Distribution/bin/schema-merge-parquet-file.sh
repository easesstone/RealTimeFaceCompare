#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    schema-merge-parquet-file.sh
## Description: 定时启动合并小文件的脚本
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
LOG_FILE=${LOG_DIR}/schema-merge-parquet-file.log        ##  log 日记文件

mkdir -p ${LOG_DIR}

hdfsClusterName=$(sed -n '1p' ${CONF_DIR}/merget-parquet-files.properties)
tmpTableHdfsPath=$(sed -n '2p' ${CONF_DIR}/merget-parquet-files.properties)
hisTableHdfsPath=$(sed -n '3p' ${CONF_DIR}/merget-parquet-files.properties)
tableName=$(sed -n '4p' ${CONF_DIR}/merget-parquet-files.properties)

echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE
cd ${BIN_DIR}
sh merge-parquet-files.sh ${hdfsClusterName}  ${tmpTableHdfsPath}  ${hisTableHdfsPath}  ${tableName}
echo $?
