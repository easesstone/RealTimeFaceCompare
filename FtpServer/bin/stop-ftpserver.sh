#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    ftpoverkafka
## Description: stop  ftp
## Version:     1.0
## Author:      caodabao
## Created:     2017-11-15
################################################################################

#set -x
cd `dirname $0`
## 脚本所在目录
BIN_DIR=`pwd`
cd ..
## ftp根目录
FTP_DIR=`pwd`
## log 日记目录
LOG_DIR=${FTP_DIR}/logs/
##log日志文件
LOG_FILE=${LOG_DIR}/ftp.log
source /etc/profile
stop_ftp=1                                                      ## 判断ftp是否关闭成功 1->失败 0->成功 默认失败


#####################################################################
# 函数名:stopftp 
# 描述: 停止ftp
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function stopftp ()
{
    echo ""  | tee -a $LOG_FILE
    echo "****************************************************"  | tee -a $LOG_FILE
    echo "ftp procceding......................." | tee  -a $LOG_FILE
    ftp_pid=$(jps | grep LocalOverFtpServer | awk '{print $1}')
    echo "ftp's pid is: ${ftp_pid}"  | tee -a $LOG_FILE
    if [ -n "${ftp_pid}" ];then
        echo "ftp process is exit,exit with 0,kill ftp now " | tee -a $LOG_FILE  
        kill -9 ${ftp_pid}
        sleep 5s
        ftp_pid=$(jps | grep LocalOverFtpServer | awk '{print $1}')
        if [ -n "${ftp_pid}" ];then
            stop_ftp=1
            echo "stop ftp failure, retry it again."  | tee -a  $LOG_FILE
        else
            stop_ftp=0
            echo "stop ftp sucessed, just to start ftp."  | tee -a  $LOG_FILE
        fi
    else 
        echo "ftp process is not exit, just to start ftp."   | tee -a $LOG_FILE
        stop_ftp=0
    fi
}

#####################################################################
# 函数名: main
# 描述:  停止dubbo的入口函数
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function main()
{
    stopftp
    if [ ${stop_ftp} -eq 0 ];then
        echo "stop ftp sucessed" | tee -a  $LOG_FILE  
    else
        stopftp
        if [ ${stop_ftp} -eq 1 ];then
            echo "retry stop ftp failed please check the config......exit with 1" | tee -a  $LOG_FILE
        fi
    fi
}

echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                        | tee  -a  $LOG_FILE
main
