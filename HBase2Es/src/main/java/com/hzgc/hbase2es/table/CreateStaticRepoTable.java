package com.hzgc.hbase2es.table;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.hbase2es.util.HBaseHelper;
import com.hzgc.util.FileUtil;
import com.hzgc.util.IOUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class CreateStaticRepoTable {
    private static Logger LOG = Logger.getLogger(CreateStaticRepoTable.class);


    public static void main(String[] args) {
        Properties tableProper = new Properties();
        InputStream coproccessor = null;
        try {
            File file = FileUtil.loadResourceFile("static-table.properties");
            if (file == null){
                return;
            }
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
            Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
            Put put = new Put(Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS_ROW_NAME));
            put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS),
                    Bytes.toBytes(0L));
            table.put(put);
            LOG.info("====================== create table " + tableName + "success.. ==================");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(coproccessor);
        }
    }
}
