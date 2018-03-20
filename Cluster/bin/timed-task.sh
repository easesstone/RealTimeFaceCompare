#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    timed-task.sh
## Description: 一键配置脚本：生成定时任务压缩包
## Version:     1.0
## Author:      caodabao
## Created:     2018-03-09
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#

cd `dirname $0`
BIN_DIR=`pwd`                                         ### bin目录：脚本所在目录
cd ..
CLUSTER_DIR=`pwd`                                      ### cluster模块部署目录
CONF_CLUSTER_DIR=$CLUSTER_DIR/conf                     ### 配置文件目录
LOG_DIR=$CLUSTER_DIR/logs                              ### log日志目录
LOG_FILE=$LOG_DIR/timed_task.log                       ### log日志目录
cd ..
OBJECT_DIR=`pwd`                                       ### 项目根目录
AZKABAN_DIR=${OBJECT_DIR}/Azkaban                      ### 定时任务压缩包存放目录

SCHEMA_MERGE_PARQUET_SH=${BIN_DIR}/schema-merge-parquet-file.sh
SCHEMA_MERGE_PARQUET_JOB=${BIN_DIR}/schema_merge_parquet_file.job

SCHEMA_MERGE_FINAL_TABLE_SH=${BIN_DIR}/schema-merge-final-table.sh
SCHEMA_MERGE_FINAL_TABLE_JOB=${BIN_DIR}/schema_merge_final_table.job

SCHEMA_MERGE_FINAL_TABLE_CRASH_SH=${BIN_DIR}/schema-merge-final-table-crash.sh
SCHEMA_MERGE_FINAL_TABLE_CRASH_JOB=${BIN_DIR}/schema_merge_final_table_crash.job

START_FACE_OFFLINE_ALARM_SH=${BIN_DIR}/start-face-offline-alarm-job.sh
START_FACE_OFFLINE_ALARM_JOB=${BIN_DIR}/start_face_offline_alarm_job.job

mkdir -p $LOG_DIR
mkdir -p $AZKABAN_DIR

#---------------------------------------------------------------------#
#                              定义函数                                #
#---------------------------------------------------------------------#

#####################################################################
# 函数名: check_timed_task
# 描述: 检测定时任务脚本是否存在，并压缩
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function check_timed_task()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "检测定时任务文件是否存在......"  | tee  -a  $LOG_FILE
	# 判断文件是否存在，存在才压缩
	if [ -f "${SCHEMA_MERGE_PARQUET_SH}" ]; then
	    if [ -f "${SCHEMA_MERGE_PARQUET_JOB}" ]; then
                cd ${BIN_DIR}
	        zip  schema_merge_parquet_file schema_merge_parquet_file.job
	        if [ $? -eq 0 ];then
	            mv schema_merge_parquet_file.zip ${AZKABAN_DIR}
	        else
	            echo "schema_merge_parquet_file.zip压缩失败！！"
	        fi
		else
		    echo "schema_merge_parquet_file.job文件不存在，请检视！"
		fi
                cd -
	else
		echo "schema-merge-parquet-file.sh脚本不存在,请检视！！"
	fi

	if [ -f "${SCHEMA_MERGE_FINAL_TABLE_SH}" ]; then
	    if [ -f "${SCHEMA_MERGE_FINAL_TABLE_JOB}" ]; then
                cd ${BIN_DIR}
	        zip  schema_merge_final_table schema_merge_final_table.job
	        if [ $? -eq 0 ];then
	            mv schema_merge_final_table.zip ${AZKABAN_DIR}
	        else
	            echo "schema_merge_final_table.zip 压缩失败！！"
	        fi
		else
		    echo "schema_merge_final_table.job 文件不存在，请检视！"
		fi
                cd -
	else
		echo "schema-merge-final-table.sh 脚本不存在,请检视！！"
	fi

	if [ -f "${SCHEMA_MERGE_FINAL_TABLE_CRASH_SH}" ]; then
	    if [ -f "${SCHEMA_MERGE_FINAL_TABLE_CRASH_JOB}" ]; then
                cd ${BIN_DIR}
	        zip  schema_merge_final_table_crash schema_merge_final_table_crash.job
	        if [ $? -eq 0 ];then
	            mv schema_merge_final_table_crash.zip ${AZKABAN_DIR}
	        else
	            echo "schema_merge_final_table_crash.zip 压缩失败！！"
	        fi
		else
		    echo "schema_merge_final_table_crash.job 文件不存在，请检视！"
		fi
                cd -
	else
		echo "schema-merge-final-table-crash.sh 脚本不存在,请检视！！"
	fi


	if [ -f "${START_FACE_OFFLINE_ALARM_SH}" ]; then
	    if [ -f "${START_FACE_OFFLINE_ALARM_JOB}" ]; then
                cd ${BIN_DIR}
	        zip  start_face_offline_alarm_job start_face_offline_alarm_job.job
	        if [ $? -eq 0 ];then
	            mv start_face_offline_alarm_job.zip ${AZKABAN_DIR}
	        else
	            echo "start_face_offline_alarm_job.zip 压缩失败！！"
	        fi
		else
		    echo "start_face_offline_alarm_job.job 文件不存在，请检视！"
		fi
                cd -
	else
		echo "start-face-offline-alarm-job.sh 脚本不存在,请检视！！"
	fi
        echo "压缩完成........"

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
    check_timed_task
}
#---------------------------------------------------------------------#
#                              执行流程                                #
#---------------------------------------------------------------------#

## 打印时间
echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE
echo "开始压缩定时任务文件"                       | tee  -a  $LOG_FILE
main

set +x
