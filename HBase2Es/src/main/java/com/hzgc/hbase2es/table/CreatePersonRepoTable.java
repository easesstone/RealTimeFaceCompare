package com.hzgc.hbase2es.table;

import com.hzgc.hbase2es.util.HBaseHelper;
import com.hzgc.util.FileUtil;
import com.hzgc.util.IOUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class CreatePersonRepoTable {
    // 读取的是Conf 目录下的文件person-table.properties
    private static Logger LOG = Logger.getLogger(CreatePersonRepoTable.class);
    public static void main(String[] args) {
        Properties tableProper = new Properties();
        InputStream coproccessor = null;
        File file = FileUtil.loadResourceFile("person-table.properties");
        if (file == null){
            return;
        }
        LOG.info("file person-table.properties path: " + file.getAbsolutePath());
        try {
            coproccessor = new FileInputStream(file);
            tableProper.load(coproccessor);
            String tableName = tableProper.getProperty("table.name");
            String colfamsString = tableProper.getProperty("table.colfams");
            String maxVersion = tableProper.getProperty("table.maxversion");
            String timetToLive = tableProper.getProperty("table.timetolive");
            String[] colfams = colfamsString.split("-");
            if (HBaseHelper.getHBaseConnection().getAdmin().tableExists(TableName.valueOf(tableName))) {
                LOG.error("表格:" + tableName + "已经存在，请进行确认是否删除表格，需要手动到HBase 客户端删除表格。");
                return;
            }
            if (timetToLive != null) {
                HBaseHelper.createTable(tableName, Integer.parseInt(maxVersion), Integer.parseInt(timetToLive), colfams);
            } else {
                HBaseHelper.createTable(tableName, Integer.parseInt(maxVersion), colfams);
            }
            LOG.info("====================== create table " + tableName + "success.. ==================");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(coproccessor);
        }
    }
}
