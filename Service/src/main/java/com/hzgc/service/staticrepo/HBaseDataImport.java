package com.hzgc.service.staticrepo;


import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.dubbo.staticrepo.PersonObject;
import net.sf.json.JSONArray;
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
            int label = 0;
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
                pstm.setObject(1, person.get(ObjectInfoTable.ROWKEY).toString());
                if (person.get(ObjectInfoTable.NAME) != null) {
                    pstm.setObject(2, person.get(ObjectInfoTable.NAME).toString());
                } else {
                    pstm.setObject(2, null);
                }
                if (person.get(ObjectInfoTable.PLATFORMID) != null) {
                    pstm.setObject(3, person.get(ObjectInfoTable.PLATFORMID).toString());
                } else {
                    pstm.setObject(3, null);
                }
                if (person.get(ObjectInfoTable.TAG) != null) {

                    pstm.setObject(4, person.get(ObjectInfoTable.TAG).toString());
                } else {
                    pstm.setObject(4, null);
                }
                if (person.get(ObjectInfoTable.PKEY) != null) {
                    pstm.setObject(5, person.get(ObjectInfoTable.PKEY).toString());
                } else {
                    pstm.setObject(5, null);
                }
                if (person.get(ObjectInfoTable.IDCARD) != null) {

                    pstm.setObject(6, person.get(ObjectInfoTable.IDCARD).toString());
                } else {
                    pstm.setObject(6, null);
                }
                if (person.get(ObjectInfoTable.SEX) != null){
                    pstm.setObject(7, Integer.parseInt(person.get(ObjectInfoTable.SEX).toString()));
                } else {
                    pstm.setObject(7, null);
                }
                JSONArray photoObject = null;
                if (person.get(ObjectInfoTable.PHOTO) != null) {
                    photoObject =  (JSONArray)person.get(ObjectInfoTable.PHOTO);
                }
                Object[] photo = null;
                byte[] finalPhoto = null;
                if (photoObject != null ) {
                    photo = photoObject.toArray();
                    finalPhoto = new byte[photo.length];
                    for (int i = 0; i < photo.length; i++) {
                        byte a = Byte.parseByte(photo[i].toString());
                        finalPhoto[i] = a;
                    }
                }
                pstm.setBytes(8, finalPhoto);

                JSONArray featureObject = (JSONArray) person.get(ObjectInfoTable.FEATURE);
                float [] finalFeature = null;
                if (featureObject != null) {
                    Object[] featureObjectArray = featureObject.toArray();
                    finalFeature = new float[featureObjectArray.length];
                    for (int i = 0; i < featureObjectArray.length; i++) {
                        finalFeature[i] = Float.parseFloat(featureObjectArray[i].toString());
                    }
                }

                java.sql.Array array = null;
                if (featureObject != null && finalFeature != null && finalFeature.length >0) {
                    array = connection.createArrayOf("FLOAT", PersonObject.otherArrayToObject(finalFeature));
                }
                pstm.setArray(9, array);
                if (person.get(ObjectInfoTable.REASON) != null) {

                    pstm.setObject(10,person.get(ObjectInfoTable.REASON).toString());
                } else {
                    pstm.setObject(10, null);
                }
                if (person.get(ObjectInfoTable.CREATOR) != null) {

                    pstm.setObject(11, person.get(ObjectInfoTable.CREATOR).toString());
                } else {
                    pstm.setObject(11, null);
                }
                if (person.get(ObjectInfoTable.CPHONE) != null) {
                    pstm.setObject(12, person.get(ObjectInfoTable.CPHONE).toString());
                } else {
                    pstm.setObject(12, null);
                }
                pstm.setObject(13, new java.sql.Timestamp(System.currentTimeMillis()));
                pstm.setObject(14, new java.sql.Timestamp(System.currentTimeMillis()));
                pstm.setObject(15, 0);
                pstm.setObject(16, 0);
                pstm.executeUpdate();
                if (count == 10) {
                    connection.commit();
                    count = 1;
                }
                label ++;
                System.out.println("第 " + label + " 条数据。。。。。。。");
                count ++;
                line = reader.readLine();
            }
            connection.commit();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
