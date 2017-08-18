package com.hzgc.hbase.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class ObjectInfoInnerHandlerImpl implements ObjectInfoInnerHandler, Serializable{
    static {
        ElasticSearchHelper.getEsClient();
    }

    @Override
    public List<String> searchByPkeys(List<String> pkeys) {
        if ( pkeys == null) {
            List<String> findResult = new ArrayList<>();
            QueryBuilder qb = matchAllQuery();
            SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient().prepareSearch("objectinfo")
                    .setTypes("person").setExplain(true).setSize(10000).setQuery(qb);
            SearchResponse searchResponse = requestBuilder.get();
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            System.out.println("pkey为：null时，查询得到的记录数为：" + hits.getTotalHits());
            if (searchHits.length > 0) {
                for (SearchHit hit : searchHits) {
                    //得到每个人员类型对应的rowkey
                    String id = hit.getId();
                    //得到每个人员类型对应的特征值
                    Map<String, Object> sourceList = hit.getSource();
                    String pkey1 = (String)sourceList.get("pkey");
                    String feature = (String) sourceList.get("feature");
                    //当有特征值时，才将结果返回
                    if(null != feature && feature.length() == 2048){
                        //将人员类型rowkey和特征值进行拼接
                        String result = id + "ZHONGXIAN" + pkey1 + "ZHONGXIAN" + feature;
                        //将结果添加到集合中
                        findResult.add(result);
                    }
                }
            }
            return findResult;
        } else {
            //遍历人员类型
            Iterator it = pkeys.iterator();
            //构造搜索对象
            SearchResponse searchResponse;
            //定义一个List用来存在查询得到的结果
            List<String> findResult = new ArrayList<>();
            //设置搜索条件
            SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient().prepareSearch("objectinfo")
                    .setTypes("person").setExplain(true);
            while (it.hasNext()) {
                //取出遍历的值
                String a = (String) it.next();
                //根据遍历得到的人员类型进行精确查询
                requestBuilder.setQuery(QueryBuilders.termQuery("pkey", a));
                //通过requestBuilder的get方法执行查询任务
                searchResponse = requestBuilder.get();
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
            }
            return findResult;
        }
    }

    public  List<String> searchByPkeysUpdateTime(){
        List<String> findResult = new ArrayList<>();
        QueryBuilder qb = matchAllQuery();
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient().prepareSearch("objectinfo")
                .setTypes("person").setExplain(true).setSize(10000).setQuery(qb);
        SearchResponse searchResponse = requestBuilder.get();
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        System.out.println("pkey为：null时，查询得到的记录数为：" + hits.getTotalHits());
        if (searchHits.length > 0) {
            for (SearchHit hit : searchHits) {
                //得到每个人员类型对应的rowkey
                String id = hit.getId();
                //得到每个人员类型对应的特征值
                Map<String, Object> sourceList = hit.getSource();
                String updatetime = (String) sourceList.get("updatetime");
                //将人员类型、rowkey和特征值进行拼接
                String result = id + "ZHONGXIAN" + updatetime;
                //将结果添加到集合中
                findResult.add(result);
            }
        }
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
