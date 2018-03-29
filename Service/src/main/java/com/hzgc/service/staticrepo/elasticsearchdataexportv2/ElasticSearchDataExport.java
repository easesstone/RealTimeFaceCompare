package com.hzgc.service.staticrepo.elasticsearchdataexportv2;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

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
                .setSize(1).get();
        long tatalsize = scrollResp.getHits().getTotalHits();


//        ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 10,
//                0L, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>());
//
//        ArrayList<Future<String>> resultsPrint = new ArrayList<>();
//        do {
//            ElasticDataGetter elasticDataGetter = new ElasticDataGetter(scrollResp, index_export,
//                    index_type, export_data_dir);
//            Future<String> resultPrint =pool.submit(elasticDataGetter);
//            resultsPrint.add(resultPrint);
//
//            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
//                    .setScroll(new TimeValue(60000)).execute().actionGet();
//        } while(scrollResp.getHits().getHits().length != 0);// Zero hits mark the end of the scroll and the while loop.
//
//        boolean allThreadsIsDone = pool.getTaskCount()==pool.getCompletedTaskCount();
//        System.out.println(allThreadsIsDone);
//        if(allThreadsIsDone){
//            System.out.println("全部执行完成");
//        }
//        while (!allThreadsIsDone){
//            allThreadsIsDone = pool.getTaskCount()==pool.getCompletedTaskCount();
//            if(allThreadsIsDone){
//                System.out.println("全部执行完成");
//            }
//        }
//
//        try {
//            for (Future<String> tmp : resultsPrint) {
//                System.out.println(tmp.get());
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        System.out.println("time: " + (System.currentTimeMillis() - start));
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


class ElasticDataGetter implements Callable<String> {
    private SearchResponse scrollResp;
    private String index_export;
    private String index_type;
    private String export_data_dir;
    public ElasticDataGetter(SearchResponse scrollResp, String index_export,
                             String index_type,String export_data_dir) {
        this.scrollResp = scrollResp;
        this.index_export = index_export;
        this.index_type = index_type;
        this.export_data_dir = export_data_dir;
    }

    @Override
    public String call() throws Exception {
        int count = 0;
        long start = System.currentTimeMillis();
        SearchHits searchHits = scrollResp.getHits();
        String fileName = export_data_dir + File.separator + UUID.randomUUID().toString();
        Writer writer = new FileWriter(new File(fileName));
        Map<String, Map<String, String>> theHead = new HashMap<>();
        Map<String, String> indexAndType = new HashMap<>();
        try {
            for (SearchHit hit : searchHits.getHits()) {
                indexAndType.put("_index", index_export);
                indexAndType.put("_type", index_type);
                indexAndType.put("_id", hit.getId());
                theHead.put("index", indexAndType);
                writer.write(JSONObject.fromObject(theHead).toString() + "\r\n");
                writer.write(JSONObject.fromObject(hit.getSource()).toString() + "\r\n");
                indexAndType.clear();
                theHead.clear();
                count ++;
                System.out.println("==================================" + count + "=============================");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
        return "当前线程：" + Thread.currentThread().getName() + ", 花费时间：" + (System.currentTimeMillis() - start);
    }
}