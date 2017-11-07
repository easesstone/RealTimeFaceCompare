package com.hzgc.dubbo.Attribute;

/**
 * 属性值
 */
public class AttributeValue {
    /**
     * 属性的值
     */
    private Integer value;
    /**
     * 值描述
     */
    private String desc;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "AttributeValue{" +
                "value=" + value +
                ", desc='" + desc + '\'' +
                '}';
    }
}
