#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    facecomp-daemon.sh
## Description: 大数据dubbo ftp 守护脚本
## Version:     1.0.0
## Author:      lidiliang
## Created:     2017-11-18
## Modified:    (ldl-2017-12-1)
################################################################################

#set -x

#crontab 里面不会读取jdk环境变量的值
source /etc/profile

#set -x



#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
cd `dirname  $0`
declare -r BIN_DIR=`pwd`   #bin 目录
cd ..
declare -r  DEPLOY_DIR=`pwd`  #项目根目录
declare -r  CONF_DIR=${DEPLOY_DIR}/conf
declare -r  LOG_DIR=${DEPLOY_DIR}/logs                       ## log 日记目录
declare -r  LOG_FILE=${LOG_DIR}/dubbo.log        ##  log 日记文件
declare -r  CHECK_LOG_FILE=${LOG_DIR}/check_dubbo.log

#############             dubbo变量               #############
SERVER_NAME=`sed '/dubbo.application.name/!d;s/.*=//' conf/dubbo.properties | tr -d '\r'`
SERVER_PROTOCOL=`sed '/dubbo.protocol.name/!d;s/.*=//' conf/dubbo.properties | tr -d '\r'`
SERVER_PORT=`sed '/dubbo.protocol.port/!d;s/.*=//' conf/dubbo.properties | tr -d '\r'`
LOGS_FILE=`sed '/dubbo.log4j.file/!d;s/.*=//' conf/dubbo.properties | tr -d '\r'`
cd ..
declare -r BIGDATA_SERVICE_DIR=`pwd`
declare -r COMMMON_DIR=${BIGDATA_SERVICE_DIR}/common
declare -r FTP_DIR=${BIGDATA_SERVICE_DIR}/ftp
## 用于包含ftpAddress.properties
CONF_DIR_DUBBO=${CONF_DIR}:${FTP_DIR}/conf

cd -

LIB_DIR=$DEPLOY_DIR/lib
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`

COMMON_JARS=`ls ${COMMMON_DIR}/lib | grep .jar | awk '{print "'${COMMMON_DIR}/lib'/"$0}'|tr "\n" ":"`
LIB_JARS=${LIB_JARS}${COMMON_JARS}

#####################################################################
# 函数名: start_dubbo
# 描述: 开启dubbo服务。
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function start_dubbo()
{
    echo "*************** start dubbo ***************" | tee -a $CHECK_LOG_FILE
    if [ -z "$SERVER_NAME" ]; then
        SERVER_NAME=`hostname`
    fi
    if [ -n "$SERVER_PORT" ]; then
        SERVER_PORT_COUNT=`netstat -tln | grep $SERVER_PORT | wc -l`
        if [ $SERVER_PORT_COUNT -gt 0 ]; then
            echo "ERROR: The $SERVER_NAME port $SERVER_PORT already used!" | tee -a $CHECK_LOG_FILE
            exit 1
        fi
    fi
    JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
    JAVA_DEBUG_OPTS=""
    if [ "$1" = "debug" ]; then
        JAVA_DEBUG_OPTS=" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n "
    fi
    JAVA_JMX_OPTS=""
    if [ "$1" = "jmx" ]; then
        JAVA_JMX_OPTS=" -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false "
    fi
    JAVA_MEM_OPTS=""
    BITS=`java -version 2>&1 | grep -i 64-bit`
    if [ -n "$BITS" ]; then
        JAVA_MEM_OPTS=" -server -Xmx2g -Xms2g -Xmn256m -XX:PermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 "
    else
        JAVA_MEM_OPTS=" -server -Xms1g -Xmx1g -XX:PermSize=128m -XX:SurvivorRatio=2 -XX:+UseParallelGC "
    fi
    echo "Starting the $SERVER_NAME ...\c" | tee -a $CHECK_LOG_FILE
    nohup java $JAVA_OPTS $JAVA_MEM_OPTS $JAVA_DEBUG_OPTS $JAVA_JMX_OPTS -classpath $CONF_DIR_DUBBO:$LIB_JARS com.alibaba.dubbo.container.Main > $LOG_FILE 2>&1 &
    COUNT=0
    while [ $COUNT -lt 1 ]; do
        echo  ".\c" | tee -a $CHECK_LOG_FILE
        sleep 1
        if [ -n "$SERVER_PORT" ]; then
            COUNT=`netstat -an | grep $SERVER_PORT | wc -l`
        else
            COUNT=`ps -f | grep java | grep "$DEPLOY_DIR" | awk '{print $2}' | wc -l`
        fi
        if [ $COUNT -gt 0 ]; then
            break
        fi
    done

    echo "successful!" | tee -a $CHECK_LOG_FILE
    PIDS=`ps -f | grep java | grep "$DEPLOY_DIR" | awk '{print $2}'`
    echo "PID: $PIDS" | tee -a $CHECK_LOG_FILE
    echo "STDOUT: $STDOUT_FILE" | tee -a $CHECK_LOG_FILE
    echo "*************** start dubbo success!!! ***************" | tee -a $CHECK_LOG_FILE

}
#####################################################################
# 函数名: check_dubbo
# 描述: 把脚本定时执行，定时监控dubbo 服务是否挂掉，如果挂掉则重启。
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function check_dubbo()
{
    echo ""  | tee  -a  $CHECK_LOG_FILE
    echo "****************************************************"  | tee -a $CHECK_LOG_FILE
    echo "dubbo procceding ing......................."  | tee  -a  $CHECK_LOG_FILE
    dubbo_pid=$(lsof  -i | grep 20881  | awk  '{print $2}' | uniq)
    echo "dubbo's pid is: ${dubbo_pid}"  | tee  -a  $CHECK_LOG_FILE
    if [ -n "${dubbo_pid}" ];then
        echo "dubbo process is exit,do not need to do anything. exit with 0 " | tee -a $CHECK_LOG_FILE
    else
        echo "dubbo process is not exit, just to restart dubbo."  | tee -a $CHECK_LOG_FILE
        #sh ${BIN_DIR}/start-dubbo.sh
        start_dubbo
        echo "starting, please wait........" | tee -a $CHECK_LOG_FILE
        sleep 1m
        dubbo_pid_restart=$(lsof  -i | grep 20881  | awk  '{print $2}' | uniq)
        if [ -z "${dubbo_pid_restart}" ];then
            echo "start dubbo failed.....,retrying to start it second time" | tee -a $CHECK_LOG_FILE
            #sh ${BIN_DIR}/start-dubbo.sh
            start_dubbo
            echo "second try starting, please wait........" | tee -a $CHECK_LOG_FILE
            sleep 1m
            dubbo_pid_retry=$(lsof  -i | grep 20881  | awk  '{print $2}' | uniq)
            if [ -z  "${dubbo_pid_retry}" ];then
                echo "retry start dubbo failed, please check the config......exit with 1"  | tee -a $CHECK_LOG_FILE
            else
                echo "secondary try start ftp sucess. exit with 0." | tee -a $CHECK_LOG_FILE
            fi
        else
            echo "trying to restart dubbo sucess. exit with 0."  | tee -a $CHECK_LOG_FILE
        fi
    fi
}

#####################################################################
# 函数名: main
# 描述: 模块功能main 入口，即程序入口, 用来监听整个大数据服务的情况。
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function main()
{
   while true
   do
       sleep 5m
       check_dubbo
   done
}


# 主程序入口
main


