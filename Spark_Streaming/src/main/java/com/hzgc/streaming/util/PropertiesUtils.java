package com.hzgc.streaming.util;

import com.hzgc.util.FileUtil;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * 资源文件工具类
 */
public class PropertiesUtils implements Serializable {
    /**
     * 加载资源文件
     *
     * @return Properties对象
     */
    public static Properties getProperties() {
        Properties ps = new Properties();
        try {
            InputStream is = new FileInputStream(FileUtil.loadResourceFile("sparkJob.properties"));
            ps.load(is);
        } catch (Exception e) {
            System.out.println(e);
        }
        return ps;
    }

}
