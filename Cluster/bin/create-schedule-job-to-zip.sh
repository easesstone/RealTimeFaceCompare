#!/bin/bash
########################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    schema-merge-parquet-file.sh
## Description: 将定时任务生成job文件并打包成zip包
## Author:      chenke
## Created:     2018-03-27
#########################################################################
#set -x ##用于调试使用，不用的时候可以注释掉

#----------------------------------------------------------------------#
#                              定义变量                                #
#----------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`   ###bin目录
cd ..
DEPLOY_DIR=`pwd`  ###项目根目录
CONF_DIR=$DEPLOY_DIR/logs  ###集群log日志目录
LOG_FILE=${LOG_DIR}/create-schedule-job-to-zip.log  ##log日志文件
SCHEMA_FILE="schema-merge-parquet-file.sh"
OFFLINE_FILE="start-face-offline-alarm-job.sh"

cd ${BIN_DIR}  ##进入cluster的bin目录
mkdir -p midTableAndPersonTableNow
if [ ! -f "$SCHEMA_FILE" ]; then
   echo "The schema-merge-parquet-file.sh is not exist!!!"
else
   touch midtable.job     ##创建midtable.job文件
   echo "type=command" >> midtable.job
   echo "cluster_home=/opt/RealTimeFaceCompare/cluster/bin" >> midtable.job
   echo "command=sh \${cluster_home}/schema-merge-parquet-file.sh mid_table" >> midtable.job

   touch person_table_now.job  ##创建person_table_now.job文件
   echo "type=command" >> person_table_now.job
   echo "cluster_home=/opt/RealTimeFaceCompare/cluster/bin" >> person_table_now.job
   echo "command=sh \${cluster_home}/schema-merge-parquet-file.sh person_table now" >> person_table_now.job
   echo "dependencies=midtable" >> person_table_now.job

   touch person_table_before.job  ##创建person_table_before.job文件
   echo "type=command" >> person_table_before.job
   echo "cluster_home=/opt/RealTimeFaceCompare/cluster/bin" >> person_table_before.job
   echo "command=sh \${cluster_home}/schema-merge-parquet-file.sh person_table before" >> person_table_before.job

fi
if [ ! -f "$OFFLINE_FILE" ]; then
   echo "The start-face-offline-alarm-job.sh is not exist!!!"
else
   touch start-face-offline-alarm-job.job  ##创建离线告警的job文件
   echo "type=command" >> start-face-offline-alarm-job.job
   echo "cluster_home=/opt/RealTimeFaceCompare/cluster/bin" >> start-face-offline-alarm-job.job
   echo "command=sh \${cluster_home}/start-face-offline-alarm-job.sh" >> start-face-offline-alarm-job.job
fi

mv midtable.job person_table_now.job midTableAndPersonTableNow
zip midTableAndPersonTableNow.zip midTableAndPersonTableNow
zip person_table_before.job.zip person_table_before.job
zip start-face-offline-alarm-job.job.zip start-face-offline-alarm-job.job

rm -rf midTableAndPersonTableNow person_table_before.job start-face-offline-alarm-job.job