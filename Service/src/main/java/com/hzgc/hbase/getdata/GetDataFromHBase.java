package com.hzgc.hbase.getdata;

import com.hzgc.hbase.putdata.MapToJson;
import com.hzgc.hbase.util.HBaseHelper;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;


public class GetDataFromHBase {
    public static void getJson(String fristjson, String secondjson) {
        Table table = HBaseHelper.getTable("objectinfo");
        Scan scan = new Scan();
        try {
            File file = new File(fristjson);
            if (!file.exists()) {
                file.createNewFile();
            }
            MapToJson mapToJson = new MapToJson();
            ResultScanner resultScanner = table.getScanner(scan);
            FileOutputStream fos = new FileOutputStream(file);
            File file1 = new File(secondjson);
            if (!file1.exists()) {
                file1.createNewFile();
            }
            FileOutputStream fos1 = new FileOutputStream(file1);
            for (Result result : resultScanner) {
                Map<String, Object> map = new HashMap<>();
                Map<String, Object> map1 = new HashMap<>();
                Map<String, Object> map2 = new HashMap<>();
                Map<String, Object> map3 = new HashMap<>();
                String rowkey = Bytes.toString(result.getRow());
                String photo = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("photo")));
                String platformid = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("platformid")));
                String tag = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("tag")));
                String pkey = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("pkey")));
                String idcard = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("idcard")));
                String sex = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("sex")));
                String feature = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("feature")));
                String creator = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("creator")));
                String cphone = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("cphone")));
                String name = Bytes.toString(result.getValue(Bytes.toBytes("person"), Bytes.toBytes("name")));
                //写入第一个json文件,用来导HBase
                map.put("rowkey", rowkey);
                map.put("photo", photo);
                map.put("platformid", platformid);
                map.put("tag", tag);
                map.put("pkey", pkey);
                map.put("idcard", idcard);
                map.put("sex", sex);
                map.put("feature", feature);
                map.put("creator", creator);
                map.put("cphone", cphone);
                map.put("name", name);
                String newJson = mapToJson.mapToJson(map);
                fos.write(newJson.getBytes());
                fos.write("\n".getBytes());
                //写入第二个json文件，用来导ES
                map2.put("_index", "objectinfo");
                map2.put("_type", "person");
                map2.put("_id", rowkey);
                map1.put("create", map2);
                String newJson1 = mapToJson.mapToJson(map1);
                fos1.write(newJson1.getBytes());
                fos1.write("\n".getBytes());
                map3.put("platformid", platformid);
                map3.put("feature", feature);
                map3.put("name", name);
                map3.put("tag", tag);
                map3.put("pkey", pkey);
                map3.put("idcard", idcard);
                map3.put("sex", sex);
                map3.put("creator", creator);
                map3.put("cphone", cphone);
                String newJson2 = mapToJson.mapToJson(map3);
                System.out.println(newJson2);
                fos1.write(newJson2.getBytes());
                fos1.write("\n".getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("");
        }
        String fristJson = args[0];
        String secondJson = args[1];
        long a = System.currentTimeMillis();
        getJson(fristJson, secondJson);
        System.out.println(System.currentTimeMillis() - a);
    }
}
