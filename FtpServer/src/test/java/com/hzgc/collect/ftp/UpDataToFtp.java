package com.hzgc.collect.ftp;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.hzgc.collect.ftp.util.FTPDownloadUtils;
import org.junit.Test;

import java.io.File;
import java.util.Random;

public class UpDataToFtp {

    //counter：计数器；在需要统计数据的位置调用inc()和dec()方法。
    private static MetricRegistry metricRegistry = new MetricRegistry();
    private final static Counter counter = metricRegistry.counter("counter");


    /**
     *
     * 对于path路径下的所有文件，循环loopNum次
     *
     * @param path 文件路径
     * @param loopNum 循环次数
     * @param IpcId 设备ID
     */
    @Test
    public static void upDataTest(String path, int loopNum, String IpcId){
        File file = new File(path);
        File[] tempList = file.listFiles();
        for (int i = 0; i < loopNum; i++) {
            Random random = new Random();
            int randNum = random.nextInt(10000000);
            String randPathEnd = String.valueOf(randNum);
            for (int j = 0; j < ( tempList != null ? tempList.length : 0); j++) {
                if (tempList[j].isFile()){
                    String originFileName = tempList[j].getAbsolutePath();
                    String fileName = tempList[j].getName();
                    StringBuilder filePath = new StringBuilder();
                    //拼接路径
                    filePath = filePath.append(IpcId).append("/")
                            .append(tempList[j].getName().substring(0, 13)).append(randPathEnd);
                    FTPDownloadUtils.upLoadFromProduction("172.18.18.163", 2121, "admin",
                            "123456", "", filePath.toString(), fileName, originFileName);
                    counter.inc();
                    System.out.println(counter.getCount());
                }
            }
        }
        System.out.println("发送到ftp的图片数量：" + counter.getCount());
    }



}
