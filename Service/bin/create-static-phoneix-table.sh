#!/bin/bash
################################################################################
## Copyright:    HZGOSUN Tech. Co, BigData
## Filename:     create-static-phoneix-table.sh
## Description:  创建动态库表(person_table,mid_table)
## Author:       李第亮
## Created:      2018-03-22
################################################################################

#set -x
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
cd ..
## 项目根目录
REALTIMEFACECOMPARE_DIR=`pwd`
## common 目录
COMMON=${REALTIMEFACECOMPARE_DIR}/common
## common conf 目录
COMMON_CONF=${COMMON}/conf
##  log 日记文件
LOG_FILE=${LOG_DIR}/create-static-phoneix-table.log
## bigdata cluster path
BIGDATA_CLUSTER_PATH=/opt/hzgc/bigdata
## bigdata hive path
SPARK_PATH=${BIGDATA_CLUSTER_PATH}/Phoenix/phoenix-4.13.1-hbase-1.2
## zk IP 地址
ZOOKEEPER_IP=$(grep zookeeper_installnode $COMMON_CONF/project-conf.properties | awk -F ";" '{print $1}')

## 创建person_table
source /opt/hzgc/env_bigdata.sh
${SPARK_PATH}/bin/psql.py  ${CONF_DIR}/staticrepo.sql

if [ $? == 0 ];then
		echo "===================================="
		echo "创建objectinfo,searchrecord成功"
		echo "===================================="
else
		echo "========================================================="
		echo "创建objectinfo,searchrecord失败,请看日志查找失败原因......"
		echo "========================================================="
fi
