package com.hzgc.service.staticrepo;

import java.io.Serializable;

public class StaticRepoObject implements Serializable {
    private float[] feature;
    private String pkey;
    private String rowkey;

    public float[] getFeature() {
        return feature;
    }

    public void setFeature(float[] feature) {
        this.feature = feature;
    }

    public String getPkey() {
        return pkey;
    }

    public void setPkey(String pkey) {
        this.pkey = pkey;
    }

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }
}
