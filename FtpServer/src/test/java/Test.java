import com.hzgc.util.FileUtil;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) {
        getUsefulIP();
    }

    public static String getUsefulIP() {
        Map<String, Integer> ipConnMap = new LinkedHashMap();
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String ipAddr = props.getProperty("ip");
        String[] ipAddressArr = ipAddr.split(",");
        int port = Integer.parseInt(props.getProperty("port"));
        String username = props.getProperty("user");
        String password = props.getProperty("password");
        for (String ip : ipAddressArr) {
            FTPClient client = new FTPClient();
            try {
                client.connect(ip, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                client.login(username, password);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                client.sendCommand("SITE STAT");
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] siteReplies = client.getReplyString().split("\r\n");
            String[] currentConnNum = siteReplies[13].split(":");
            int connectionNum = Integer.parseInt(currentConnNum[1].trim());
            /**
             "File Upload Number       : 2", siteReplies[2]));
             "File Download Number     : 1", siteReplies[3]));
             "File Delete Number       : 1", siteReplies[4]));
             "File Upload Bytes        : 16", siteReplies[5]));
             "File Download Bytes      : 8", siteReplies[6]));
             "Directory Create Number  : 2", siteReplies[7]));
             "Directory Remove Number  : 1", siteReplies[8]));
             "Current Logins           : 2", siteReplies[9]));
             ("Total Logins             : 3", siteReplies[10]));
             ("Current Anonymous Logins : 1", siteReplies[11]));
             ("Total Anonymous Logins   : 1", siteReplies[12]));
             ("Current Connections      : 2", siteReplies[13]));
             ("200 Total Connections        : 3", siteReplies[14]));
             */
            System.out.println("当前IP地址：" + ip + "连接数：" + connectionNum);
           /* System.out.println(currentConnNum[1].trim());*/
            ipConnMap.put(ip, connectionNum);
        }
        return sortByValue(ipConnMap).entrySet().iterator().next().getKey();
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();
        st.sorted(Comparator.comparing(Map.Entry::getValue)).forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }
}
