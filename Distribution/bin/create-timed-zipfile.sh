#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    create-timed-zipfile.sh
## Description: 一键配置脚本：生成定时任务job文件并压缩
## Version:     1.0
## Author:      caodabao
## Created:     2018-03-09
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#

cd `dirname $0`
BIN_DIR=`pwd`                                          ### bin目录：脚本所在目录
cd ..
COMMON_DIR=`pwd`                                       ### common模块部署目录
CONF_CLUSTER_DIR=$CLUSTER_DIR/conf                     ### 配置文件目录
LOG_DIR=$CLUSTER_DIR/logs                              ### log日志目录
LOG_FILE=$LOG_DIR/timed_task.log                       ### log日志目录
cd ..
OBJECT_DIR=`pwd`                                       ### 项目根目录
CLUSTER_DIR=${OBJECT_DIR}/cluster                      ### cluster模块目录
CLUSTER_BIN_DIR=${CLUSTER_DIR}/                        ### cluster模块的bin目录

AZKABAN_DIR=${OBJECT_DIR}/Azkaban                      ### 定时任务压缩包存放目录

SCHEMA_MERGE_PARQUET_SH=${CLUSTER_BIN_DIR}/schema-merge-parquet-file.sh

SCHEMA_MERGE_MID_TABLE_JOB=${AZKABAN_DIR}/schema_merge_mid_table.job
SCHEMA_MERGE_PERSON_TABLE_JOB=${AZKABAN_DIR}/schema_merge_person_table.job
SCHEMA_MERGE_PERSON_TABLE_BEFORE_JOB=${AZKABAN_DIR}/schema_merge_person_table_before.job

START_FACE_OFFLINE_ALARM_SH=${CLUSTER_BIN_DIR}/start-face-offline-alarm-job.sh
START_FACE_OFFLINE_ALARM_JOB=${AZKABAN_DIR}/start_face_offline_alarm_job.job

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
	    echo "========生成schema_merge_mid_table.zip压缩包========"
	    if [ -f "${SCHEMA_MERGE_MID_TABLE_JOB}" ]; then
            rm -rf ${SCHEMA_MERGE_MID_TABLE_JOB}
		fi
		### 生成schema_merge_mid_table.job文件
        create_zip "null"
        if [ $? -eq 0 ];then
            cd ${AZKABAN_DIR}
	        zip  schema_merge_mid_table schema_merge_mid_table.job
	        if [ $? -eq 0 ];then
	            echo "schema_merge_mid_table.zip压缩成功！！"
	            rm -rf ${SCHEMA_MERGE_MID_TABLE_JOB}
	        else
	            echo "schema_merge_mid_table.zip压缩失败！！"
	        fi
	    else
	        echo "生成schema_merge_mid_table.job文件失败"
        fi

	    echo "========生成schema_merge_person_table.zip压缩包========"
	    if [ -f "${SCHEMA_MERGE_PERSON_TABLE_JOB}" ]; then
            rm -rf ${SCHEMA_MERGE_PERSON_TABLE_JOB}
		fi
		### 生成schema_merge_person_table.job文件
        create_zip "now"
        if [ $? -eq 0 ];then
            cd ${AZKABAN_DIR}
	        zip  schema_merge_person_table schema_merge_person_table.job
	        if [ $? -eq 0 ];then
	            echo "schema_merge_person_table.zip压缩成功！！"
	            rm -rf ${SCHEMA_MERGE_PERSON_TABLE_JOB}
	        else
	            echo "schema_merge_person_table.zip压缩失败！！"
	        fi
	    else
	        echo "生成schema_merge_person_table.job文件失败"
        fi

	    echo "========生成schema_merge_person_table_before.zip压缩包========"
	    if [ -f "${SCHEMA_MERGE_PERSON_TABLE_BEFORE_JOB}" ]; then
            rm -rf ${SCHEMA_MERGE_PERSON_TABLE_BEFORE_JOB}
		fi
		### 生成schema_merge_person_table_before.job文件
        create_zip "before"
        if [ $? -eq 0 ];then
            cd ${AZKABAN_DIR}
	        zip  schema_merge_person_table_before schema_merge_person_table_before.job
	        if [ $? -eq 0 ];then
	            echo "schema_merge_person_table_before.zip压缩成功！！"
	            rm -rf ${SCHEMA_MERGE_PERSON_TABLE_BEFORE_JOB}
	        else
	            echo "schema_merge_person_table_before.zip压缩失败！！"
	        fi
	    else
	        echo "生成schema_merge_person_table_before.job文件失败"
        fi
	else
		echo "schema-merge-parquet-file.sh脚本不存在,请检视！！"
	fi

	if [ -f "${START_FACE_OFFLINE_ALARM_SH}" ]; then
        ### 生成start_face_offline_alarm_job.job文件
	    echo "========生成schema_merge_person_table_before.zip压缩包========"
	    if [ -f "${START_FACE_OFFLINE_ALARM_JOB}" ]; then
            rm -rf ${START_FACE_OFFLINE_ALARM_JOB}
		fi
        create_zip "offline"
        if [ $? -eq 0 ];then
            cd ${AZKABAN_DIR}
	        zip  start_face_offline_alarm_job start_face_offline_alarm_job.job
	        if [ $? -eq 0 ];then
	            echo "start_face_offline_alarm_job.zip压缩成功！！"
	            rm -rf ${START_FACE_OFFLINE_ALARM_JOB}
	        else
	            echo "start_face_offline_alarm_job.zip 压缩失败！！"
	        fi
		else
		    echo "生成start_face_offline_alarm_job.job文件失败"
		fi
                cd -
	else
		echo "start-face-offline-alarm-job.sh 脚本不存在,请检视！！"
	fi

