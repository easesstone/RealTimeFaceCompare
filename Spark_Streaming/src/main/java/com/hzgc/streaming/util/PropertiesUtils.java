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
     * 通过key获取资源文件的value
     * @param key 资源文件的key
     * @return 返回key对应资源文件的value
     * @throws Exception
     */
    public static String getPropertiesValue(String key) {
        Properties ps = new Properties();
        try{
            InputStream is = new FileInputStream(FileUtil.loadResourceFile("sparkJob.properties"));
            ps.load(is);
        }catch (Exception e){
            System.out.println(e);
        }
        String value = ps.getProperty(key);
        return value;

    }

}
