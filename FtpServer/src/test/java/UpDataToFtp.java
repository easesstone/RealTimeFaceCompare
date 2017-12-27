import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.hzgc.ftpserver.common.Download;
import com.hzgc.util.common.UuidUtil;

import java.io.File;
import java.util.Map;

/**
 * FTP文件上传
 */
public class UpDataToFtp {
    private static MetricRegistry metric = new MetricRegistry();
    private final static Counter counter = metric.counter("counter");


    /**
     * @param path    文件路径
     * @param loopNum 循环次数
     * @param IpcId   设备ID
     */
    public static void upDataTest(String path, int loopNum, String IpcId) {
        File file = new File(path);
        File[] tempList = file.listFiles();
        for (int i = 0; i < loopNum; i++) {
            String uuid = UuidUtil.setUuid();
            for (int j = 0; j < (tempList != null ? tempList.length : 0); j++) {
                StringBuilder filePath = new StringBuilder();
                if (tempList[j].isFile()) {
                    String orginFileName = tempList[j].getAbsolutePath();
                    String fileName = tempList[j].getName();
                    //拼装路径
                    filePath = filePath.append(uuid).append(IpcId).append("/").append(tempList[j].getName().substring(0, 13).replaceAll("_", "/"));
                    long start = System.currentTimeMillis();
                    Download.upLoadFromProduction("172.18.18.136", 2121, "admin", "123456", "", filePath.toString(), fileName, orginFileName);
                    System.out.println("current thread is:[" + Thread.currentThread() + "], start time is:[" + start + "]," +
                            "up time is:[" + (System.currentTimeMillis() - start) + "]");
                    counter.inc();
                    System.out.println(counter.getCount());
                }
            }
        }
        System.out.println("上传的文件数量：" + counter.getCount());
    }
}
