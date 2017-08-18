package com.hzgc.hbase.staticreposuite;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class EsDataSuite {
    @Test
    public void testPutFloatArrayToEs() throws IOException {
        Client client = ElasticSearchHelper.getEsClient();
        IndexResponse response = client.prepareIndex("twitter", "tweet", "1000")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("user" ,"enhaye")
                        .field("postDate", new float[]{1.23411321341f, 1.09f, 2.3454f, 1.0939309f})
                        .endObject()).get();

        String _index = response.getIndex();
        String _type = response.getType();
        String _id = response.getId();
        long _version = response.getVersion();
        RestStatus status = response.status();
        System.out.println("_index: " + _index  + ", _type: " + _type + ", _id： "
                + _id + "_version: " + _version  + "status: " + status );
    }

    @Test
    public void testScrollsSearch(){
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder builder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(300000))
                .setExplain(true).setSize(200);
        QueryBuilder qb = QueryBuilders.matchQuery("name", "花");
        builder.setQuery(qb);
        SearchResponse response = builder.get();
        do {
            System.out.println("Search  total " + response.getHits().getTotalHits());
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(new TimeValue(300000))
                    .execute()
                    .actionGet();
        }while (response.getHits().getHits().length != 0);
    }
}
