package com.hzgc.hbase.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.util.Bytes;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class ObjectInfoInnerHandlerImpl implements ObjectInfoInnerHandler, Serializable{
    static {
        ElasticSearchHelper.getEsClient();
    }

    private long totalNums;

    // 对外接口，用于判断HBase 中的静态信息库数据量是否有改变,true 表示有变化
    // false 表示没有变化
    public boolean totalNumIsChange(){
        if (totalNums == getTotalNums()){
            return false;
        } else {
            setTotalNums();
            return true;
        }
    }

    // 类内部使用方法
    private void setTotalNums(){
        totalNums = getTotalNums();
    }

    // 类内部使用方法
    public long getTotalNums(){
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

    //查询所有的数据，返回其中的rowkey 和 feature
    public List<String> searchByPkeys() {
        List<String> findResult = new ArrayList<>();
        Table objectinfo = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Scan scan = new Scan();
        try {
            ResultScanner resultScanner = objectinfo.getScanner(scan);
            Iterator<Result> iterator = resultScanner.iterator();
            while (iterator.hasNext()){
                Result result = iterator.next();
                String rowKey = Bytes.toString(result.getRow());
                String pkey = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PKEY)));
                byte[] feature = result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.FEATURE));
                if(null != feature && feature.length == 2048){
                    //将人员类型rowkey和特征值进行拼接
                    String feature_str = new String(feature, "ISO8859-1");
                    String result1 = rowKey + "ZHONGXIAN" + pkey + "ZHONGXIAN" + feature_str;
                    //将结果添加到集合中
                    findResult.add(result1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            HBaseUtil.closTable(objectinfo);
        }
        return findResult;
    }

    // 根据pkey 的List 来进行返回符合条件的数据，
    @Override
    public List<String> searchByPkeys(List<String> pkeys) {
        if (pkeys == null){
            return null;
        }
        //遍历人员类型
        Iterator it = pkeys.iterator();
        //构造搜索对象
        SearchResponse searchResponse;
        //定义一个List用来存在查询得到的结果
        List<String> findResult = new ArrayList<>();
        //设置搜索条件
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(6000))
                .setSize(1000)
                .setExplain(true);
        while (it.hasNext()) {
            //取出遍历的值
            String a = (String) it.next();
            //根据遍历得到的人员类型进行精确查询
            requestBuilder.setQuery(QueryBuilders.termQuery(ObjectInfoTable.PKEY, a));
            //通过requestBuilder的get方法执行查询任务
            searchResponse = requestBuilder.get();
            do {
                //将结果进行封装
                SearchHits hits = searchResponse.getHits();
                //输出某个人员类型对应的记录条数
                SearchHit[] searchHits = hits.getHits();
                System.out.println("pkey为：" + a + "时，查询得到的记录数为：" + hits.getTotalHits());
                if (searchHits.length > 0) {
                    for (SearchHit hit : searchHits) {
                        //得到每个人员类型对应的rowkey
                        String id = hit.getId();
                        //得到每个人员类型对应的特征值
                        Map<String, Object> sourceList = hit.getSource();
                        String feature = (String) sourceList.get("feature");
                        //当有特征值时，才将结果返回
                        if (null != feature) {
                            //将人员类型、rowkey和特征值进行拼接
                            String result = id + "ZHONGXIAN" + a + "ZHONGXIAN" + feature;
                            //将结果添加到集合中
                            findResult.add(result);
                        }
                    }
                }
                searchResponse = ElasticSearchHelper.getEsClient().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(new TimeValue(6000))
                        .execute()
                        .actionGet();
            } while (searchResponse.getHits().getHits().length != 0);
        }
        return findResult;
    }

    public  List<String> searchByPkeysUpdateTime(){
        List<String> findResult = new ArrayList<>();
        QueryBuilder qb = matchAllQuery();
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setQuery(qb)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(6000))
                .setSize(1000)
                .setExplain(true);
        SearchResponse searchResponse = requestBuilder.get();
        do {
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
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
            searchResponse = ElasticSearchHelper.getEsClient().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(new TimeValue(6000))
                    .execute()
                    .actionGet();
        } while (searchResponse.getHits().getHits().length != 0);
        return findResult;
   }

    public  List<String> searchByPkeysUpdateTime(List<String> pkeys){
        List<String> findResult = new ArrayList<>();
        QueryBuilder qb = QueryBuilders.termsQuery(ObjectInfoTable.PKEY, pkeys);
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setQuery(qb)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(6000))
                .setSize(1000)
                .setExplain(true);
        SearchResponse searchResponse = requestBuilder.get();
        do {
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
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
            searchResponse = ElasticSearchHelper.getEsClient().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(new TimeValue(6000))
                    .execute()
                    .actionGet();
        } while (searchResponse.getHits().getHits().length != 0);
        return findResult;
    }

    public int updateObjectInfoTime(String rowkey) {
        // 获取table 对象，通过封装HBaseHelper 来获取
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Put put = new Put(Bytes.toBytes(rowkey));
        // 获取系统当前时间
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = format.format(date);
        // 构造一个更新对象信息中的更新时间段的put
        put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                Bytes.toBytes(ObjectInfoTable.UPDATETIME), Bytes.toBytes(dateString));
        try {
            // 更新对象信息中的更新时间。
            table.put(put);
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        } finally {
            HBaseUtil.closTable(table);
        }
        return 0;
    }
}
