package com.hzgc.service.dynamicrepo;

import com.hzgc.service.staticrepo.ElasticSearchHelper;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018-2-1.
 */
public class CptureNumberImpTest1 {
    public static void main(String[] args) {
        List<String> lists = new ArrayList<>();
        lists.add("3K01E84PAU00083");
        lists.add("3K01E84PAU00498");
        lists.add("2L04129PAU01933");
        String startTime = "2018-02-01 10:00:00";
        String endTime = "2018-02-01 11:00:00";
        if (lists != null && lists.size() > 0) {
            for (String list : lists) {
                //查询动态库中数据
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                boolQueryBuilder.must(QueryBuilders.matchQuery(DynamicTable.IPCID, list));
                boolQueryBuilder.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gte(startTime).lte(endTime));
                SearchRequestBuilder searchRequestBuilder = ElasticSearchHelper.getEsClient()
                        .prepareSearch(DynamicTable.DYNAMIC_INDEX)
                        .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                        .setQuery(boolQueryBuilder);
                SearchResponse searchResponse = searchRequestBuilder.get();
                SearchHits searchHits = searchResponse.getHits();
                int number = (int) searchHits.getTotalHits();
                //将数据插入新表中
                Map<String, Object> map = new HashMap<>();
                map.put("ipcid", list);
                map.put("time", endTime);
                map.put("count", number);
                IndexResponse indexResponse = ElasticSearchHelper.getEsClient()
                        .prepareIndex("dynamicshow", "person")
                        .setSource(map)
                        .get();
                System.out.println(indexResponse.getVersion());
            }
        }
    }
}
