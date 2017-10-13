package com.hzgc.ftpserver.kafka.consumer;

import com.hzgc.ftpserver.util.FtpUtil;
import com.hzgc.hbase.dynamicrepo.DynamicTable;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private BulkRequestBuilder bulkRequestBuilder;
    protected ArrayList<Put> consumer2HBaselist;
    private  ConsumerRecord<String, byte[]> consumerRecord;

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
        int i=0;
        try {
            if (null != tableName) {
                picTable = hbaseConn.getTable(TableName.valueOf(tableName));
            }
            while (true) {
                int count=buffer.size();
                int realNum;
                if (i < count) {
                    consumerRecord = buffer.take();
                    String topic=consumerRecord.topic();
                    int faceNum=0;
                    if (null != columnFamily && null != column_pic) {
                        Put put = new Put(Bytes.toBytes(consumerRecord.key()));//rowkey
                        put.setDurability(Durability.SKIP_WAL);
                        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column_pic), consumerRecord.value());
                        Map<String, String> map = FtpUtil.getRowKeyMessage(consumerRecord.key());
                        String ipcID = map.get("ipcID");
                        long timestamp = Long.valueOf(map.get("time"));
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column_ipcID), Bytes.toBytes(ipcID));
                        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column_time), Bytes.toBytes(dateFormat.format(timestamp)));
                        consumer2HBaselist.add(put);
                        LOG.info(Thread.currentThread().getName() + " [topic:" + topic +
                                ", key:" + consumerRecord.key() +
                                ", offset:" + consumerRecord.offset() +
                                ", partition:" + consumerRecord.partition() +
                                "]");
                        //将ipcID与time同步到ES中(只有人脸图信息同步到bulk中)
                        if ("face".equals(topic)) {
                            faceNum++;
                            Map<String, String> mapES = new HashMap<>();
                            mapES.put("s", ipcID);
                            mapES.put("t", dateFormat.format(timestamp));
                            mapES.put("sj", map.get("sj"));
                            bulkRequestBuilder = ElasticSearchHelper.getEsClient().prepareBulk();
                            bulkRequestBuilder.add(ElasticSearchHelper.getEsClient()
                                    .prepareIndex(DynamicTable.DYNAMIC_INDEX, DynamicTable.PERSON_INDEX_TYPE, consumerRecord.key())
                                    .setSource(mapES));//批量导入map到bulk
                            LOG.info("add face to bulk Num："+faceNum);
                        }
                        realNum=bulkRequestBuilder.numberOfActions();
                        LOG.info("sum bulk："+realNum);
                    }
                    i++;
                }
                if (i >=count) {
                    picTable.put(consumer2HBaselist);
                    consumer2HBaselist.clear();
                    BulkResponse bulkItemResponses = bulkRequestBuilder.get();//批量导入到ES
                    LOG.info(Thread.currentThread().getName() + "[topic:" + consumerRecord.topic() +
                            ", key:" + consumerRecord.key() +
                            ", bulk to ES status: " + bulkItemResponses.status() + "]");

                }
            }

        }catch (Exception e) {
            isCommit.replace("isCommit", false);
            e.printStackTrace();
        }finally {
            try {
                picTable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
