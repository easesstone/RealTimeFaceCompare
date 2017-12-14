package com.hzgc.service.dynamicrepo;

import com.hzgc.service.staticrepo.ElasticSearchHelper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * Created by Administrator on 2017-12-13.
 */
public class test {
    public static void main(String[] args) {
        SearchResponse searchResponse = ElasticSearchHelper.getEsClient().prepareSearch("dynamic").setTypes("person")
                .setQuery(QueryBuilders.matchQuery("ipcid","ckqkf")).get();
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();
        System.out.println("总数：" + totalHits);
        SearchHit[] hits2 = hits.getHits();
        for(SearchHit searchHit : hits2){
            System.out.println(searchHit.getSourceAsString());
            System.out.println(searchHit.getId());
        }
    }
}
