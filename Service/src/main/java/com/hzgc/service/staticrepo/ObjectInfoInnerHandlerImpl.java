package com.hzgc.service.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.service.util.HBaseHelper;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ObjectInfoInnerHandlerImpl implements Serializable {

    private static Logger LOG = Logger.getLogger(ObjectInfoInnerHandlerImpl.class);
    private static ObjectInfoInnerHandlerImpl instance;
    private static long totalNums = getTotalNums();
    private static List<Object[]> totalList = null;

    /**
     * 接口实现使用单例模式
     */
    private ObjectInfoInnerHandlerImpl() {
    }

    /**
     * 获取内存中底库数据
     *
     * @return 返回底库
     */
    public List<Object[]> getTotalList() {
        if (totalList == null || totalNumIsChange()) {
            System.out.println("Start load static library...");
            totalList = searchByPkeys();
            System.out.println("Load static library successfull...");
            return totalList;
        } else {
            return totalList;
        }
    }

    /**
     * 用于判断HBase 中的静态信息库数据量是否有改变
     *
     * @return true表示有变化, false 表示没有变化
     */
    private boolean totalNumIsChange() {
        long newTotalNums = getTotalNums();
        if (totalNums == newTotalNums) {
            return false;
        } else {
            System.out.println("TotalNums changed");
            setTotalNums(newTotalNums);
            return true;
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
     * 设置当前底库总数
     *
     * @param newToalNums 底库中最新总数信息
     */
    private static void setTotalNums(long newToalNums) {
        totalNums = newToalNums;
        LOG.info("set new number successfull, new number is:" + totalNums);
    }

    /**
     * 获取底库中最新总数信息
     *
     * @return 最新总数信息
     */
    private static long getTotalNums() {
        Get get = new Get(Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS_ROW_NAME));
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        try {
            Result result = table.get(get);
            return Bytes.toLong(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                    Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS)));
        } catch (IOException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 查询所有的数据
     *
     * @return 返回其中的rowkey, pkey, feature
     */
    private List<Object[]> searchByPkeys() {
        String sql = "select " + ObjectInfoTable.ROWKEY + ", " + ObjectInfoTable.PKEY +
                ", " + ObjectInfoTable.FEATURE + " from " + ObjectInfoTable.TABLE_NAME;
        PreparedStatement pstm = null;
        List<Object[]> findResult = new ArrayList<>();
        ComboPooledDataSource comboPooledDataSource = PhoenixJDBCHelper.getComboPooledDataSource();
        java.sql.Connection conn;
        try {
            conn = comboPooledDataSource.getConnection();
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
        String pkeysWhere = "";
        if (pkeys.size() == 1) {
            pkeysWhere = " where " + ObjectInfoTable.PKEY +" = ?";
        } else {
            pkeysWhere += " where (";
            int i = 0;
            for (String pkey : pkeys) {
                if (i == 0){
                    pkeysWhere += ObjectInfoTable.PKEY;
                    pkeysWhere += " = ? ";
                } else {
                    pkeysWhere += " or ";
                    pkeysWhere += ObjectInfoTable.PKEY;
                    pkeysWhere += " = ?";
                }
                i++;
            }
            pkeysWhere += ")";
        }
        sql = sql + pkeysWhere;

        PreparedStatement pstm = null;
        ComboPooledDataSource comboPooledDataSource = PhoenixJDBCHelper.getComboPooledDataSource();
        java.sql.Connection conn;
        try {
            conn = comboPooledDataSource.getConnection();
            pstm = conn.prepareStatement(sql);
            for(int i = 0; i< pkeys.size(); i++) {
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
        ComboPooledDataSource comboPooledDataSource = PhoenixJDBCHelper.getComboPooledDataSource();
        java.sql.Connection conn;
        try {
            conn = comboPooledDataSource.getConnection();
            pstm = conn.prepareStatement(sql);
            java.sql.Timestamp timeStamp = new java.sql.Timestamp(System.currentTimeMillis());
            for (int i = 0;i < rowkeys.size(); i++) {
                pstm.setString(1, rowkeys.get(i));
                pstm.setTimestamp(2, timeStamp);
                pstm.executeUpdate();
                if (i % 200 == 0) {
                    conn.commit();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm);
        }
        return 0;
    }
}
