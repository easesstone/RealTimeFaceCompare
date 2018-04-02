package com.hzgc.service.staticrepo;


import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import net.sf.json.JSONObject;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class HBaseDataImport {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("show usage: ");
            System.out.println("第一个参数，args[0]： 表示HBase 数据文件。");
            System.exit(1);
        }

        String dataFilePath = args[0];
        File dataFile = new File(dataFilePath);
        if (!dataFile.exists()) {
            System.out.println("传入的文件：" + dataFilePath + " 不存在。");
            System.exit(1);
        }
        Map<String, Object> person = new HashMap<>();
        Connection connection = PhoenixJDBCHelper.getPhoenixJdbcConn();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)));
            String line = reader.readLine();
            int count = 1;
            while (line != null) {
                person = JSONObject.fromObject(line);
                String sql = "upsert into objectinfo(" + ObjectInfoTable.ROWKEY+ ", " + ObjectInfoTable.NAME  + ", "
                        + ObjectInfoTable.PLATFORMID + ", " + ObjectInfoTable.TAG + ", " + ObjectInfoTable.PKEY + ", "
                        + ObjectInfoTable.IDCARD + ", " + ObjectInfoTable.SEX + ", " + ObjectInfoTable.PHOTO + ", "
                        + ObjectInfoTable.FEATURE + ", " + ObjectInfoTable.REASON + ", " + ObjectInfoTable.CREATOR + ", "
                        + ObjectInfoTable.CPHONE + ", " + ObjectInfoTable.CREATETIME + ", " + ObjectInfoTable.UPDATETIME + ", "
                        + ObjectInfoTable.IMPORTANT + ", "  + ObjectInfoTable.STATUS
                        + ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement pstm = null;
                pstm = connection.prepareStatement(sql);
                pstm.setObject(1, person.get(ObjectInfoTable.ROWKEY));
                pstm.setObject(2, person.get(ObjectInfoTable.NAME));
                pstm.setObject(3, person.get(ObjectInfoTable.PLATFORMID));
                pstm.setObject(4, person.get(ObjectInfoTable.TAG));
                pstm.setObject(5, person.get(ObjectInfoTable.PKEY));
                pstm.setObject(6, ObjectInfoTable.IDCARD);
                pstm.setObject(7, ObjectInfoTable.SEX);
                pstm.setObject(8, person.get(ObjectInfoTable.PHOTO));
                pstm.setObject(9, person.get(ObjectInfoTable.FEATURE));
                pstm.setObject(10,person.get(ObjectInfoTable.REASON));
                pstm.setObject(11, person.get(ObjectInfoTable.CREATOR));
                pstm.setObject(12, person.get(ObjectInfoTable.CPHONE));
                pstm.setObject(13, person.get(ObjectInfoTable.CREATETIME));
                pstm.setObject(14, person.get(ObjectInfoTable.UPDATETIME));
                pstm.setObject(15, person.get(ObjectInfoTable.IMPORTANT));
                pstm.setObject(16, person.get(ObjectInfoTable.STATUS));
                if (count == 1000) {
                    connection.commit();
                    count = 1;
                }
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
