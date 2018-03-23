package com.hzgc.dubbo.staticrepo;

import java.io.Serializable;

/**
 * 静态库的排序参数
 */
public enum StaticSortParam implements Serializable{
    PEKEY,       //人员类型
    TIMEASC,     //时间升序
    TIMEDESC,    //时间降序
    RELATEDASC,     //相似度升序
    RELATEDDESC,    //相似度降序
    IMPORTANTASC,   //重点人员升序
    IMPORTANTDESC    // 重点人员降序
}
