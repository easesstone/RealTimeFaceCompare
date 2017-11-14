import com.hzgc.ftpserver.local.FileType;
import com.hzgc.ftpserver.util.FtpUtil;

import java.util.Map;

public class FtpUtilTest {
    public static void main(String[] args) {
        String key = "DS-2DE72XYZIW-ABCVS20160823CCCH_171109111223_1844_1_s105";
        String filePath = "/DS-2DE72XYZIW-ABCVS20160823CCCH/2017_11_09_11_12_23_1844_1.jpg";
        String url = "ftp://192.168.1.28:2121/3B0383FPAG00883/2017/05/23/16/2017_05_23_16_00_15_5704_1.jpg";

        System.out.println("----------------------------key2absolutePath------------------------------");
        String picPath = FtpUtil.key2absolutePath(key, FileType.PICTURE);
        System.out.println("picture  path : " + picPath);
        String facePath = FtpUtil.key2absolutePath(key, FileType.FACE);
        System.out.println("face     Path : " + facePath);
        String jsonPath = FtpUtil.key2absolutePath(key, FileType.JSON);
        System.out.println("json     Path : " + jsonPath);
        System.out.println("---------------------------------------------------------------------------");

        System.out.println("----------------------------filePath2absolutePath--------------------------");
        String path = FtpUtil.filePath2absolutePath(filePath);
        System.out.println("absolute path : " + path);
        System.out.println("---------------------------------------------------------------------------");

        System.out.println("----------------------------key2relativePath-------------------------------");
        String relativePath = FtpUtil.key2relativePath(key);
        System.out.println("relative Path : " + relativePath);
        System.out.println("---------------------------------------------------------------------------");

        System.out.println("----------------------------key2fileName-----------------------------------");
        String picName = FtpUtil.key2fileName(key, FileType.PICTURE);
        System.out.println("picture  Name : " + picName);
        String faceName = FtpUtil.key2fileName(key, FileType.FACE);
        System.out.println("face     Name : " + faceName);
        String jsonName = FtpUtil.key2fileName(key, FileType.JSON);
        System.out.println("json     Name : " + jsonName);
        System.out.println("---------------------------------------------------------------------------");

        System.out.println("----------------------------getFtpPathMessage------------------------------");
        Map<String, String> map = FtpUtil.getFtpPathMessage(filePath);
        System.out.println(map.toString());
        System.out.println("---------------------------------------------------------------------------");

    }
}
