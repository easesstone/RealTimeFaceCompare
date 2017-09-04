package com.hzgc.ftpserver.kafka.consumer;

import com.hzgc.ftpserver.util.DynamicTable;
import com.hzgc.ftpserver.util.ElasticSearchHelper;
import com.hzgc.ftpserver.util.FtpUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerThread implements Runnable, Serializable {
    private Logger LOG = Logger.getLogger(WorkerThread.class);
    private BlockingQueue<ConsumerRecord<String, byte[]>> buffer;
    private ConcurrentHashMap<String, Boolean> isCommit;
    private Connection hbaseConn;
    private Table picTable;
    private String tableName;
    private String columnFamily;
    private String column_pic;
    private String column_ipcID;
    private String column_time;

    WorkerThread(Connection conn,
                 BlockingQueue<ConsumerRecord<String, byte[]>> buffer,
                 String tableName,
                 String columnFamily,
                 String column_pic,
                 String column_ipcID,
                 String column_time,
                 ConcurrentHashMap<String, Boolean> commit) {
        this.buffer = buffer;
        this.hbaseConn = conn;
        this.isCommit = commit;
        this.tableName = tableName;
        this.columnFamily = columnFamily;
        this.column_pic = column_pic;
        this.column_ipcID = column_ipcID;
        this.column_time = column_time;
        LOG.info("Create [" + Thread.currentThread().getName() + "] of PicWorkerThreads success");
    }

    public void run() {
        send();
    }

    private void send() {
        try {
            if (null != tableName) {
                picTable = hbaseConn.getTable(TableName.valueOf(tableName));
            }
            while (true) {
                ConsumerRecord<String, byte[]> consumerRecord = buffer.take();
                picTable = hbaseConn.getTable(TableName.valueOf(tableName));
                if (null != columnFamily && null != column_pic && null != consumerRecord) {
                    Put put = new Put(Bytes.toBytes(consumerRecord.key()));
                    put.setDurability(Durability.SKIP_WAL);
                    put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column_pic), consumerRecord.value());
                    Map<String, String> map = FtpUtil.getRowKeyMessage(consumerRecord.key());
                    String ipcID = map.get("ipcID");
                    long timestamp = Long.valueOf(map.get("time"));
                    Date date = new Date(timestamp);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column_ipcID), Bytes.toBytes(ipcID));
                    put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column_time), Bytes.toBytes(dateFormat.format(date)));
                    picTable.put(put);
                    LOG.info(Thread.currentThread().getName() + " [topic:" + consumerRecord.topic() +
                            ", offset:" + consumerRecord.offset() +
                            ", key:" + consumerRecord.key() +
                            ", partition:" + consumerRecord.partition() +
                            "]");
                    //将ipcID与time同步到ES中
                    Map<String, String> mapES = new HashMap<>();
                    mapES.put("s", ipcID);
                    mapES.put("t", dateFormat.format(date));
                    mapES.put("sj", map.get("sj"));
                    ElasticSearchHelper.getEsClient().prepareIndex(DynamicTable.DYNAMIC_INDEX, DynamicTable.PERSON_INDEX_TYPE,
                            consumerRecord.key()).setSource(mapES).get();
                }
            }
        } catch (Exception e) {
            isCommit.replace("isCommit", false);
            e.printStackTrace();
        } finally {
            try {
                picTable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
