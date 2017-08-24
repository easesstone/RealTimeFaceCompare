import com.hzgc.streaming.util.PropertiesUtils;

import java.util.Properties;

public class PropertiesTest {

    public static void main(String[] args) {
        Properties p =  PropertiesUtils.getProperties();
        String str = p.getProperty("job.timeInterval");
        System.out.println(str);



    }
}
