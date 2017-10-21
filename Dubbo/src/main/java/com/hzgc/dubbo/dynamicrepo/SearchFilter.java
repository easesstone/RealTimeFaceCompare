package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;

/**
 * 搜索过滤条件
 */
public class SearchFilter implements Serializable {
    /**
     * 过滤参数：比如头发颜色
     */
    private String param;
    /**
     * 参数类型：黄色、棕色、黑色、白色等
     */
    private String type;
    /**
     * 与其他条件的拼接运算，OR或者AND操作
     */
    private String logic;

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLogic() {
        return logic;
    }

    public void setLogic(String logic) {
        this.logic = logic;
    }

    @Override
    public String toString() {
        return "SearchFilter{" +
                "param='" + param + '\'' +
                ", type='" + type + '\'' +
                ", logic='" + logic + '\'' +
                '}';
    }
}
