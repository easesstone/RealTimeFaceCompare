package com.hzgc.collect.expand.merge;

import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.util.JSONHelper;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class MergeUtilTest {

    private MergeUtil mergeUtil = new MergeUtil();
    private Logger LOG = Logger.getLogger(MergeUtilTest.class);
    private String testDir = "/home/test/ftp";

    //get log dir from CommonConf
    private CommonConf commonConf = new CommonConf();
    private String processDir = commonConf.getProcessLogDir();
    private String receiveDir = commonConf.getReceiveLogDir();
    private String successDir = commonConf.getSuccessLogDir();
    private String writingLogFile = commonConf.getLogName();

    private String mergeErrorDir = commonConf.getMergeLogDir() + "/error";

    private String processFile = processDir + File.separator + "p-0/0000000000000000010.log";
    private String receiveFile = receiveDir + "/r-0/0000000000000000010.log";
    private String notExistFile = receiveDir + "/r-0/notExistFile.log";
    private String errorFile = processDir + "/p-0/error/error.log";

    /**
     * 辅助方法，循环删除文件或者整个目录
     * @param path 文件或者目录
     */
    private void deleteFile(String path){
        deleteFile(new File(path));
    }

    /**
     * 辅助方法，循环删除文件或者整个目录
     * @param file 文件或者目录
     */
    private void deleteFile(File file) {
        if (file.exists() && file.isFile()){
            file.delete();
        }
        if (file.exists() && file.isDirectory()){
            File[] files = file.listFiles();
            //若文件夹下无文件
            if (files == null || files.length == 0){
                file.delete();
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()){
                    deleteFile(files[i]);
                    continue;
                }
                files[i].delete();
            }
        }
    }

    /**
     * 辅助方法：创建文件
     *
     * @param path 文件的绝对路径
     * @throws IOException
     */
    private void createFile(String path) throws IOException {
        createFile(new File(path));
    }

    /**
     * 辅助方法：创建文件
     * @param file 文件对象
     * @throws IOException
     */
    private void createFile(File file) throws IOException {
        if (file.exists()){
            deleteFile(file);
        }
        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
    }

    /**
     * 创建目录
     * @param dir 目录路径
     */
    private void createDir(String dir){
        createDir(new File(dir));
    }

    /**
     * 创建目录
     * @param dir 目录
     */
    private void createDir(File dir){
        if (dir.exists()){
            deleteFile(dir);
        }
        dir.mkdir();
    }


    /**
     * 测试根据文件夹路径，获取文件夹下所有的内容
     *
     * 测试结果：
     * 需要在JDK 1.8中才支持内部类访问本地变量不用改成final，已修改maven中配置。
     */
    @Test
    public void testListAllFileAbsPath() throws IOException {

        LOG.info("根据文件夹路径，获取文件夹下所有文件的绝对路径：");

        //第1组测试
        LOG.info("第1组测试：根据文件夹路径：" + testDir);
        List<String> allFileDir1 = mergeUtil.listAllFileAbsPath(testDir);
        System.out.println(allFileDir1.size());
        for (String file:allFileDir1) {
            System.out.println(file);
        }

        //第2组测试
        LOG.info("第2组测试：" + receiveDir);
        List<String> allFileDir2 = mergeUtil.listAllFileAbsPath(receiveDir);
        System.out.println(allFileDir2.size());
        for (String file:allFileDir2) {
            System.out.println(file);
        }

        //第3组测试，传入Null值
        LOG.info("第3组测试，传入Null值的情况：");
        mergeUtil.listAllFileAbsPath(null);
        mergeUtil.listAllFileAbsPath("");

        //第4组测试，传入文件
        LOG.info("第4组测试，传入文件的情况：");
        mergeUtil.listAllFileAbsPath(processFile);
    }


    /**
     * 传入某个路径
     * 得到所有错误日志/process/p-N/error/error.log绝对路径的FileList
     */
    @Test
    public void listAllErrorLogAbsPathTest(){
        LOG.info("根据某个目录，获取所有错误日志/process/p-N/error/error.log绝对路径的FileList");

        //第1组测试
        LOG.info("第1组测试，传入process目录：" + processDir +"，得到所有error日志：");
        List<String> errorList1 = mergeUtil.listAllErrorLogAbsPath(processDir);
        for (String file:errorList1) {
            System.out.println(file);
        }

        //第2组测试
        LOG.info("第2组测试，传入receive目录的情况：");
        List<String> errorList2 = mergeUtil.listAllErrorLogAbsPath(receiveDir);
        for (String file:errorList2) {
            System.out.println(file);
        }

        //第3组测试
        LOG.info("第3组测试，传入文件的情况：");
        mergeUtil.listAllErrorLogAbsPath(processFile);

        //第4组测试
        LOG.info("第4组测试，传入null的情况：");
        mergeUtil.listAllErrorLogAbsPath(null);
        mergeUtil.listAllErrorLogAbsPath("");
    }

    /**
     * 得到所有process日志/process/p-N/..绝对路径的FileList
     * 除去每一个/p-N下的000000.log和最大文件
     *
     * 测试结果：
     * 遍历结果时，会有包含error日志的情况，已修改。
     */
    @Test
    public void listAllBackupLogAbsPathTest() {
        LOG.info("得到所有process日志/process/p-N/..绝对路径的FileList");

        //第1组测试
        LOG.info("第1组测试，传入process目录" + processDir + "的情况：");
        List<String> executableList1 = mergeUtil.listAllBackupLogAbsPath(processDir, writingLogFile);
        for (String file : executableList1) {
            System.out.println(file);
        }

        //第2组测试
        LOG.info("第2组测试，传入receive目录" + receiveDir + "的情况：");
        List<String> executableList2 = mergeUtil.listAllBackupLogAbsPath(receiveDir, writingLogFile);
        for (String file:executableList2) {
            System.out.println(file);
        }

        //第3组测试
        LOG.info("第3组测试，传入文件的情况：");
        mergeUtil.listAllBackupLogAbsPath(processFile, writingLogFile);

        //第4组测试
        LOG.info("第4组测试，传入null的情况：");
        mergeUtil.listAllBackupLogAbsPath(null, writingLogFile);
        mergeUtil.listAllBackupLogAbsPath("",writingLogFile);

        //第5组测试：入参的路径中，文件名有不是整数的情况

    }


    /**
     * 得到两个文件全部内容的一个List
     *
     * 测出结果：
     * 1、入参为两个文件时，只能得到一个文件的内容
     * 2、入参有一个为null时，报空指针错误
     */
    @Test
    public void getAllContentFromFileTest(){
        LOG.info("得到两个文件全部内容的一个List：");

        //第1组测试
        LOG.info("第1组测试，传入processFile：" + processFile +"\n" + "和receiveFile路径：" + receiveFile);
        List<String> allContent = mergeUtil.getAllContentFromFile(receiveFile, processFile);
        for (String row:allContent) {
            System.out.println(row);
        }
        LOG.info("第1组测试：获取的文件数量是" + allContent.size());

        //第2组测试
        LOG.info("第2组测试，传入1个文件，1个文件夹：");
        mergeUtil.getAllContentFromFile(processFile, processDir);

        //第3组测试
        LOG.info("第3组测试，入参中有1个不存在的文件：");
        mergeUtil.getAllContentFromFile(processFile, notExistFile);

        //第4组测试
        LOG.info("第4组测试，传入null值：");
        mergeUtil.getAllContentFromFile(null, processFile);
        mergeUtil.getAllContentFromFile(null);
        mergeUtil.getAllContentFromFile("",processFile);
    }

    /**
     * 循环删除目录或文件
     *
     * 测试结果：测试正确
     */
    @Test
    public void deleteFileTest() throws IOException {
        LOG.info("循环删除目录或文件：");
        String deleteFile1 = testDir + File.separator + "" + "test1.log";
        createFile(deleteFile1);
        String deleteFolder = testDir + File.separator + "" + "deleteTest";
        createDir(deleteFolder);

        //第1组测试
        LOG.info("第1组测试，入参为文件：" + deleteFile1);
        mergeUtil.deleteFile(deleteFile1);
        assertFalse("删除是否成功",new File(deleteFile1).exists());

        //第2组测试
        LOG.info("第2组测试，入参为空");
        mergeUtil.deleteFile("");

        //第3组测试
        LOG.info("第3组测试，入参为文件夹");
        mergeUtil.deleteFile(deleteFolder);

        //第4组测试
        LOG.info("第4组测试，入参是不存在的文件");
        mergeUtil.deleteFile(deleteFile1);

        //第5组测试
        LOG.info("第5组测试，入参是不存在的文件夹");
        mergeUtil.deleteFile(deleteFolder);
    }

    /**
     * 根据文件路径判断文件是否存在
     */
    @Test
    public void isFileExistTest(){
        String deleteFile1 = testDir +File.separator + "delete_1.log";
        LOG.info("文件是否存在：");
        //第1组测试
        LOG.info("第1组测试，入参为文件：" + processFile);
        System.out.println(mergeUtil.isFileExist(processFile));

        //第2组测试
        LOG.info("第2组测试，入参为空");
        System.out.println(mergeUtil.isFileExist(""));
        System.out.println(mergeUtil.isFileExist(null));

        //第3组测试
        LOG.info("第3组测试，入参为文件夹");
        System.out.println(mergeUtil.isFileExist(processDir));

        //第4组测试
        LOG.info("第4组测试，入参是不存在的文件");
        System.out.println(mergeUtil.isFileExist(deleteFile1));

    }

    /**
     * 一行一行写日志到merge的某个目录下
     * @throws IOException
     */
    @Test
    public void writeMergeFileTest() throws IOException {
        LOG.info("一行一行写日志到merge的某个目录下");

        //第1组测试：入参为不存在的文件绝对路径
        List<String> content = mergeUtil.getAllContentFromFile(processFile);
        for (String dir : content) {
            LogEvent event = JSONHelper.toObject(dir, LogEvent.class);
            mergeUtil.writeMergeFile(event, notExistFile);
        }

        //第2组测试：入参为父目录不存在的文件绝对路径
        String notExistMergeFile = mergeErrorDir + File.separator + "error" +File.separator + "error.log";
        List<String> content2 = mergeUtil.getAllContentFromFile(processFile);
        for (String dir : content2) {
            LogEvent event = JSONHelper.toObject(dir, LogEvent.class);
            mergeUtil.writeMergeFile(event, notExistMergeFile);
        }

        //第3组测试：入参为null或空值
        List<String> content3 = mergeUtil.getAllContentFromFile(processFile);
        for (String dir : content3) {
            LogEvent event = JSONHelper.toObject(dir, LogEvent.class);
            mergeUtil.writeMergeFile(event, "");
        }

        /*
         * 测试4.1：将process目录下所有error日志，写入到merge/error中
         */
        //列出process目录下所有error日志路径
        List<String> allErrorDir = mergeUtil.listAllErrorLogAbsPath(processDir);
        System.out.println("获取allErrorDir：");
        System.out.println(allErrorDir);

        for (String dir : allErrorDir) {
            //获取process/error对应的merge/error路径
            String mergeErrorFile = mergeUtil.getMergeFilePath(dir);
            System.out.println(mergeErrorFile);
            //获取其中每一行数据
            List<String> errorRows = mergeUtil.getAllContentFromFile(dir);
            //判断errorRows是否为空，若不为空，则需要处理出错数据
            if (errorRows != null && errorRows.size() != 0) {
                for (String row : errorRows) {
                    //用JSONHelper将某行数据转化为LogEvent格式
                    LogEvent event = JSONHelper.toObject(row, LogEvent.class);
                    //将错误日志写入对应的/merge/error/中
                    mergeUtil.writeMergeFile(event, mergeErrorFile);
                }
            }
        }

        /*
         * 测试4.2：将merge/error目录下所有error日志，重新写入到merge/error中
         */
        //列出process目录下所有error日志路径
        List<String> allErrorDir2 = mergeUtil.listAllFileAbsPath(mergeErrorDir);
        System.out.println("获取allErrorDir2：");
        System.out.println(allErrorDir2);

        for (String dir : allErrorDir2) {
            //获取process/error对应的merge/error路径
            String mergeErrFileNew = dir.replace(".log","")+"-N"+".log";
            System.out.println(mergeErrFileNew);
            //获取其中每一行数据
            List<String> errorRows = mergeUtil.getAllContentFromFile(dir);
            //判断errorRows是否为空，若不为空，则需要处理出错数据
            if (errorRows != null && errorRows.size() != 0) {
                for (String row : errorRows) {
                    //用JSONHelper将某行数据转化为LogEvent格式
                    LogEvent event = JSONHelper.toObject(row, LogEvent.class);
                    //将错误日志写入对应的/merge/error/中
                    mergeUtil.writeMergeFile(event, mergeErrFileNew);
                }
            }
        }

    }


    /**
     * 移动文件
     *
     * 测试正确。
     */
    @Test
    public void moveFileTest() throws IOException {
        String moveFrom = mergeErrorDir +File.separator + "moveFrom" +File.separator +"error.log";
        String moveTo = mergeErrorDir +File.separator + "moveTo" +File.separator +"error1.log";

        //第1组测试
        LOG.info("第1组测试：源文件路径存在，目标文件路径不存在");
        createFile(moveFrom);
        mergeUtil.moveFile(moveFrom, moveTo);
        assertTrue("移动成功", new File(moveTo).exists());

        //第2组测试
        LOG.info("源文件路径不存在");
        mergeUtil.moveFile(moveFrom, moveTo);

        //第3组测试
        LOG.info("有一个为null或空值");
        mergeUtil.moveFile(null, moveTo);

        //第4组测试
        LOG.info("有一个是文件夹路径");
        mergeUtil.moveFile(moveFrom, processDir);

        //第5组测试
        LOG.info("源文件路径存在，目标文件的父目录不存在");
        mergeUtil.moveFile(moveFrom, moveTo);

        //第6组测试
        LOG.info("第6组测试：源文件路径存在，目标文件路径存在（是否会覆盖目标文件）");
        mergeUtil.moveFile(moveFrom, moveTo);

    }

    /**
     * 复制文件
     */
    @Test
    public void copyFileTest() {
        String moveFrom = mergeErrorDir +File.separator + "moveFrom" +File.separator +"error.log";
        String moveTo = mergeErrorDir +File.separator + "moveTo" +File.separator +"error1.log";

        //第1组测试
        LOG.info("第1组测试：源文件路径存在，目标文件路径存在");
        mergeUtil.copyFile(moveFrom, moveTo);
    }

    /**
     * 根据processFile路径，获取对应的receiveFile路径
     *
     * 测试正确。
     */
    @Test
    public void getRecFilePathFromProFileTest(){
        LOG.info("根据processFile路径，获取对应的receiveFile路径：");

        //第1组测试
        LOG.info("第1组测试，传入processFile路径：" + processFile +"，得到receive路径：");
        String receiveFile = mergeUtil.getRecFilePathFromProFile(processFile);
        System.out.println(receiveFile);

        //第2组测试，传入Null值
        LOG.info("第2组测试，传入Null值的情况：");
        mergeUtil.getRecFilePathFromProFile(null);
        mergeUtil.getRecFilePathFromProFile(processFile);

        //第3组测试，传入文件夹
        LOG.info("第3组测试，传入文件夹的情况：");
        mergeUtil.getRecFilePathFromProFile(processDir);

        //第4组测试，传入receive路径
        LOG.info("第4组测试，传入receive路径的情况：");
        mergeUtil.getRecFilePathFromProFile(receiveDir);
    }

    /**
     * 根据processFile路径，获取对应的errorFile路径
     *
     * 测试结果：路径的拼接缺少“/”（data/process/p-0error.log）
     * 已修改
     */
    @Test
    public void getErrFilePathFromProFileTest(){
        LOG.info("根据processFile路径，获取对应的errorFile路径：");

        //第1组测试
        LOG.info("第1组测试，传入processFile路径：" + processFile +"，得到error路径：");
        String errorFile = mergeUtil.getErrFilePathFromProFile(processFile);
        System.out.println(errorFile);

        //第2组测试，传入Null值
        LOG.info("第2组测试，传入Null值的情况：");
        mergeUtil.getErrFilePathFromProFile(null);
        mergeUtil.getErrFilePathFromProFile("");

        //第3组测试，传入文件夹
        LOG.info("第3组测试，传入文件夹的情况：");
        mergeUtil.getErrFilePathFromProFile(processDir);

        //第4组测试，传入receive路径
        LOG.info("第4组测试，传入receive路径的情况：");
        mergeUtil.getErrFilePathFromProFile(receiveDir);

    }

    /**
     * 根据processFile/ReceiveFile路径（包括error.log），获取对应的mergeFile路径
     *
     * 测试结果：1、路径拼接不正确，已修改。
     * 2、error日志重命名时，去掉名字中含有的空格。
     */
    @Test
    public void getMergeFilePathTest(){
        LOG.info("根据processFile/receiveFile路径，获取对应的mergeFile路径：");

        //第1组测试
        LOG.info("第1组测试，传入processFile路径：" + processFile +"，得到mergeFile路径：");
        String mergeFile1 = mergeUtil.getMergeFilePath(processFile);
        System.out.println(mergeFile1);

        //第2组测试
        LOG.info("第2组测试，传入receiveFile路径：" + receiveFile +"，得到mergeFile路径：");
        String mergeFile2 = mergeUtil.getMergeFilePath(receiveFile);
        System.out.println(mergeFile2);

        //第3组测试
        LOG.info("第3组测试，传入errorFile路径：" + errorFile +"，得到mergeFile路径：");
        String mergeFile3 = mergeUtil.getMergeFilePath(errorFile);
        System.out.println(mergeFile3);

        //第4组测试，传入Null值
        LOG.info("第4组测试，传入Null值的情况：");
        mergeUtil.getMergeFilePath(null);
        mergeUtil.getMergeFilePath("");

        //第5组测试，传入文件夹
        LOG.info("第5组测试，传入文件夹的情况：");
        mergeUtil.getMergeFilePath(processDir);

    }


    /**
     * 根据processFile/ReceiveFile路径（包括error.log），获取对应的successProcessFile路径
     *
     * 测试正确。
     */
    @Test
    public void getSuccessFilePathTest(){
        LOG.info("根据processFile/receiveFile路径，获取对应的successFile路径：");

        //第1组测试
        LOG.info("第1组测试，传入processFile路径：" + processFile +"，得到successFile路径：");
        String successFile1 = mergeUtil.getSuccessFilePath(processFile);
        System.out.println(successFile1);

        //第2组测试
        LOG.info("第2组测试，传入receiveFile路径：" + receiveFile +"，得到successFile路径：");
        String successFile2 = mergeUtil.getSuccessFilePath(receiveFile);
        System.out.println(successFile2);

        //第3组测试
        LOG.info("第3组测试，传入errorFile路径：" + errorFile +"，得到successFile路径：");
        String successFile3 = mergeUtil.getSuccessFilePath(errorFile);
        System.out.println(successFile3);

        //第4组测试，传入Null值
        LOG.info("第4组测试，传入Null值的情况：");
        mergeUtil.getSuccessFilePath(null);
        mergeUtil.getSuccessFilePath("");

        //第5组测试，传入文件夹
        LOG.info("第5组测试，传入文件夹的情况：");
        mergeUtil.getSuccessFilePath(processDir);

    }


    /**
     * 获取锁，处理错误日志
     */
    @Test
    public void lockAndMoveTest() throws IOException {

        String moveFrom = mergeErrorDir +File.separator + "moveFrom" +File.separator +"error.log";
        String moveTo = mergeErrorDir +File.separator + "moveTo" +File.separator +"error1.log";
        createFile(moveFrom);

        //第1组测试
        LOG.info("第1组测试：源文件路径存在，目标文件路径不存在");
        mergeUtil.lockAndMove(moveFrom,moveTo);

        //第2组测试
        LOG.info("第2组测试：源文件路径存在，目标文件路径不存在");
        mergeUtil.lockAndMove(moveFrom,moveTo);
        //第3组测试
        LOG.info("第3组测试：入参有一个是文件夹路径");
        mergeUtil.lockAndMove(processDir,moveTo);
        mergeUtil.lockAndMove(moveFrom, processDir);

        //第4组测试
        LOG.info("第4组测试：入参有一个是null或空值");
        mergeUtil.lockAndMove(null, processFile);
        mergeUtil.lockAndMove(processFile, "");

        //第5组测试
        LOG.info("第5组测试：源文件路径存在，目标文件的父目录不存在");
        mergeUtil.lockAndMove(moveFrom, moveTo);

        //第6组测试
        LOG.info("第6组测试：源文件路径存在，目标文件路径存在（测试是否会覆盖目标文件）");
        mergeUtil.lockAndMove(moveFrom, moveTo);

        //第7组测试
        LOG.info("第7组测试：测试另外的程序在对源文件写入时，是否能移动成功");
        mergeUtil.lockAndMove(moveFrom, moveTo);


        //第8组测试：测试是否能将process目录下所有error日志再获取锁的情况下，移动到正确的success和merge目录
        //列出process目录下所有error日志路径
        List<String> allErrorDir = mergeUtil.listAllErrorLogAbsPath(processDir);
        for (String errFile:allErrorDir) {
            //获取每个error.log需要移动到的success和merge目录下的路径
            String successErrFile = mergeUtil.getSuccessFilePath(errFile);
            String mergeErrFile = mergeUtil.getMergeFilePath(errFile);
            System.out.println("errFile" + errFile);
            System.out.println("successErrFile：" + successErrFile);
            System.out.println("mergeErrFile：" + mergeErrFile);
            //移动到merge后，拷贝一份到success
            mergeUtil.lockAndMove(errFile, mergeErrFile); //其中包括判断锁是否存在
            mergeUtil.copyFile(mergeErrFile, successErrFile);
        }
    }


}
