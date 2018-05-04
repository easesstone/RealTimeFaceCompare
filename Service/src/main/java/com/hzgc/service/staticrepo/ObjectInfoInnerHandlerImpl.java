package com.hzgc.service.staticrepo;

import com.hzgc.collect.expand.util.JSONHelper;
import com.hzgc.collect.expand.util.ProducerOverFtpProperHelper;
import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.util.common.UuidUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

class StaticRepoThread implements Runnable {
    private ObjectInfoInnerHandlerImpl handler;
    private KafkaConsumer<String, String> consumer;

    StaticRepoThread(ObjectInfoInnerHandlerImpl handler) {
        this.handler = handler;
        Properties properties = new Properties();
        properties.put("bootstrap.servers", ProducerOverFtpProperHelper.getBootstrapServers());
        properties.put("group.id", UuidUtil.setUuid());
        properties.put("enable.auto.commit", "true");
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("session.timeout.ms", "30000");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(ObjectInfoHandlerImpl.INNERTOPIC));
    }

    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, String> events = consumer.poll(0);
            if (!events.isEmpty()) {
                for (ConsumerRecord<String, String> event : events) {
                    if (ObjectInfoHandlerImpl.ADD.equals(event.key())) {
                        addObject(event.value());
                    }
                    if (ObjectInfoHandlerImpl.DELETE.equals(event.key())) {
                        deleteObject(event.value());
                    }
                    if (ObjectInfoHandlerImpl.UPDATE.equals(event.key())) {
                        updateObject(event.value());
                    }
                }
            }
        }
    }

    private void addObject(String event) {
        StaticRepoObject object = JSONHelper.toObject(event, StaticRepoObject.class);
        handler.addObject(object.getRowkey(), object.getPkey(), object.getFeature());
    }

    private void deleteObject(String event) {
        handler.deleteObject(event);
    }

    private void updateObject(String event) {
        StaticRepoObject object = JSONHelper.toObject(event, StaticRepoObject.class);
        handler.updateObject(object.getRowkey(), object.getPkey(), object.getFeature());
    }
}

public class ObjectInfoInnerHandlerImpl implements Serializable {

    private static Logger LOG = Logger.getLogger(ObjectInfoInnerHandlerImpl.class);
    private static ObjectInfoInnerHandlerImpl instance;
    private static final List<Object[]> totalList = searchByPkeys();

    /**
     * 接口实现使用单例模式
     */
    private ObjectInfoInnerHandlerImpl() {
        Thread thread = new Thread(new StaticRepoThread(this));
        thread.start();
        LOG.info("StaticRepo consumer started");
    }

    /**
     * 获取内存中底库数据
     *
     * @return 返回底库
     */
    public List<Object[]> getTotalList() {
        synchronized (totalList) {
            return totalList;
        }
    }


    /**
     * 获取对象的唯一方法
     *
     * @return 返回实例化对象
     */
    public static ObjectInfoInnerHandlerImpl getInstance() {
        if (instance == null) {
            synchronized (ObjectInfoInnerHandlerImpl.class) {
                if (instance == null) {
                    instance = new ObjectInfoInnerHandlerImpl();
                }
            }
        }
        return instance;
    }

