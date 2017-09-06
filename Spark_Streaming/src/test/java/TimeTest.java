import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl;
import org.apache.ftpserver.command.impl.SYST;
import org.elasticsearch.common.geo.builders.LineStringBuilder;
import scala.Tuple4;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeTest {
    public static void main(String[] args) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = df.format(new Date());
        System.out.println(dateStr);
        String str = "";
        for (int i = 0; i < 50000; i++) {

            str = str + "aaa";

        }
        String dateStr2 = df.format(new Date());
        System.out.println(dateStr2);




    }
}
