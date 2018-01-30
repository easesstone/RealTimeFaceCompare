package com.hzgc.collect.expand.meger;

import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.util.JSONHelper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 与文件读写相关的工具类（马燊偲）
 */
public class FileUtil {

    private Logger LOG = Logger.getLogger(FileUtil.class);
    //系统换行符
    private String newLine = System.getProperty("line.separator");

    /**
     * 内部类：filePathVistor，用于对文件进行递归遍历的listAllFileOfDir方法。
     *
     * 使用Files工具类中的walkFileTree()方法可以很容易的实现对目录下的所有文件进行遍历。
     * 这个方法需要一个Path和一个FileVisitor参数。
     * 其中Path是要遍历的路径，而FileVisitor则可以看成的一个文件访问器，它主要提供了四个方法。
     * 这四个方法返回的都是FileVisitResult对象，它是一个枚举类，代表的是返回之后的一些后续的操作。
     *
     * FileVisitResult主要包含四个常见的操作：
     * 1、FileVisitResult.CONTINUE 继续遍历
     * 2、FileVisitResult.TERMINATE 中止访问
     * 3、FileVisitResult.SKIP_SIBLINGS 不访问同级的文件或目录
     * 4、FileVisitResult.SKIP_SUBTREE 不访问子目录
     *
     * 通过创建SimpleFileVisitor对象来对文件进行遍历即可，它是FileVisitor的实现类，这样可以有选择的重写指定的方法。
     */
    private class FilePathVisitor extends SimpleFileVisitor<Path> {

        private List<String> allFileOfDir = new ArrayList<>();

