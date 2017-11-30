#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    config-projectconf
## Description: 一键配置脚本：执行cluster、ftp、service的一键配置脚本
## Version:     1.0
## Author:      mashencai
## Created:     2017-11-30
################################################################################
set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#

cd `dirname $0`
BIN_DIR=`pwd`                                         ### bin目录：脚本所在目录
cd ..
DEPLOY_DIR=`pwd`                                      ### common模块部署目录
CONF_FTP_DIR=$DEPLOY_DIR/conf                         ### 配置文件目录
LOG_DIR=$DEPLOY_DIR/logs                              ### log日志目录
LOG_FILE=$LOG_DIR/config-project.log                  ### log日志目录
cd ..
OBJECT_DIR=`pwd`                                      ### 项目根目录 
CLUSTER_DIR=$OBJECT_DIR/cluster                       ### cluster模块部署目录
FTP_DIR=$OBJECT_DIR/ftp                               ### ftp模块部署目录
SERVICE_DIR=$OBJECT_DIR/service                       ### service模块部署目录

CLUSTER_SCRPIT=$CLUSTER_DIR/bin/config-clusterconf.sh ### 一键配置cluster脚本
FTP_SCRPIT=$FTP_DIR/bin/config-ftpconf.sh             ### 一键配置ftp脚本
SERVICE_SCRPIT=$SERVICE_DIR/bin/config-serviceconf.sh ### 一键配置service脚本


function sh_cluster()
{
	# 判断脚本是否存在，存在才执行
	if [ -f "${CLUSTER_SCRPIT}" ]; then 
		sh ${CLUSTER_SCRPIT}
	else
		echo "config-clusterconf.sh脚本不存在...." 
	fi
}

function sh_ftp()
{
	if [ -f "${FTP_SCRPIT}" ]; then 
		sh ${FTP_SCRPIT}
	else
		echo "config-ftpconf.sh脚本不存在...." 
	fi
}

function sh_service()
{
	if [ -f "${SERVICE_SCRPIT}" ]; then 
		sh ${SERVICE_SCRPIT}
	else
		echo "config-serviceconf.sh脚本不存在...." 
	fi
}

#---------------------------------------------------------------------#
#                              执行流程                                #
#---------------------------------------------------------------------#

## 打印时间
echo ""  
echo ""  
echo "==================================================="
echo "$(date "+%Y-%m-%d  %H:%M:%S")"


sh_cluster
sh_ftp
sh_service



set +x
