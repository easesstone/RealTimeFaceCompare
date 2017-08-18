package com.hzgc.hbase.staticreposuite;

import com.hzgc.dubbo.staticrepo.ObjectInfoHandler;
import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.dubbo.staticrepo.PSearchArgsModel;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import com.hzgc.hbase.staticrepo.ObjectInfoHandlerImpl;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
}
