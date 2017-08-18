package com.hzgc.util;

import org.apache.log4j.Logger;

public class StringUtil {
    private static Logger LOG = Logger.getLogger(StringUtil.class);

    public static boolean strIsRight(String str) {
        if (null != str && str.length() > 0) {
            return true;
        }
        return false;
    }
}
