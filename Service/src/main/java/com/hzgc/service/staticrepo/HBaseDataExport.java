package com.hzgc.service.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.jni.FaceFunction;
import com.hzgc.service.util.HBaseHelper;
import net.sf.json.JSONObject;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HBaseDataExport {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("show usage: ");
            System.out.println("第一个参数，args[0]： 表示导出的数据的存储的目录。");
            System.exit(1);
        }

        Table objectInfo = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Scan scan = new Scan();
        String storePath = args[0];
        File file = new File(storePath);
        if (!file.exists()) {
            file.mkdir();
        }

        String storeFile = storePath + File.separator + UUID.randomUUID().toString() + ".json";
        Writer writer = null;

        try {
            writer = new FileWriter(new File(storeFile));
            ResultScanner scanner = objectInfo.getScanner(scan);
            int count = 1;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for(Result result : scanner) {
                Map<String, Object> map = new HashMap<>();
                String rowkey = Bytes.toString(result.getRow());
                String platFormID = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PLATFORMID)));
                String tag = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.TAG)));
                String name = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.NAME)));
                String idCard = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.IDCARD)));
                int sex = 0;
                if (result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.SEX)) != null) {
                    String sexStr = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                            Bytes.toBytes(ObjectInfoTable.SEX)));
                    if (!"".equals(sexStr.trim()) && !"\"\"".equals(sexStr)) {
                        sex = Integer.parseInt(sexStr);
                    }
                }
                String pkey = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PKEY)));

                byte[] photo = result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PHOTO));
                String feature = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.FEATURE)));
                String creator = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CREATOR)));
                String cphone = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CPHONE)));
                String reason  = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.REASON)));
                String createTime = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CREATETIME)));
                String updateTime = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.UPDATETIME)));
                map.put(ObjectInfoTable.ROWKEY, rowkey);
                map.put(ObjectInfoTable.PLATFORMID, platFormID);
                map.put(ObjectInfoTable.NAME, name);
                map.put(ObjectInfoTable.TAG, tag);
                map.put(ObjectInfoTable.IDCARD, idCard);
                map.put(ObjectInfoTable.SEX, sex);
                map.put(ObjectInfoTable.PHOTO, photo);
                map.put(ObjectInfoTable.FEATURE, FaceFunction.string2floatArray(feature));
                map.put(ObjectInfoTable.CREATOR, creator);
                map.put(ObjectInfoTable.CPHONE, cphone);
                map.put(ObjectInfoTable.REASON, reason);
                map.put(ObjectInfoTable.PKEY, pkey);
                if (createTime != null && !"".equals(createTime)) {
                    map.put(ObjectInfoTable.CREATETIME, new java.sql.Timestamp(format.parse(createTime).getTime()));
                } else {
                    map.put(ObjectInfoTable.CREATETIME, new java.sql.Timestamp(System.currentTimeMillis()));
                }
                if (updateTime != null && !"".equals(updateTime))  {
                    map.put(ObjectInfoTable.UPDATETIME, new java.sql.Timestamp(format.parse(updateTime).getTime()));
                } else {
                    map.put(ObjectInfoTable.UPDATETIME, new java.sql.Timestamp(System.currentTimeMillis()));
                }
                map.put(ObjectInfoTable.IMPORTANT, 0);
                map.put(ObjectInfoTable.STATUS, 0);
                writer.write(JSONObject.fromObject(map).toString() + "\r\n");
                if (count % 1000 == 0) {
                    writer.flush();
                }
                System.out.println("正在导出第: " + count + " 条数据......");
                count ++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
