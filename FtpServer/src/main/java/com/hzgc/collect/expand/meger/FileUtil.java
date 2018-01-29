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


/**
 * 与文件读取相关的工具类（马燊偲）
 *
 */
public class FileUtil {

    private Logger LOG = Logger.getLogger(FileUtil.class);
    private List<String> allFileOfDir = new ArrayList<>();
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
    private class FilePathVistor extends SimpleFileVisitor<Path>{

        //访问目录前触发该方法
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (Files.isDirectory(dir)){
                LOG.info("You are visiting directory" + dir +" ...");
            }
            return FileVisitResult.CONTINUE;
        }

        //访问文件时触发该方法。
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!Files.isDirectory(file) && isLogFile(file)){
                allFileOfDir.add(file.toString());
            }
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * 判断文件名是否是“.log”结尾
     * @param dir
     * @return
     */
    private boolean isLogFile(Path dir){
        return dir.getFileName().toString().contains(".log");
    }


    /**
     * NIO扫描得到某个目录下的所有文件的绝对路径的FileList
     * @param path 需扫描的根目录
     * @return 该根目录下所有文件的FileList
     */
    public List<String> listAllFileOfDir(String path){
        FilePathVistor FPV = new FilePathVistor();
        try {
            //若传入的参数是一个目录
            if(Files.isDirectory(Paths.get(path))){
                Files.walkFileTree(Paths.get(path), FPV) ; //用NIO对path目录下的文件进行递归遍历
            }
            else {
                LOG.error(path + " is not a directory!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return allFileOfDir;
    }

    /**
     * NIO读取两个文件，把两个文件的每行内容导入到一个List
     * @param filePaths 两个文件的路径
     * @return 包含两个文件中所有内容的List
     */
    public List<String> getAllContentFromFile(String ... filePaths){
        //最终添加到的List
        List<String> allContentList = new ArrayList<>();
        try {
            for (int i = 0; i < filePaths.length; i++){
                //先将每个文件的内容，导入到各自的allContentList_i中
                List<String> allContentList_i = Files.readAllLines(Paths. get(filePaths[i]));
                //先将每个allContentList_i，导入到最终的allContentList中
                allContentList.addAll(allContentList_i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allContentList;
    }

    /**
     * 删除文件
     * @param filePaths 要删除的文件的路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String ... filePaths){

        //用于标记删除文件成功的个数
        int flag = 0;
        for (int i = 0; i < filePaths.length; i++){
            File file = new File(filePaths[i]);
            if (file.exists() && file.isFile() && file.delete()){
                flag ++;
            }
            else {
                LOG.error("The file " + file + " is not a file, delete failed!");
            }
        }

        //文件删除成功的个数（标记的flag）是否等于传入的文件的个数
        if (flag == filePaths.length) {
            return true;
        }
        else {
            return false;
        }
    }


    /**
     * 根据文件路径判断文件是否存在
     * @param filePath 需判断的文件所在路径
     * @return 文件是否存在
     */
    public boolean isFileExist(String filePath){
        //若输入为空
        if (filePath == null || filePath == ""){
            return false;
        }
        //输入不为空，文件为目录
        else {
            File file = new File(filePath);
            //输入为目录或文件不存在
            if (!file.isFile() || !file.exists()) {
                return false;
            }
            else{
                return true;
            }
        }
    }

    /**
     * 根据要处理的process文件路径，得到对应的receive的文件路径。
     * process文件路径：/opt/logdata/process/p-0/0000000000001.log
     * receive文件路径：/opt/logdata/receive/r-0/0000000000001.log
     */
    public String getRecFileFromProFile(String processFilePath){
        /*
            substring(int beginIndex, int endIndex)：
            获取字符串中以索引beginIndex开始（包含），endIndex结尾（不含）的部分。
         */

        //拼接receive文件路径：根据process文件路径，将其“process/p”部分，替换为“receive/r”


        //****************************test********************************/
        //本地测试时路径为：D:\Test\opt\logdata\process\p-0
//        String recSubstring1 = processFilePath.substring(processFilePath.indexOf("\\opt"),processFilePath.indexOf("process"));
//        String recSubstring2 = "receive\\r";
        String recSubstring1 = processFilePath.substring(processFilePath.indexOf("/opt"),processFilePath.indexOf("process"));
        String recSubstring2 = "receive/r";
        String recSubstring3 = processFilePath.substring(processFilePath.indexOf("-"));

        String receiveFilePath = recSubstring1 + recSubstring2 + recSubstring3;

        return receiveFilePath;
    }


    /**
     * 根据processFile的文件路径：/opt/logdata/process/p-0/000000000001.log
     * 获取对应mergeReceiveFile的文件路径：/opt/logdata/merge/receive/r-0/000000000001.log
     */
    public String getMergeRecFilePath(String receiveFile){
        String mergeRecSubstring1 = receiveFile.substring(receiveFile.indexOf("/opt/logdata"),receiveFile.indexOf("receive"));
        String mergeRecSubstring2 = "/merge";
        String mergeRecSubstring3 = receiveFile.substring(receiveFile.indexOf("/receive"));

        //****************************test********************************/
        //本地测试时路径为：
        // D:\Test\opt\logdata\process\p-0
        // D:\Test\opt\logdata\receive\p-0
        // D:\Test\opt\logdata\merge\process\p-0
        // D:\Test\opt\logdata\merge\receive\p-0
//        String mergeRecSubstring1 = receiveFile.substring(receiveFile.indexOf("\\opt\\logdata"),receiveFile.indexOf("receive"));
//        String mergeRecSubstring2 = "\\merge";
//        String mergeRecSubstring3 = receiveFile.substring(receiveFile.indexOf("\\receive"));
        String mergeRecFilePath = mergeRecSubstring1 +mergeRecSubstring2 +mergeRecSubstring3;

        return mergeRecFilePath;
    }

    /**
     * 根据processFile的日志路径：/opt/logdata/process/r-0/000000000001.log
     * 获取对应mergeProeiveFile的日志路径：/opt/logdata/merge/process/r-0/000000000001.log
     */
    public String getMergeProFilePath(String processFile){
        String mergeProSubstring1 = processFile.substring(processFile.indexOf("/opt/logdata"),processFile.indexOf("process"));
        String mergeProSubstring2 = "/merge";
        String mergeProSubstring3 = processFile.substring(processFile.indexOf("/process"));
//        String mergeProSubstring1 = processFile.substring(processFile.indexOf("\\opt/logdata"),processFile.indexOf("process"));
//        String mergeProSubstring2 = "\\merge";
//        String mergeProSubstring3 = processFile.substring(processFile.indexOf("\\process"));
        String mergeProFilePath = mergeProSubstring1 +mergeProSubstring2 +mergeProSubstring3;

        return mergeProFilePath;
    }


    /**
     * 以追加的方式写日志
     * 把errProFiles中的每一条数据，保存到/opt/logdata/merge/receive（或process）目录下对应的文件名中
     * @param row errProFiles中的每一条数据
     * @param mergeFilePath 要保存到的文件的绝对路径
     */
    public void writeMergeFile(String row, String mergeFilePath){
        //将每行数据用JSONHelper转化为LogEvent格式
        LogEvent event = JSONHelper.toObject(row, LogEvent.class);

        FileWriter fw = null;
        try {
            //用File对象构造FileWriter，如果第二个参数为true，表示以追加的方式写数据，从文件尾部开始写起
            fw = new FileWriter(mergeFilePath, true);
            fw.write(JSONHelper.toJson(event));
            fw.write(newLine);
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null){
                    fw.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
