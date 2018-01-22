package com.hzgc.collect.expand.util;

import com.hzgc.util.common.FileUtil;

public class FTPAddressProperHelper implements ProperHelper {
    private static String properName = "ftpAddress.properties";
    public static String ip;
    public static String port;
    public static String user;
    public static String password;
    public static String pathRule;

    public FTPAddressProperHelper() {
        FileUtil.loadResourceFile(properName);
    }

    public FTPAddressProperHelper(String properName) {
        FTPAddressProperHelper.properName = properName;
    }

}
