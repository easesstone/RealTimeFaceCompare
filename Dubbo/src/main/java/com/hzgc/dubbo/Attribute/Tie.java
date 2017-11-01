package com.hzgc.dubbo.Attribute;

import java.io.Serializable;

/**
 * 是否系领带：0->无；1->系领带；2->没有系领带；
 */
public enum Tie implements Serializable{
    None(0), Tie_y(1), Tie_n(2);

    private int tievalue;

    private Tie(int tievalue) {
        this.tievalue = tievalue;
    }

    public int getTievalue() {
        return tievalue;
    }

    public void setTievalue(int tievalue) {
        this.tievalue = tievalue;
    }

    public String toString() {
        return "TieValue{" + "value=" + tievalue + '}';
    }

}
