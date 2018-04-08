package com.hzgc.dubbo.staticrepo;

import java.util.Map;

/**
 *
 */
public class PrisonCountResult {
    private String pkey;
    private Map<String, Integer> locationCounts;

    public PrisonCountResult() {
    }

    public String getPkey() {
        return pkey;
    }

    public void setPkey(String pkey) {
        this.pkey = pkey;
    }

    public Map<String, Integer> getLocationCounts() {
        return locationCounts;
    }

    public void setLocationCounts(Map<String, Integer> locationCounts) {
        this.locationCounts = locationCounts;
    }

    @Override
    public String toString() {
        return "PrisonCountResult{" +
                "pkey='" + pkey + '\'' +
                ", locationCounts=" + locationCounts +
                '}';
    }
}
