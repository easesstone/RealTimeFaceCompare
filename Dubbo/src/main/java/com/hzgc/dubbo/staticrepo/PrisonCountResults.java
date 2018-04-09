package com.hzgc.dubbo.staticrepo;

import java.io.Serializable;
import java.util.List;

public class PrisonCountResults implements Serializable{
    private List<PrisonCountResult> results;

    public PrisonCountResults() {
    }

    public List<PrisonCountResult> getResults() {
        return results;
    }

    public void setResults(List<PrisonCountResult> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "PrisonCountResults{" +
                "results=" + results +
                '}';
    }
}