echo "压缩完成........"

}
#####################################################################
# 函数名: create_zip
# 描述: 生成定时任务文件并压缩
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function create_zip()
{
    echo "==================================================="
    case $1 in
    [nN][uU][lL][lL] )
        echo "type=command" > ${SCHEMA_MERGE_MID_TABLE_JOB}
        echo "CLUSTER_BIN_DIR=/opt/RealTimeCompare/cluster/bin" >> ${SCHEMA_MERGE_MID_TABLE_JOB}
        echo 'command=sh ${CLUSTER_BIN_DIR}/schema-merge-parquet-file.sh mid_table' >> ${SCHEMA_MERGE_MID_TABLE_JOB};;
    [nN][oO][wW] )
        echo "type=command" > ${SCHEMA_MERGE_PERSON_TABLE_JOB}
        echo "CLUSTER_BIN_DIR=/opt/RealTimeCompare/cluster/bin" >> ${SCHEMA_MERGE_PERSON_TABLE_JOB}
        echo 'command=sh ${CLUSTER_BIN_DIR}/schema-merge-parquet-file.sh person_table now ' >> ${SCHEMA_MERGE_PERSON_TABLE_JOB};;
    [bB][eE][fF][oO][rR][eE] )
        echo "type=command" > ${SCHEMA_MERGE_PERSON_TABLE_BEFORE_JOB}
        echo "CLUSTER_BIN_DIR=/opt/RealTimeCompare/cluster/bin person_table before" >> ${SCHEMA_MERGE_PERSON_TABLE_BEFORE_JOB}
        echo 'command=sh ${CLUSTER_BIN_DIR}/schema-merge-parquet-file.sh person_table before' >> ${SCHEMA_MERGE_PERSON_TABLE_BEFORE_JOB};;
    [oO][fF][fF][lL][iI][nN][eE] )
        echo "type=command" > ${START_FACE_OFFLINE_ALARM_JOB}
        echo "CLUSTER_BIN_DIR=/opt/RealTimeCompare/cluster/bin" >> ${START_FACE_OFFLINE_ALARM_JOB}
        echo 'command=sh ${CLUSTER_BIN_DIR}/start-face-offline-alarm-job.sh' >> ${START_FACE_OFFLINE_ALARM_JOB};;
    *)
        echo "输入参数有误，请检视！！"
    esac
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
