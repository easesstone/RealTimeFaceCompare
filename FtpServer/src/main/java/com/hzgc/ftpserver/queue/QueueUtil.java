package com.hzgc.ftpserver.queue;

import com.hzgc.util.common.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.FileImageInputStream;
import java.io.*;
import java.util.Properties;

public class QueueUtil {
    final Logger LOG = LoggerFactory.getLogger(QueueUtil.class);

    /**
     * 获取资源文件信息
     *
     * @return Properties对象
     */
    public static Properties getProperties() {
        Properties ps = new Properties();
        try {
            InputStream is = new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties"));
            ps.load(is);
        } catch (Exception e) {
            System.out.println(e);
        }
        return ps;
    }

    /**
     * 本地图片转化为字节数组
     *
     * @param path 图片路径
     * @return 字节数组
     */
    public static byte[] getData(String path) {
        byte[] data = null;
        //数据存储位置
        StringBuilder sb = new StringBuilder("/opt/ftpdata");
        FileImageInputStream input = null;
        try {
            input = new FileImageInputStream(new File(sb.append(path).toString()));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();
        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return data;
    }

}
