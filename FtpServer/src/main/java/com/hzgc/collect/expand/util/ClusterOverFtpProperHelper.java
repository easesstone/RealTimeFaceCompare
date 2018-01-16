package com.hzgc.collect.expand.util;

import com.hzgc.util.common.FileUtil;

public class ClusterOverFtpProperHelper implements ProperHelper {
    private static String properName = "cluster-over-ftp.properties";
    public static String port;
    public static String threadNum;
    public static String implicitSsl;

    public ClusterOverFtpProperHelper() {
        FileUtil.loadResourceFile(properName);
    }

    public ClusterOverFtpProperHelper(String properName) {
        ClusterOverFtpProperHelper.properName = properName;
    }
    @Override
    public String getProper(String key) {
        return null;
    }
}
