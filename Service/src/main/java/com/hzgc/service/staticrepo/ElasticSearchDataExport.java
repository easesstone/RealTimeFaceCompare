package com.hzgc.service.staticrepo;

import com.google.common.io.Files;
import net.sf.json.JSONObject;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Es导出工具
 */
public class ElasticSearchDataExport {
    public static void main(String[] args) {
//        // 参数验证
//        if (args.length != 4) {
//            System.exit(0);
//        }
//        String es_ip = args[0];
//        String es_cluster_name = args [1];
//        String index_export = args[2];
//        String index_type = args[3];
//        String export_data_dir  = args[4];
        long start = System.currentTimeMillis();
        String es_ip = "172.18.18.103";
        String es_cluster_name = "hbase2es-cluster";
        String index_export = "dynamic";
        String index_type = "person";
        String export_data_dir  = "E:\\Json";
        delteFileOrDir(export_data_dir);
        new File(export_data_dir).mkdirs();

        Settings settings = Settings.builder().put("cluster.name", es_cluster_name)
                .put("client.transport.sniff", true).build();
        TransportClient client = null;
        try {
             client = new PreBuiltTransportClient(settings)
                     .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(es_ip),
                             9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        QueryBuilders.matchAllQuery();
        if (client == null) {
            System.exit(0);
        }
        SearchResponse scrollResp = client.prepareSearch(index_export)
                .setTypes(index_type)
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(5000).get();

        //Scroll until no hits are returned
        Map<String, Map<String, String>> theHead = new HashMap<>();
        Map<String, String> indexAndType = new HashMap<>();
        int count = 0;

        Writer writer = null;
        File tmpJson = new File(export_data_dir + File.separator + "tmp.json");
        delteFileOrDir(tmpJson);
        try {
            tmpJson.createNewFile();
            delteFileOrDir(tmpJson);
            writer = new FileWriter(tmpJson);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        long countTime = 0;
        do {
            SearchHits searchHits = scrollResp.getHits();
            long startNow = System.currentTimeMillis();
            for (SearchHit hit : searchHits.getHits()) {
                try {
                    indexAndType.put("_index", index_export);
                    indexAndType.put("_type", index_type);
                    indexAndType.put("_id", hit.getId());
                    theHead.put("index", indexAndType);
                    writer.write(JSONObject.fromObject(theHead).toString() + "\r\n");
                    writer.write(JSONObject.fromObject(hit.getSource()).toString() + "\r\n");
                    indexAndType.clear();
                    theHead.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                count ++;
                System.out.println("==================================" + count + "=============================");
                if (count == 20000) {
                    File fileFinal = new File(export_data_dir + File.separator + UUID.randomUUID().toString()
                            .replaceAll("-", "")+".json");
                    try {
                        writer.flush();
                        Files.copy(tmpJson, fileFinal);
                        delteFileOrDir(tmpJson);
                        writer = new FileWriter(tmpJson);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    count = 0;
                }
            }
            countTime += System.currentTimeMillis() -startNow;
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                    .setScroll(new TimeValue(60000)).execute().actionGet();

        } while(scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
        try {
            if (writer != null) {
                writer.flush();
            }
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

        boolean flag = tmpJson.delete();
        System.out.println("time: " + flag + " : " + countTime + " : " + (System.currentTimeMillis() - start));
    }

    private static void delteFileOrDir(String filePath) {
        delteFileOrDir(new File(filePath));
    }
    private static void delteFileOrDir(File file) {
        if (!file.exists()){
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File files[] = file.listFiles();
            if (files != null && files.length == 0) {
                file.delete();
                return;
            }
            for (File tmp : files) {
                delteFileOrDir(tmp.getAbsolutePath());
            }
        }
    }
}
