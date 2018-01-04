#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-consumer.sh
## Description: to start consumer
## Version:     1.0
## Author:      liushanbin
## Created:     2017-08-03
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`    ### bin目录
cd ..
DEPLOY_DIR=`pwd`
CONF_DIR=$DEPLOY_DIR/conf    ### 项目根目录

cd ..
declare -r BIGDATA_SERVICE_DIR=`pwd`
declare -r COMMMON_DIR=${BIGDATA_SERVICE_DIR}/common
declare -r FTP_DIR=${BIGDATA_SERVICE_DIR}/ftp
declare -r SERVICE=${BIGDATA_SERVICE_DIR}/service
declare -r CLUSTER_DIR=${BIGDATA_SERVICE_DIR}/cluster

cd -
COMMON_JARS=`ls ${COMMMON_DIR}/lib | grep .jar | awk '{print "'${COMMMON_DIR}/lib'/"$0}'|tr "\n" ":"`

LIB_DIR=$DEPLOY_DIR/lib        ## Jar 包目录
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`    ## jar 包位置以及第三方依赖jar包，绝对路径
LIB_JARS=${LIB_JARS}${COMMON_JARS}
LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录
LOG_FILE=${LOG_DIR}/ftpserver.log        ##  log 日记文件
CHECK_LOG_FILE=${LOG_DIR}/check_ftpserver.log
flag_ftp=0   #标志ftp 进程是否在线
#####################################################################
# 函数名: start_ftpserver
# 描述: 启动ftp服务
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function start_ftpserver()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
    echo "$SERVER_NAME:$SERVER_PORT started ..."
    nohup java -server -Xms2g -Xmx4g  -XX:PermSize=512m -XX:MaxPermSize=512m  -classpath $CONF_DIR:$LIB_JARS com.hzgc.ftpserver.FTP > ${LOG_FILE} 2>&1 &
}
####################################################################
# 函数名:check_ftpserver
# 描述：检查ftp是否健康运行
# 参数: N/A
# 返回值: N/A
# 其他: N/A
####################################################################
function check_ftpserver()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
    echo ""  | tee -a $CHECK_LOG_FILE
    echo "****************************************************"  | tee -a $CHECK_LOG_FILE
    source /etc/profile;
    ftp_pid=$(jps | grep FTP)
    sleep 2s
    if [ -n "${ftp_pid}" ];then
        echo "current time : $(date)"  | tee -a $CHECK_LOG_FILE
        echo "ftp process is exit,do not need to do anything. exit with 0 " | tee -a $CHECK_LOG_FILE
    else
        echo "ftp process is not exit, just to restart ftp."   | tee -a $CHECK_LOG_FILE
        start_ftpserver
        echo "starting, please wait........" | tee -a $CHECK_LOG_FILE
        sleep 20s
        ftp_pid_restart=$(jps | grep FTP)
        if [ -z "${ftp_pid_restart}" ];then
            echo "first trying start ftp failed.....,retrying to start it second time"  | tee -a $CHECK_LOG_FILE
            start_ftpserver
            sleep 20s
            ftp_pid_retry=$(jps | grep FTP)
            if [ -z  "${ftp_pid_retry}" ];then
                echo "retry start ftp failed, please check the config......exit with 1"  | tee -a  $CHECK_LOG_FILE
                flag_ftp=1
            else
                echo "secondary try start ftp sucess. exit with 0."  | tee -a  $CHECK_LOG_FILE
            fi
        else
            echo "trying to start ftp sucess. exit with 0."  | tee -a  $CHECK_LOG_FILE
        fi
    fi
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
   while true
   do
       sleep 5m
       check_ftpserver
   done
}

## 脚本主要业务入口
main
