package com.hzgc.ftpserver.kafka.ftp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {

    private static final Logger LOG = LoggerFactory.getLogger(Test.class);
    private static String fileName = "/17130NCY0HZ0d004-000/16/00/2017_05_23_16_00_15_5704_2.jpg";

    public static String transformNameToKey(String fileName) {
        StringBuilder key = new StringBuilder();

        if (fileName != null && fileName.length() > 0) {
            String ipcID = fileName.substring(1, fileName.indexOf("/", 2));
            String tempKey = fileName.substring(fileName.lastIndexOf("/"), fileName.lastIndexOf("_")).replace("/", "");
            String prefixName = tempKey.substring(tempKey.lastIndexOf("_") + 1, tempKey.length());
            String timeName = tempKey.substring(2, tempKey.lastIndexOf("_")).replace("_", "");

            StringBuffer prefixNameKey = new StringBuffer();
            prefixNameKey = prefixNameKey.append(prefixName).reverse();
            if (prefixName.length() < 10) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < 10 - prefixName.length(); i++) {
                    stringBuilder.insert(0, "0");
                }
                prefixNameKey.insert(0, stringBuilder);
            }
            key.append(ipcID).append("_").append(timeName).append("_").append(prefixNameKey).append("_00");
        } else {
            key.append(fileName);
        }
        return key.toString();
    }

    private static String key = "3B0383FPAG00883_170926192916_0000000524_01";

    public static String key2pictureFileName(String rowKey) {
        String ipcId = rowKey.substring(0, rowKey.indexOf("_"));
        String timeStr = rowKey.substring(rowKey.indexOf("_") + 1, rowKey.length());
        String year = timeStr.substring(0, 2);
        String month = timeStr.substring(2, 4);
        String day = timeStr.substring(4, 6);
        String hour = timeStr.substring(6, 8);
        String minute = timeStr.substring(8, 10);
        //String second = timeStr.substring(10, 12);

        StringBuilder fileName = new StringBuilder();
        fileName = fileName.append("/opt/ftpserver/").append(ipcId).
                append("/20").append(year).append("/").append(month).append("/").append(day).
                append("/").append(hour).append("/").append(minute).append("/").append(rowKey).append(".jpg");
        return fileName.toString();
    }

    public static String key2jsonFileName(String rowKey) {
        String ipcId = rowKey.substring(0, rowKey.indexOf("_"));
        String timeStr = rowKey.substring(rowKey.indexOf("_") + 1, rowKey.length());
        String year = timeStr.substring(0, 2);
        String month = timeStr.substring(2, 4);
        String day = timeStr.substring(4, 6);
        String hour = timeStr.substring(6, 8);
        String minute = timeStr.substring(8, 10);
        //String second = timeStr.substring(10, 12);

        int type = Integer.parseInt(rowKey.substring(rowKey.lastIndexOf("_") + 1, rowKey.length()));

        StringBuilder fileName = new StringBuilder();
        if (type == 0) {
            fileName = fileName.append("/opt/ftpserver/").append(ipcId).
                    append("/20").append(year).append("/").append(month).append("/").append(day).
                    append("/").append(hour).append("/").append(minute).append("/").append(rowKey).append(".json");
        } else if (type > 0) {
            //StringBuilder key = new StringBuilder();
            //key = key.append(rowKey.substring(0, rowKey.lastIndexOf("_"))).append("_00");
            rowKey = rowKey.substring(0, rowKey.lastIndexOf("_") + 1) + "00";
            fileName = fileName.append("/opt/ftpserver/").append(ipcId).
                    append("/20").append(year).append("/").append(month).append("/").append(day).
                    append("/").append(hour).append("/").append(minute).append("/").append(rowKey).append(".json");
        } else {
            LOG.warn("rowkey format error" + rowKey);
        }
        return fileName.toString();
    }

    public static void main(String[] args) {
        System.out.println(key);
        String path = key2pictureFileName(key);
        System.out.println(path);
        String path1 = key2jsonFileName(key);
        System.out.println(path1);
        /*String key = transformNameToKey(fileName);
        System.out.println(key);*/
    }

}
