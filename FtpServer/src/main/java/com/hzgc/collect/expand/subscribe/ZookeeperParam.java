package com.hzgc.collect.expand.subscribe;

import com.hzgc.collect.expand.util.ClusterOverFtpProperHelper;

import java.io.Serializable;

/**
 * Zookeeper客户端连接参数
 */
public class ZookeeperParam implements Serializable{
    //session失效时间
    public static final int SESSION_TIMEOUT = 6000;
    //Zookeeper地址
    public static String zookeeperAddress = ClusterOverFtpProperHelper.getZookeeperAddress();
    //订阅节点路径
    public static final String PATH_SUBSCRIBE = "/ftp_subscribe";
    //演示节点路径
    public static final String PATH_SHOW = "/ftp_show";
    //注册在path上的Watcher,节点变更会通知会向客户端发起通知
    public static final boolean WATCHER = false;
}
