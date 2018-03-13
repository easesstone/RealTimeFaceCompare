package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;
import java.util.List;

public class SearchResult implements Serializable {
    private String searchId;    //总搜索ID

    private List<SingleResult> results;    //子结果集集合

    private String searchType;    //搜索类型

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public List<SingleResult> getResults() {
        return results;
    }

    public void setResults(List<SingleResult> results) {
        this.results = results;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }
}

