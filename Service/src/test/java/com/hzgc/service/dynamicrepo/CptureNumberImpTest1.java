package com.hzgc.service.dynamicrepo;

import com.hzgc.service.staticrepo.ElasticSearchHelper;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CptureNumberImpTest1 {
    public static void main(String[] args) {
        List<String> lists = new ArrayList<>();
        lists.add("3K01E84PAU00083");
        lists.add("3K01E84PAU00498");
        lists.add("2L04129PAU01933");
        String startTime = "2018-02-01 10:00:00";
        String endTime = "2018-03-02 11:00:00";
       CptureNumberImpl cptureNumber = new CptureNumberImpl();
       Map<String,Integer> map = cptureNumber.timeSoltNumber(lists,startTime,endTime);
        System.out.println(map);
//        System.out.println(Integer.MAX_VALUE);
//        CptureNumberImpl cptureNumber = new CptureNumberImpl();
//        Map<String,Integer> map = cptureNumber.dynaicNumberService(lists);
//        Map<String,Integer> map1 = cptureNumber.staticNumberService("0001");
//        System.out.println(map1);
    }
}
