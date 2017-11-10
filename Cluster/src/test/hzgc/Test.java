import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl;

import java.util.List;

public class Test {
    public static void main(String[] args) {

        List<String[]>list = ObjectInfoInnerHandlerImpl.getInstance().getTotalList();
        System.out.println(list);



    }
}
