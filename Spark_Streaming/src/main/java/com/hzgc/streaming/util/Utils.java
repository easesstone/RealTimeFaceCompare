package com.hzgc.streaming.util;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 转换工具类（刘善彬）（内 to 刘善彬）
 */
public class Utils implements Serializable {
    /**
     * 字节数组转化为字符串
     *
     * @param b
     * @return
     * @throws Exception
     */
    public static String byteArray2string(byte[] b) throws Exception {
        String str = new String(b, "ISO-8859-1");
        return str;
    }

    /**
     * scala 数组转化为Java list（刘善彬 To 内）
     *
     * @param array
     * @return
     */
    public static List<String> arrayBuffer2javaList(String[] array) {
        return Arrays.asList(array);
    }

    /**
     * java list 转化为Scala数组
     *
     * @param list
     * @return
     */
    public static String[] javaList2arrayBuffer(List list) {
        String[] str = new String[list.size()];
        return (String[]) list.toArray(str);
    }

    /**
     * 获取离线告警更新时间距离与当前时间的天数（刘善彬 To 内）
     *
     * @param updateTime 告警更新时间（时间格式：yyyy-MM-dd HH:mm:ss）
     * @return 距当前时间的天数
     */
    public static String timeTransition(String updateTime) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //更新时间
            Date date = sf.parse(updateTime);
            long updateT = date.getTime();
            //当前时间
            Date d = new Date(System.currentTimeMillis());
            long currentTime = d.getTime();
            //一天的毫秒数
            long t = 1000 * 60 * 60 * 24;
            long interval = currentTime - updateT;
            Double days = interval * 1.0 / t;
            DecimalFormat df = new DecimalFormat("######0.00");
            return df.format(days);

        } catch (ParseException e) {
            System.out.println(e.toString());
        }
        return null;
    }


}