    /**
     * 查询所有的数据
     *
     * @return 返回其中的rowkey, pkey, feature
     */
    private static List<Object[]> searchByPkeys() {
        String sql = "select " + ObjectInfoTable.ROWKEY + ", " + ObjectInfoTable.PKEY +
                ", " + ObjectInfoTable.FEATURE + " from " + ObjectInfoTable.TABLE_NAME;
        System.out.println(sql);
        PreparedStatement pstm = null;
        List<Object[]> findResult = new ArrayList<>();
        java.sql.Connection conn;
        try {
            conn = PhoenixJDBCHelper.getInstance().getConnection();
            pstm = conn.prepareStatement(sql);
            ResultSet resultSet = pstm.executeQuery();
            while (resultSet.next()) {
                String rowKey = resultSet.getString(ObjectInfoTable.ROWKEY);
                String pkey = resultSet.getString(ObjectInfoTable.PKEY);
                Array array = resultSet.getArray(ObjectInfoTable.FEATURE);
                float[] feature = null;
                if (array != null) {
                    feature = (float[]) array.getArray();
                }
                if (feature != null && feature.length > 0) {
                    //将人员类型rowkey和特征值进行拼接
                    Object[] result1 = new Object[3];
                    result1[0] = rowKey;
                    result1[1] = pkey;
                    result1[2] = feature;
                    //将结果添加到集合中
                    findResult.add(result1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm);
        }

        return findResult;
    }

    /**
     * 获取底库中包含在此List中的人员信息及最新出现时间
     *
     * @param pkeys 对象类型列表
     * @return 返回符合条件的数据
     */
    public List<String> searchByPkeysUpdateTime(List<String> pkeys) {
        if (pkeys == null || pkeys.size() == 0) {
            LOG.info("pkeys 为Null 或者pkeys 个数为0，请传入正确参数.");
            return null;
        }

        List<String> findResult = new ArrayList<>();

        String sql = "select " + ObjectInfoTable.ROWKEY + ", " + ObjectInfoTable.PKEY +
                ", " + ObjectInfoTable.UPDATETIME + " from " + ObjectInfoTable.TABLE_NAME;
        StringBuilder pkeysWhere = new StringBuilder();
        if (pkeys.size() == 1) {
            pkeysWhere = new StringBuilder(" where " + ObjectInfoTable.PKEY + " = ?");
        } else {
            pkeysWhere.append(" where (");
            int i = 0;
            for (String ignored : pkeys) {
                if (i == 0) {
                    pkeysWhere.append(ObjectInfoTable.PKEY);
                    pkeysWhere.append(" = ? ");
                } else {
                    pkeysWhere.append(" or ");
                    pkeysWhere.append(ObjectInfoTable.PKEY);
                    pkeysWhere.append(" = ?");
                }
                i++;
            }
            pkeysWhere.append(")");
        }
        sql = sql + pkeysWhere;

        PreparedStatement pstm = null;
        java.sql.Connection conn;
        try {
            conn = PhoenixJDBCHelper.getInstance().getConnection();
            pstm = conn.prepareStatement(sql);
            for (int i = 0; i < pkeys.size(); i++) {
                pstm.setString(i + 1, pkeys.get(i));
            }
            ResultSet resultSet = pstm.executeQuery();
            if (resultSet != null) {
                while (resultSet.next()) {
                    String rowKey = resultSet.getString(ObjectInfoTable.ROWKEY);
                    String pkey = resultSet.getString(ObjectInfoTable.PKEY);
                    java.sql.Timestamp updateTime = resultSet.getTimestamp(ObjectInfoTable.UPDATETIME);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String finalTime = format.format(updateTime);
                    //将人员类型、rowkey和特征值进行拼接
                    String result = rowKey + "ZHONGXIAN" + pkey + "ZHONGXIAN" + finalTime;
                    //将结果添加到集合中
                    findResult.add(result);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm);
        }
        return findResult;
    }

    /**
     * 更新此List中的人员最新出现时间
     *
     * @param rowkeys 对象类型列表
     * @return 0成功, 1失败
     */
    public int updateObjectInfoTime(List<String> rowkeys) {
        if (rowkeys == null || rowkeys.size() <= 0) {
            return 0;
        }
        String sql = "upsert into " + ObjectInfoTable.TABLE_NAME + "(" + ObjectInfoTable.ROWKEY + ", " + ObjectInfoTable.UPDATETIME +
                ") values(?,?)";

        PreparedStatement pstm = null;
        java.sql.Connection conn;
        try {
            conn = PhoenixJDBCHelper.getInstance().getConnection();
            pstm = conn.prepareStatement(sql);
            java.sql.Timestamp timeStamp = new java.sql.Timestamp(System.currentTimeMillis());
            for (int i = 0; i < rowkeys.size(); i++) {
                pstm.setString(1, rowkeys.get(i));
                pstm.setTimestamp(2, timeStamp);
                pstm.addBatch();
                if (i % 500 == 0) {
                    pstm.executeBatch();
                    conn.commit();
                }
            }
            pstm.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm);
        }
        return 0;
    }

    void addObject(String rowKey, String pkey, float[] feature) {
        Object[] objects = new Object[3];
        objects[0] = rowKey;
        objects[1] = pkey;
        objects[2] = feature;
        synchronized (totalList) {
            System.out.println("Method [addObject], totalList before is [" + totalList.size() + "], current time is [" + new Date() + "]");
            totalList.add(objects);
            System.out.println("Method [addObject], totalList after is [" + totalList.size() + "], current time is [" + new Date() + "]");
        }
    }

    void deleteObject(String event) {
        synchronized (totalList) {
            System.out.println("Method [deleteObject], totalList before is [" + totalList.size() + "], current time is [" + new Date() + "]");
            for (int i = 0; i < totalList.size(); i++) {
                if (Objects.equals(event, totalList.get(i)[0])) {
                    totalList.remove(i);
                }
            }
            System.out.println("Method [deleteObject], totalList after is [" + totalList.size() + "], current time is [" + new Date() + "]");
        }
    }

    void updateObject(String rowKey, String pkey, float[] feature) {
        Object[] objects = new Object[3];
        objects[0] = rowKey;
        objects[1] = pkey;
        objects[2] = feature;
        synchronized (totalList) {
            System.out.println("Method [updateObject], totalList before is [" + totalList.size() + "], current time is [" + new Date() + "]");
            for (int i = 0; i < totalList.size(); i++) {
                if (Objects.equals(rowKey, totalList.get(i)[0])) {
                    totalList.add(i, objects);
                }
            }
            System.out.println("Method [updateObject], totalList after is [" + totalList.size() + "], current time is [" + new Date() + "]");
        }
    }
}
