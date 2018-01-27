package util;

import com.hzgc.collect.expand.meger.FileUtil;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUtilTest {
    private FileUtil fileUtil = new FileUtil();
    private Logger LOG = Logger.getLogger(FileUtilTest.class);

    private String testDir = "/home/diliang.li/diliang.li.test";

    /**
     * 辅助方法，循环删除文件或者整个目录
     * @param path 文件或者目录
     */
    /**
     * 辅助方法，循环删除文件或者整个目录
     *
     * @param path 文件或者目录
     */
    private void deleteFile(String path) {
        deleteFile(new File(path));
    }

    /**
     * 辅助方法，循环删除文件或者整个目录
     *
     * @param file, 文件或者目录
     */
    private void deleteFile(File file) {
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                file.delete();
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteFile(files[i]);
                    continue;
                }
                files[i].delete();
            }
        }
    }

    /**
     * 辅助方法： 创建文件
     *
     * @param path, 文件的绝对路径
     * @throws IOException
     */
    private void createFile(String path) throws IOException {
        createFile(new File(path));
    }

    /**
     * 辅助方法： 创建文件
     *
     * @param file 文件对象
     * @throws IOException
     */
    private void createFile(File file) throws IOException {
        if (file.exists()) {
            deleteFile(file);
        }
        file.createNewFile();
    }

    /**
     * 创建目录
     *
     * @param dir 目录
     */
    private void createDir(String dir) {
        createDir(new File(dir));
    }

    /**
     * 创建目录
     *
     * @param dir 目录
     */
    private void createDir(File dir) {
        if (dir.exists()) {
            deleteFile(dir);
        }
        dir.mkdir();
    }

    /**
     * 测试开始前的动作
     */
    @Before
    public void initTest() {
        createDir(testDir);
    }


    /**
     * 测试结果： 1，需要添加判空操作 2，需要排序传入是目录的情况，因为判断的是文件是否存在
     *
     * @throws IOException
     */
    @Test
    public void testIsFileExits() throws IOException {
        String currentPath = testDir + File.separator + "testIsFileExits";
        createDir(currentPath);

        String path = currentPath + File.separator + UUID.randomUUID() + ".txt";
        createFile(path);
        // 1,当文件存在的时候
        boolean isFileExits = fileUtil.isFileExist(path);
        LOG.info("当文件存在的时候: " + isFileExits);

        // 2,当传入的是一个目录的时候
        isFileExits = fileUtil.isFileExist(currentPath);
        LOG.info("当传入的是一个目录的时候: " + isFileExits);

        // 3,当传入是第一个空字符串的时候
        isFileExits = fileUtil.isFileExist("");
        LOG.info("当传入是第一个空字符串的时候: " + isFileExits);

        // 4,当传入是Null 的时候
        isFileExits = fileUtil.isFileExist(null);
        LOG.info("当传入是Null 的时候: " + isFileExits);

        // 5,当传入的是一个uuid 的字符串
        path = currentPath + File.separator + UUID.randomUUID() + ".txt";
        isFileExits = fileUtil.isFileExist(path);
        LOG.info("当传入的是一个uuid 的字符串: " + isFileExits);
    }

    /**
     * 测试结果: 没有判空操作
     *
     * @throws IOException
     */
    @Test
    public void testDeleteFile() throws IOException {
        String currentPath = testDir + File.separator + "testDelteFile";
        createDir(currentPath);
        // 准备测试数据
        // 1，第一组测试数据，每个文件都存在
        String[] prepareFile = new String[10];
        for (int i = 1; i <= 10; i++) {
            File file = new File(currentPath + File.separator + i + ".txt");
            prepareFile[i - 1] = currentPath + File.separator + i + ".txt";
            createFile(file);
        }
        LOG.info("1，第一组测试数据，每个文件都存在 " + fileUtil.deleteFile(prepareFile));

        //2，第二组测试数据，12个文件有两个文件不存在
        prepareFile = new String[12];
        for (int i = 1; i <= 12; i++) {
            File file = new File(currentPath + File.separator + i + ".txt");
            prepareFile[i - 1] = currentPath + File.separator + i + ".txt";
            if (i == 11 || i == 12) {
                prepareFile[i - 1] = currentPath + File.separator + i + "1.txt";
            }
            createFile(file);
        }
        LOG.info("第二组测试数据，12个文件有两个文件不存在 " + fileUtil.deleteFile(prepareFile));

        //3，第3组测试数据，12个文件有两个是目录
        prepareFile = new String[12];
        for (int i = 1; i <= 12; i++) {
            File file = new File(currentPath + File.separator + i + ".txt");
            prepareFile[i - 1] = currentPath + File.separator + i + ".txt";
            if (i == 11) {
                prepareFile[i - 1] = currentPath;
            }
            if (i == 12) {
                File file1 = new File(currentPath + File.separator + "demodiliang.li12");
                createDir(file1);
                prepareFile[i - 1] = currentPath + File.separator + "demodiliang.li12";
            }
            createFile(file);
        }
        LOG.info("第3组测试数据，12个文件有两个是目录 " + fileUtil.deleteFile(prepareFile));

        // 4,第四组测试，里面传入Null 值
        LOG.info("第四组测试，里面传入Null 值: " + fileUtil.deleteFile(null));
    }

    /**
     * 测试出的问题：
     * 1，不必要的日记太多
     * 2，逻辑是否有问题，没有问题，只是文件必须以log结尾
     * 3,AllList 应该放在FilePathVistor中
     * 4,没有判空操作
     * test list all file of a dir method
     *
     * @throws IOException
     */
    @Test
    public void testListAllFileOfDir() throws IOException {
        // prepare a dir for test
        String currentDir = testDir + File.separator + "testListALlFileOfDir";
        createDir(currentDir);

        // prepare 100 dirs and 100files
        File file;
        List<String> files = new ArrayList<>();

        //第1组测试：
        for (int i = 0; i < 100; i++) {
            file = new File(currentDir + File.separator + i);
            createDir(file);
            file = new File(currentDir + File.separator + i + File.separator + i + ".txt");
            createFile(file);
        }

        files = fileUtil.listAllFileOfDir(currentDir);
        LOG.info("第1组测试：获取的文件数量的是" + files.size() + ", 是否正确： " + (0 == files.size()));

        //第2组测试：
        for (int i = 0; i < 100; i++) {
            file = new File(currentDir + File.separator + i);
            createDir(file);
            file = new File(currentDir + File.separator + i + File.separator + i + ".log");
            createFile(file);
        }
        files = fileUtil.listAllFileOfDir(currentDir);
        LOG.info("第2组测试：获取的文件数量的是" + files.size() + ", 是否正确： " + (100 == files.size()));


        //第三组测试：
        for (int i = 0; i < 100; i++) {
            file = new File(currentDir + File.separator + i);
            createDir(file);
//            file = new File(currentDir + File.separator + i + File.separator + i + ".txt");
//            createFile(file);
        }
        files = fileUtil.listAllFileOfDir(currentDir);
        LOG.info("第三组测试：获取的文件数量的是" + files.size() + ", 是否正确： " + (0 == files.size()));

        //第四组测试：
        createDir(currentDir);
        files = fileUtil.listAllFileOfDir(currentDir);
        LOG.info("第四组测试：获取的文件数量的是" + files.size() + ", 是否正确： " + (0 == files.size()));

        // 第5组测试
        files = fileUtil.listAllFileOfDir(null);
        LOG.info("第5组测试: 里面传入Null值: " + files.size());
    }

    /**
     * 测试出的问题：
     * 1,没有判空操作
     *
     * @throws IOException
     */
    @Test
    public void testGetALlContent() throws IOException {
        String currentDir = testDir + File.separator + "testGetAllContent";
        createDir(currentDir);
        // prepare Content
        String filePathV1 = currentDir + File.separator + "test01.txt";
        createFile(filePathV1);

        BufferedWriter writer = new BufferedWriter(new FileWriter(filePathV1));
        for (int i = 0; i < 10; i++) {
            writer.write("a string for test in: " + i + "\n");
        }
        writer.close();
        //第1组测试：
        List<String> contentsV1 = fileUtil.getAllContentFromFile();
        LOG.info("第1组测试, 里面什么文件也不传： " + contentsV1.size());


        //第2组测试：
        List<String> contentsV2 = fileUtil.getAllContentFromFile(filePathV1);
        LOG.info("第2组测试, pass a file： " + contentsV2.size()  + " :" + (10 == contentsV2.size()));

        //第3组测试：
        String filePathV3 = currentDir + File.separator + "test01.txt";
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(filePathV3));
        for (int i = 0; i < 10; i++) {
            writer1.write("b string for test in: " + i + "\n");
        }
        writer1.close();
        List<String> contentsV3 = fileUtil.getAllContentFromFile(filePathV1, filePathV3);
        LOG.info("第3组测试, 里面传入two ： " + contentsV3.size());


        //第4组测试：
        List<String> contentsV4 = fileUtil.getAllContentFromFile(null);
        LOG.info("第4组测试, 里面传入Null值： " + contentsV4.size());



    }

}
