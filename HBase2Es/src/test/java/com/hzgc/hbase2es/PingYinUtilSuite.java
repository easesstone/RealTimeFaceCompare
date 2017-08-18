package com.hzgc.hbase2es;

import com.hzgc.util.PinYinUtil;
import org.junit.Test;

public class PingYinUtilSuite {
    @Test
    public void testHanYuPingYinUtil(){
        System.out.println(PinYinUtil.toHanyuPinyin("ni王重ma阳ma?/重庆@重庆魔都"));
        System.out.println(PinYinUtil.toHanyuPinyin("花1满天2"));
    }
}
