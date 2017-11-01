package com.hzgc.dubbo.Attribute;


import java.io.Serializable;

/**
 * 头发颜色：0->无；1->金色；2->黑色；3->棕色；4->灰白
 */
public enum HairColor implements Serializable{
    None(0), Blond(1), Black(2), Brown(3), Gray(4);

    private int haircolorvalue;

    private HairColor(int haircolorvalue) {
        this.haircolorvalue = haircolorvalue;
    }

    public int getHaircolor() {
        return haircolorvalue;
    }

    public void setHaircolor(int haircolorvalue) {
        this.haircolorvalue = haircolorvalue;
    }

    public String toString() {
        return "HairColor{" + "color=" + haircolorvalue + '}';
    }
}
