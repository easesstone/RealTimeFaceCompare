package com.hzgc.collect.expand.log;

import com.google.common.base.Optional;
import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.util.JsonHelper;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Random;

/**
 * 此对象为数据接收写日志对象的实例
 */
public class DataReceiveLogWriter extends AbstractLogWrite {
    private static final Logger LOG = Logger.getLogger(DataReceiveLogWriter.class);
    /**
     * 接收队列日志文件大小
     */
    private int receiveLogSize;

    /**
     * 接收队列日志名称
     */
    private String receiveLogName;

    /**
     * 接收队列日志目录
     */
    private String receiveLogDir;

    /**
     * 当前日志文件
     */
    private String currentFile;

    /**
     * 当前日志行号
     */
    private long count = 0;

    /**
     * 系统换行符
     */
    private String newLine;
    /**
     * 路径分隔符
     */
    private String fileSeparator;


    public DataReceiveLogWriter(CommonConf conf, String queueID, long count) {

        super(conf, queueID);
        this.receiveLogDir = conf.getReceiveLogDir();
        this.receiveLogName = conf.getReceiveLogName();
        this.receiveLogSize = conf.getReceiveLogSize();
        this.count = count;
        this.newLine = System.getProperty("line.separator");
        this.fileSeparator = System.getProperty("file.separator");

        this.currentFile = this.receiveLogDir + this.fileSeparator + "receive-" + super.queueID + this.fileSeparator + receiveLogName;
        LOG.info("Init DataReceiveLogWriter successful [" + queueID + ":" + this.queueID
                + ", count:" + count
                + ", receiveLogName:" + this.receiveLogName
                + ", receiveLogSize:" + this.receiveLogSize
                + ", currentFile:" + this.currentFile + "]");
        this.prepare();
    }

    /**
     * 数据写入队列时写日志事件
     *
     * @param event 日志信息
     */
    @Override
    public void writeEvent(LogEvent event) {
        String state = "0";
        count++;
        Path path = Paths.get(this.currentFile);
        JsonHelper.toJson(event);
        Charset charset = Charset.forName("UTF-8");
        ArrayList<String> lines = new ArrayList<>();
        /*StringBuilder logStr = new StringBuilder();
        logStr.append(count).append(" ").append(event.getUrl()).append(" ").append(state);*/
        lines.add(JsonHelper.toJson(event));
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
                LOG.info("create log file:" + this.currentFile);
            }
            Files.write(path, lines, charset, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void prepare() {
        if (count == 0) {
            File file = new File(this.currentFile);
            try {
                if (!file.exists()) {
                    boolean status = file.createNewFile();
                    LOG.info("Current count is " + count + ", create log file:" + this.currentFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回文件的行数
     * @param filePath 文件路径
     * @return line
     */
    private Long getLineNumber(String filePath) {
        File file = new File(filePath);
        LineNumberReader rf=null;
        long lineCount=0L;
        try {
            rf = new LineNumberReader(new FileReader(file));
            lineCount = rf.getLineNumber();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }finally {
            if (rf != null) {
                try {
                    rf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return lineCount;
    }

    /**
     * 快速读取文件最后一行
     *
     * @param file 要读取的文件
     * @return lastLine 文件的最后一行内容
     */
    public String readLastLine(File file) {
        RandomAccessFile raf = null;
        String lastLine = "";
        try {
            raf = new RandomAccessFile(file, "r");
            long len = raf.length();
            System.out.println(len);
            if (len != 0L) {
                long pos = len - 1;
                while (pos > 0) {
                    pos--;
                    raf.seek(pos);
                    if (raf.readByte() == '\n') {
                        lastLine = raf.readLine();
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lastLine;
    }

    public int getReceiveLogSize() {
        return receiveLogSize;
    }

    public void setReceiveLogSize(int receiveLogSize) {
        this.receiveLogSize = receiveLogSize;
    }

    public String getReceiveLogName() {
        return receiveLogName;
    }

    public void setReceiveLogName(String receiveLogName) {
        this.receiveLogName = receiveLogName;
    }

    public String getReceiveLogDir() {
        return receiveLogDir;
    }

    public void setReceiveLogDir(String receiveLogDir) {
        this.receiveLogDir = receiveLogDir;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
