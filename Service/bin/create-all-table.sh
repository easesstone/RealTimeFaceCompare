#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    create-all.sh
## Description: 一键创建：hive表、es索引、kafka的topic、add-udf
##              相关参数（例如kafka分区数）在配置文件project-conf.properties中配置
## Version:     1.0
## Author:      mashencai
## Created:     2017-11-30 
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`                                         ### bin目录：脚本所在目录
cd ..
DEPLOY_DIR=`pwd`                                      ### service模块部署目录
CONF_SERVICE_DIR=$DEPLOY_DIR/conf                     ### 配置文件目录
LOG_DIR=$DEPLOY_DIR/logs                              ### log日志目录
LOG_FILE=$LOG_DIR/create-all.log                      ### log日志文件
cd ..
OBJECT_DIR=`pwd`                                      ### 项目根目录 
CONF_FILE=$OBJECT_DIR/project-conf.properties         ### 项目配置文件
cd ../hzgc/conf
CONF_HZGC_DIR=`pwd`                                   ### 集群配置文件目录

#---------------------------------------------------------------------#
#                              定义函数                                #
#---------------------------------------------------------------------#

#####################################################################
# 函数名: create_hivetable
# 描述: 创建hive表，执行create-dynamic-table.sh和create-static-repos.sh
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function create_hivetable()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "创建hive表......"  | tee  -a  $LOG_FILE

	# 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/create-dynamic-table.sh" ]; then 
        sh ${BIN_DIR}/create-dynamic-table.sh
    else
        echo "create-dynamic-table.sh脚本不存在...." 
    fi
	
	if [ -f "${BIN_DIR}/create-static-repos.sh" ]; then 
        sh ${BIN_DIR}/create-static-repos.sh
    else
        echo "create-static-repos.sh脚本不存在...." 
    fi
    
    echo "创建完毕......"  | tee  -a  $LOG_FILE
}


#####################################################################
# 函数名: add_udf
# 描述: 执行add-udf.sh
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function add_udf()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "执行add-udf.sh......"  | tee  -a  $LOG_FILE

	# 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/add-udf.sh" ]; then 
        sh ${BIN_DIR}/add-udf.sh
    else
        echo "add-udf.sh脚本不存在...." 
    fi
    
    echo "执行完毕......"  | tee  -a  $LOG_FILE
}


#####################################################################
# 函数名: index_es
# 描述: 创建es索引：执行index-dynamic.sh和index-static.sh
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function index_es()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "创建es索引......"  | tee  -a  $LOG_FILE

	index_es_dynamic
	index_es_static
    
    echo "创建完毕......"  | tee  -a  $LOG_FILE
}

#####################################################################
# 函数名: index_es_dynamic
# 描述: index_es的子函数，替换index-dynamic.sh中的节点名
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function index_es_dynamic()
{
    # 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/index-dynamic.sh.templete" ]; then 
	    cp ${BIN_DIR}/index-dynamic.sh.templete ${BIN_DIR}/index-dynamic.sh
		
		### 替换index-dynamic.sh中的节点名，共三处
		# 要替换的节点名，如s106
		es_host=$(sed -n '1p' ${CONF_HZGC_DIR}/hostnamelists.properties)
		
		## 第一处
		# 要查找的目标
		a1="curl -XDELETE '"
		b1="/dynamic?pretty'  -H 'Content-Type: application/json'"
		replace1="curl -XDELETE '${es_host}:9200/dynamic?pretty'  -H 'Content-Type: application/json'"
		# ^表示以什么开头，.*a表示以a结尾。替换以a1开头、b1结尾匹配到的字符串为repalce1
		sed -i "s#^${a1}.*${b1}#${replace1}#g" ${BIN_DIR}/index-dynamic.sh
		
		## 第二处
		a2="curl -XPUT '"
		b2="/dynamic?pretty' -H 'Content-Type: application/json' -d'"
		replace2="curl -XPUT '${es_host}:9200/dynamic?pretty' -H 'Content-Type: application/json' -d'"
		sed -i "s#^${a2}.*${b2}#${replace2}#g" ${BIN_DIR}/index-dynamic.sh
		
		## 第三处
		a3="curl -XPUT '"
		b3="/dynamic/_settings' -d '{"
		replace3="curl -XPUT '${es_host}:9200/dynamic/_settings' -d '{"
		sed -i "s#^${a3}.*${b3}#${replace3}#g" ${BIN_DIR}/index-dynamic.sh
		
		sh ${BIN_DIR}/index-dynamic.sh
		echo "修改index-dynamic.sh成功并执行......"  | tee  -a  $LOG_FILE
		
    else
        echo "index-dynamic.sh.templete不存在...." 
    fi
}

#####################################################################
# 函数名: index_es_static
# 描述: index_es的子函数，替换index-static.sh中的节点名
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function index_es_static()
{
    # 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/index-static.sh.templete" ]; then 
	    cp ${BIN_DIR}/index-static.sh.templete ${BIN_DIR}/index-static.sh
		
		### 替换index-static.sh中的节点名，共三处
		# 要替换的节点名，如s106
		es_host=$(sed -n '1p' ${CONF_HZGC_DIR}/hostnamelists.properties)
		
		## 第一处
		# 要查找的目标
		a1="curl -XDELETE '"
		b1="/objectinfo?pretty' -H 'Content-Type: application/json'"
		replace1="curl -XDELETE '${es_host}:9200/objectinfo?pretty' -H 'Content-Type: application/json'"
		# ^表示以什么开头，.*a表示以a结尾。替换以a1开头、b1结尾匹配到的字符串为repalce1
		sed -i "s#^${a1}.*${b1}#${replace1}#g" ${BIN_DIR}/index-static.sh
		
		## 第二处
		a2="curl -XPUT '"
		b2="/objectinfo?pretty' -H 'Content-Type: application/json' -d'"
		replace2="curl -XPUT '${es_host}:9200/objectinfo?pretty' -H 'Content-Type: application/json' -d'"
		sed -i "s#^${a2}.*${b2}#${replace2}#g" ${BIN_DIR}/index-static.sh
		
		## 第三处
		a3="curl -XPUT '"
		b3="/objectinfo/_settings' -d '{"
		replace3="curl -XPUT '${es_host}:9200/objectinfo/_settings' -d '{"
		sed -i "s#^${a3}.*${b3}#${replace3}#g" ${BIN_DIR}/index-static.sh
		
		sh ${BIN_DIR}/index-static.sh
		echo "修改index-static.sh成功并执行......"  | tee  -a  $LOG_FILE
		
    else
        echo "index-static.sh.templete不存在...." 
    fi
}

#####################################################################
# 函数名: create_kafka_topic
# 描述: 创建kafka的topic
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function create_kafka_topic()
{
    echo ""  | tee -a $LOG_FILE
    echo "**********************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "执行create-kafka-topic.sh......"  | tee  -a  $LOG_FILE
    
	# 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/create-kafka-topic.sh" ]; then 
        sh ${BIN_DIR}/create-kafka-topic.sh
    else
        echo "create-kafka-topic.sh脚本不存在...." 
    fi
	
    echo "执行完毕......"  | tee  -a  $LOG_FILE
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
    create_hivetable
	add_udf
	index_es
	create_kafka_topic
}


#---------------------------------------------------------------------#
#                              执行流程                                #
#---------------------------------------------------------------------#

## 打印时间
echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE
echo "开始一键创建：hive表、es索引、kafka的topic、add-udf.." | tee  -a  $LOG_FILE
main

set +x
