package util;

import com.hzgc.collect.expand.meger.FileFactory;
import org.junit.Test;

import java.util.List;

public class FileFactoryTest {
    private FileFactory fileFactory = new FileFactory("/home/diliang.li/develop");
    @Test
    public void testGetAllProcessFiles() {
        List<String> allFiles  = fileFactory.getAllProcessFiles();
        for(String tmp : allFiles) {
            System.out.println(tmp);
        }
    }
}
