#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    create-all.sh
## Description: 一键创建静态库和动态库的表
## Author:      mashencai
## Created:     2017-11-30 
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`                                          ### bin目录：脚本所在目录
cd ..
SERVICE_DIR=`pwd`                                      ### service模块部署目录
CONF_SERVICE_DIR=$SERVICE_DIR/conf                     ### 配置文件目录
LOG_DIR=$SERVICE_DIR/logs                              ### log日志目录
LOG_FILE=$LOG_DIR/create-all.log                       ### log日志文件

sh ${BIN_DIR}/create-dynamic-table.sh
sh ${BIN_DIR}/create-static-repos.sh

set +x