        //访问目录前触发该方法
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        //访问文件时触发该方法。
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!Files.isDirectory(file) && isLogFile(file)) {
                allFileOfDir.add(file.toString());
            }
            return FileVisitResult.CONTINUE;
        }

        List<String> getAllFileOfDir() {
            return allFileOfDir;
        }
    }

    /**
     * 判断文件名是否是“.log”结尾
     *
     * @param dir 文件路径，Path格式
     * @return 文件是否以.log结尾的布尔值
     */
    private boolean isLogFile(Path dir) {
        return dir.getFileName().toString().contains(".log");
    }

    /**
     * NIO扫描得到某个目录下的所有文件的绝对路径的FileList
     *
     * @param path 需扫描的根目录
     * @return 该根目录下所有文件的FileList
     */
    public List<String> listAllFileOfDir(String path) {
        FilePathVisitor FPV = new FilePathVisitor();
        try {
            if (path != null && !Objects.equals(path, "")) {
                //若传入的参数是一个目录
                if (Files.isDirectory(Paths.get(path))) {
                    Files.walkFileTree(Paths.get(path), FPV); //用NIO对path目录下的文件进行递归遍历
                } else {
                    LOG.error(path + " is not a directory!");
                }
            } else {
                LOG.error("The parameter is null!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FPV.getAllFileOfDir();
    }

    /**
     * NIO读取两个文件，把两个文件的每行内容导入到一个List
     *
     * @param filePaths 两个文件的路径
     * @return 包含两个文件中所有内容的List
     */
    public List<String> getAllContentFromFile(String... filePaths) {
        //最终添加到的List
        List<String> allContentList = new ArrayList<>();
        try {
            //入参不为空
            if (filePaths != null) { // if-A start
                //记录入参中符合文件路径格式的参数个数（入参可能为文件夹路径）
                int count = 0;
                for (String filePath : filePaths) {
                    //入参均不为空，再执行以下操作
                    if (!Objects.equals(filePath, "")) { // if-B start
                        File file = new File(filePath);
                        //判断入参类型是文件的个数
                        if (file.isFile()) {
                            count++;
                        }
                        //若入参都是文件的绝对路径
                        if (count == filePaths.length) { // if-C start
                            //先将每个文件的内容，导入到各自的allContentList_i中
                            List<String> allContentList_i = Files.readAllLines(Paths.get(filePath));
                            //先将每个allContentList_i，导入到最终的allContentList中
                            allContentList.addAll(allContentList_i);
                        } else {
                            LOG.error("The parameter must be file!");
                        } // if-C end
                    } else {
                        LOG.error("The parameter is empty!");
                    } // if-B end
                }
            } else {
                LOG.error("The parameter is null!");
            }// if-A end
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allContentList;
    }

    /**
     * 删除文件
     *
     * @param filePaths 要删除的文件的路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String... filePaths) {
        //用于标记删除文件成功的个数
        int flag = 0;
        //用于标记删除文件是否成功，默认为false
        boolean deleteSuccess = false;
        //判断入参是否为Null
        if (filePaths != null) { //if-A start
            for (String filePath : filePaths) {
                //传入的每个参数都不为空
                if (!Objects.equals(filePath, "")) { //if-B start
                    File file = new File(filePath);
                    //判断文件是否存在、类型是否是文件
                    if (file.exists() && file.isFile()) { //if-C start
                        if (file.delete()){
                            flag++;
                        }
                    } else {
                        LOG.error("The file " + file + " is not a file absolute path or not exists, delete failed!");
                    } //if-C end
                    //文件删除成功的个数（标记的flag）是否等于传入的文件的个数
                    if(flag == filePaths.length) {
                        deleteSuccess = true;
                    }
                }
                else {
                    LOG.error("The parameter is empty!");
                } //if-B end
            }
        } else {
            LOG.error("The parameter is NULL!");
        } //if-A end
        return deleteSuccess;
    }


    /**
     * 根据文件路径判断文件是否存在
     *
     * @param filePath 需判断的文件所在路径
     * @return 文件是否存在
     */
    public boolean isFileExist(String filePath) {
        //若输入为空
        if (filePath == null || Objects.equals(filePath, "")) {
            return false;
        } else { //输入不为空，文件为目录
            File file = new File(filePath);
            //输入为目录或文件不存在
            return file.isFile() && file.exists();
        }
    }

    /**
     * 根据要处理的process文件路径，得到对应的receive的文件路径。
     * process文件路径：/opt/logdata/process/p-0/0000000000001.log
     * receive文件路径：/opt/logdata/receive/r-0/0000000000001.log
     * 拼接receive文件路径：根据process文件路径，将其“process/p”部分，替换为“receive/r”
     */
    public String getRecFileFromProFile(String processFilePath) {
        String receiveFilePath = "";
        if (processFilePath != null && !Objects.equals(processFilePath, "")) {
            File file = new File(processFilePath);
            if (file.isFile() && processFilePath.contains("process/p-")) {
                //截取：-0/0000000000001.log部分
                String subStrEnd = processFilePath.substring(processFilePath.lastIndexOf("-"));
                //截取：.../opt/logdata/部分
                String subStrStart = processFilePath.replace(processFilePath.substring(processFilePath.indexOf("process")), "");
                String subStrReceive = "receive/r";
                receiveFilePath = subStrStart + subStrReceive + subStrEnd;
            } else {
                LOG.error("The processFilePath is not correct!");
            }
        } else {
            LOG.error("The processFilePath is null!");
        }
        return receiveFilePath;
    }


    /**
     * 根据receiveFile的文件路径：/opt/logdata/receive/r-0/000000000001.log
     * 获取对应mergeReceiveFile的文件路径：/opt/logdata/merge/receive/r-0/000000000001.log
     */
    public String getMergeRecFilePath(String receiveFile) {
        String mergeRecFilePath = "";
        if (receiveFile != null && !Objects.equals(receiveFile, "")) {
            File file = new File(receiveFile);
            if (file.isFile() && receiveFile.contains("receive/r-")) {
                String subStrEnd = receiveFile.substring(receiveFile.indexOf("/receive"));
                String subStrStart = receiveFile.replace(subStrEnd, "");
                String subStrMerge = "/merge";
                mergeRecFilePath = subStrStart + subStrMerge + subStrEnd;
            } else {
                LOG.error("The receiveFile path is not correct!");
            }
        } else {
            LOG.error("The receiveFile is null!");
        }
        return mergeRecFilePath;
    }

    /**
     * 根据processFile的日志路径：/opt/logdata/process/p-0/000000000001.log
     * 获取对应mergeProcessFile的日志路径：/opt/logdata/merge/process/p-0/000000000001.log
     */
    public String getMergeProFilePath(String processFile) {
        String mergeProFilePath = "";
        if (processFile != null && !Objects.equals(processFile, "")) {
            File file = new File(processFile);
            if (file.isFile() && processFile.contains("process/p-")) {
                String subStrEnd = processFile.substring(processFile.indexOf("/process"));
                String subStrStart = processFile.replace(subStrEnd, "");
                String subStrMerge = "/merge";
                mergeProFilePath = subStrStart + subStrMerge + subStrEnd;
            } else {
                LOG.error("The processFilePath is not correct!");
            }
        } else {
            LOG.error("The processFile path is null!");
        }
        return mergeProFilePath;
    }


    /**
     * 以追加的方式写日志（参照AbstractLogWrite类中的action方法，但多了一个保存目录的入参）
     * 把errProFiles中的每一条数据，保存到/opt/logdata/merge/receive（或process）目录下对应的文件名中
     *
     * @param event         errProFiles中的每一条数据（LogEvent格式）
     * @param mergeFilePath 要保存到的文件的绝对路径
     */
    public void writeMergeFile(LogEvent event, String mergeFilePath) {
        FileWriter fw = null;
        File mergeFile = new File(mergeFilePath);
        //获取入参文件绝对路径的文件夹路径
        File folderPath = new File(getFolderPathFromFile(mergeFilePath));
        try {
            //若文件夹路径不存在，先创建
            if (!folderPath.exists()) {
                folderPath.mkdirs();
            }
            //若日志文件不存在，先创建
            if (!mergeFile.exists()) {
                mergeFile.createNewFile();
            }
            //用File对象构造FileWriter，如果第二个参数为true，表示以追加的方式写数据，从文件尾部开始写起
            fw = new FileWriter(mergeFilePath, true);
            fw.write(JSONHelper.toJson(event));
            fw.write(newLine);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据文件路径，获取这个文件所在文件夹路径
     * 例如根据/opt/logdata/process/p-0/000000000001.log，获得/opt/logdata/process/p-0/
     *
     * @param filePath 文件路径
     * @return 文件所在文件夹路径
     */
    private String getFolderPathFromFile(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf("\\"));
        return filePath.replace(fileName, "");
    }


}
