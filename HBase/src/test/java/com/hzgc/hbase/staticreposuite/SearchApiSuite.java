package com.hzgc.hbase.staticreposuite;

import com.hzgc.dubbo.staticrepo.ObjectInfoHandler;
import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.dubbo.staticrepo.PSearchArgsModel;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import com.hzgc.hbase.staticrepo.ObjectInfoHandlerImpl;
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandler;
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class SearchApiSuite {
    @Test
    public void testSearchByTeams(){
        List<String> pkeys = new ArrayList<>();
        pkeys.add("223456");
        pkeys.add("223458");
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setExplain(true).setSize(10000);
        requestBuilder.setQuery(QueryBuilders.termsQuery("pkey", pkeys));
        System.out.println(requestBuilder.get().getHits().getTotalHits());
    }

    @Test
    public void testSearchByScrol01() {
        PSearchArgsModel pSearchArgsModel = new PSearchArgsModel();
        pSearchArgsModel.setRowkey(null);
        pSearchArgsModel.setPaltaformId("0001");
        pSearchArgsModel.setName(null);
        pSearchArgsModel.setIdCard(null);
        pSearchArgsModel.setSex(1);
        pSearchArgsModel.setImage(null);
        pSearchArgsModel.setFeature(null);
        pSearchArgsModel.setThredshold(-1);
        pSearchArgsModel.setCreator("破");
        List<String> pkeys = new ArrayList<>();
        pkeys.add("1234008");
        pkeys.add("1234010");
        pkeys.add("1234015");
        pkeys.add("1234016");
        pSearchArgsModel.setPkeys(pkeys);
        pSearchArgsModel.setStart(1);
        pSearchArgsModel.setPageSize(11);
        pSearchArgsModel.setSearchId(null);
        pSearchArgsModel.setSearchType("searchByMuti");
        pSearchArgsModel.setMoHuSearch(true);

        ObjectInfoHandler objectInfoHandler = new ObjectInfoHandlerImpl();
        System.out.println(objectInfoHandler.getObjectInfo(pSearchArgsModel));
    }

    @Test
    public void testStringContains(){
        System.out.println("根据搜索条件得到的记录数是".contains("到的记"));
    }

    //获取静态信息库下面的所有rowkeys
    public List<String> getAllRowKeyOfStaticRepo() {
        List<String> findResult = new ArrayList<>();
        Table objectinfo = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Scan scan = new Scan();
        try {
            ResultScanner resultScanner = objectinfo.getScanner(scan);
            Iterator<Result> iterator = resultScanner.iterator();
            while (iterator.hasNext()){
                Result result = iterator.next();
                String rowKey = Bytes.toString(result.getRow());
                findResult.add(rowKey);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            HBaseUtil.closTable(objectinfo);
        }
        return findResult;
    }

    @Test
    public void testUpdateObjectInfoTime(){
//        new ObjectInfoInnerHandlerImpl().updateObjectInfoTime(getAllRowKeyOfStaticRepo());
    }

    @Test
    public void testUpdateObjectInfoTimeDemo(){
//        new ObjectInfoInnerHandlerImpl().updateObjectInfoTimeDemo(getAllRowKeyOfStaticRepo());
    }


    @Test
    public void testGetAllRowkeyFromHbase(){
        List<String[]> findResult = new ArrayList<>();
        Table objectinfo = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Scan scan = new Scan();
        try {
            ResultScanner resultScanner = objectinfo.getScanner(scan);
            for (Result result : resultScanner) {
                String rowKey = Bytes.toString(result.getRow());
                String pkey = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PKEY)));
                byte[] feature = result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.FEATURE));
                if (null != feature && feature.length == 2048) {
                    //将人员类型rowkey和特征值进行拼接
                    String feature_str = new String(feature, "ISO8859-1");
                    String[] result1 = new String[3];
                    result1[0] = rowKey;
                    result1[1] = pkey;
                    result1[2] = feature_str;
                    //将结果添加到集合中
                    findResult.add(result1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            HBaseUtil.closTable(objectinfo);
        }
//        return findResult;
    }

    @Test
    public void testGetAllRowKeyFromES(){
        List<String> findResult = new ArrayList<>();
        QueryBuilder qb = matchAllQuery();
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setQuery(qb)
//                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setSize(50000);
        SearchResponse searchResponse = requestBuilder.get();
        ArrayList<Future<List<String>>> results = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        do {
            synchronized (pool)
            {
                Future<List<String>> result = pool.submit(new EsDataGetter(searchResponse));
                results.add(result);
            }
            searchResponse = ElasticSearchHelper.getEsClient().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(new TimeValue(60000))
                    .execute()
                    .actionGet();
        } while (searchResponse.getHits().getHits().length != 0);
        List<String> finalResults = new ArrayList<>();
        try
        {
            for (Future<List<String>> result : results)
            {
                List<String> rd = result.get();
                finalResults.addAll(rd);
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        System.out.println(finalResults.size());
    }
    @Test
    public void testGetAllRowKeyFromESDemo(){
        QueryBuilder qb = matchAllQuery();
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setFetchSource(new String[]{"sex"}, null)
                .setQuery(qb);
        SearchResponse searchResponse = requestBuilder.get();
        long totalRecord = searchResponse.getHits().getTotalHits();
        long totalPages = (totalRecord % 5000 == 0)? (totalRecord / 5000):(totalRecord / 5000 + 1);

        ArrayList<Future<List<String>>> results = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(20);
        for (long i = 0; i < totalPages; i++){
            requestBuilder.setFetchSource(new String[]{"pkey", "updatetime", "feature"}, null)
                    .setFrom((int) i * 5000).setSize(5000);
            searchResponse = requestBuilder.get();
            synchronized (pool)
            {
                Future<List<String>> result = pool.submit(new EsDataGetter(searchResponse));
                results.add(result);
            }
        }
        List<String> finalResults = new ArrayList<>();
        try
        {
            for (Future<List<String>> result : results)
            {
                List<String> rd = result.get();
                finalResults.addAll(rd);
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        }
        System.out.println(finalResults.size());
    }
}

class EsDataGetter implements Callable<List<String>>{
    private SearchResponse searchResponse;
    public EsDataGetter(SearchResponse searchResponse){
        this.searchResponse = searchResponse;
    }

    @Override
    public List<String> call() throws Exception {
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        List<String> findResult = new ArrayList<>();
        if (searchHits.length > 0) {
            for (SearchHit hit : searchHits) {
                //得到每个人员类型对应的rowkey
                String id = hit.getId();
                //得到每个人员类型对应的特征值
                Map<String, Object> sourceList = hit.getSource();
                String updatetime = (String) sourceList.get("updatetime");
                String pkey = (String)sourceList.get("pkey");
                //将人员类型、rowkey和特征值进行拼接
                String result = id + "ZHONGXIAN" + pkey + "ZHONGXIAN" + updatetime;
                //将结果添加到集合中
                findResult.add(result);
            }
        }
        return findResult;
    }
}
