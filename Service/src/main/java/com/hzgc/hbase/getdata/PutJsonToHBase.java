package com.hzgc.hbase.getdata;

import com.hzgc.hbase.util.HBaseHelper;
import net.sf.json.JSONObject;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class PutJsonToHBase {
    public static void putjson(String josnPath) {
        Table table = HBaseHelper.getTable("objectinfo");
        BufferedReader reader = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(josnPath);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                JSONObject jsonObject = JSONObject.fromObject(tempString);
                String rowkey = String.valueOf(jsonObject.get("rowkey"));
                Put put = new Put(Bytes.toBytes(rowkey));
                if (jsonObject.get("photo") != null && !jsonObject.get("photo").equals("")) {
                    String photo = String.valueOf(jsonObject.get("photo"));
                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("photo"), Bytes.toBytes(photo));
                }
                if (jsonObject.get("platformid") != null && !jsonObject.get("platformid").equals("")) {
                    String platformid = String.valueOf(jsonObject.get("platformid"));
                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("platformid"), Bytes.toBytes(platformid));
                }
                if (jsonObject.get("tag") != null && !jsonObject.get("tag").equals("")) {
                    String tag = String.valueOf(jsonObject.get("pkey"));
                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("tag"), Bytes.toBytes(tag));
                }
                if (jsonObject.get("idcard") != null && !jsonObject.get("idcard").equals("")) {
                    String idcard = String.valueOf(jsonObject.get("idcard"));
                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("idcard"), Bytes.toBytes(idcard));
                }
                if (jsonObject.get("sex") != null && !jsonObject.get("sex").equals("")) {
                    String sex = String.valueOf(jsonObject.get("sex"));
                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("sex"), Bytes.toBytes(sex));
                }
                if (jsonObject.get("feature") != null && !jsonObject.get("feature").equals("")) {
                    String feature = String.valueOf(jsonObject.get("feature"));
                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("feature"), Bytes.toBytes(feature));
                }
                if (jsonObject.get("creator") != null && !jsonObject.get("creator").equals("")) {
                    String creator = String.valueOf(jsonObject.get("creator"));
                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("creator"), Bytes.toBytes(creator));
                }
                if (jsonObject.get("cphone") != null && !jsonObject.get("cphone").equals("")) {
                    String cphone = String.valueOf(jsonObject.get("cphone"));
                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("cphone"), Bytes.toBytes(cphone));
                }
                if (jsonObject.get("name") != null && !jsonObject.get("name").equals("")) {
                    String name = String.valueOf(jsonObject.get("name"));
                    put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("name"), Bytes.toBytes(name));
                }
                table.put(put);
            }
            fileInputStream.close();
            inputStreamReader.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("");
        }
        String josnPath = args[0];
        long a = System.currentTimeMillis();
        putjson(josnPath);
        System.out.println(System.currentTimeMillis() - a);
    }
}
