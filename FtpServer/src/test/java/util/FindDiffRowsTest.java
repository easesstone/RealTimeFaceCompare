package util;

import com.hzgc.collect.expand.meger.FileUtil;
import com.hzgc.collect.expand.meger.FindDiffRows;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class FindDiffRowsTest {
    private FileUtil fileUtil = new FileUtil();
    private List<String> allList;
    private FindDiffRows findDiffRows = new FindDiffRows();
    private List<String> allDiffRows;

    @Before
    public void init() {
        allList = fileUtil.getAllContentFromFile(ClassLoader.getSystemResource("receive.log").getPath(),
                ClassLoader.getSystemResource("process.log").getPath());
        allDiffRows = findDiffRows.getAllDiffRows(allList);
    }

    @Test
    public void testGetAllDiffRows() {
        System.out.println();
        System.out.println("all diff rows ----------------------------------");
        System.out.println(allDiffRows.size());
        for (String diff : allDiffRows) {
            System.out.println(diff);
        }
    }

    @Test
    public void testGetNotProRows() {
        System.out.println();
        System.out.println("all not pro rows ----------------------------------");
        List<String> notProRows = findDiffRows.getNotProRows(allDiffRows);
        for (String row : notProRows) {
            System.out.println(row);
        }
    }

    @Test
    public void testGetErrProRows() {
        System.out.println();
        System.out.println("all err rows ----------------------------------");
        List<String> errProRows = findDiffRows.getErrProRows(allDiffRows);
        for (String row : errProRows) {
            System.out.println(row);
        }
    }

}
