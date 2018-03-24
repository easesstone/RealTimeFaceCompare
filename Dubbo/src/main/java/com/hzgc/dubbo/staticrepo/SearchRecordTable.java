package com.hzgc.dubbo.staticrepo;

import java.io.Serializable;

public class SearchRecordTable implements Serializable {
    public static final String TABLE_NAME ="searchrecord";  // 表示的是历史记录的表格名字
    public static final String RESULT = "result";        // 搜索结果
    public static final String ID = "id"; // 表格的id
    public static final String RECORDDATE="indate";  // 历史记录保存的时间
}
