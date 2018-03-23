package com.hzgc.collect.expand.subscribe;

import com.hzgc.collect.expand.util.ClusterOverFtpProperHelper;

import java.io.Serializable;

/**
 * FTP接收处理数据总开关
 */
public class FtpSwitch implements Serializable{

    private static boolean ftpSwitch;

    public FtpSwitch(){
        ftpSwitch = Boolean.parseBoolean(ClusterOverFtpProperHelper.getFtpSwitch());
    }

    public static boolean isFtpSwitch() {
        return ftpSwitch;
    }
}
