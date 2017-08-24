package com.hzgc.streaming.addData;

import com.hzgc.dubbo.device.WarnRule;
import com.hzgc.hbase.device.DeviceUtilImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟规则库（设定测试规则                                                       ）
 */
public class PutDataDemo {
    public List<WarnRule> setwarnRuleData() {
        List<WarnRule> rules = new ArrayList<WarnRule>();
        WarnRule warnRule1 = new WarnRule();
        warnRule1.setCode(0);
        warnRule1.setDayThreshold(3);
        warnRule1.setThreshold(70);
        warnRule1.setObjectType("123456");
        rules.add(warnRule1);
        WarnRule warnRule2 = new WarnRule();
        warnRule2.setCode(0);
        warnRule2.setDayThreshold(3);
        warnRule2.setThreshold(70);
        warnRule2.setObjectType("123457");
        rules.add(warnRule2);
        WarnRule warnRule3 = new WarnRule();
        warnRule3.setCode(0);
        warnRule3.setDayThreshold(3);
        warnRule3.setThreshold(70);
        warnRule3.setObjectType("223458");
        rules.add(warnRule3);
        WarnRule warnRule4 = new WarnRule();
        warnRule1.setCode(0);
        // warnRule1.setDayThreshold(3);
        warnRule1.setThreshold(70);
        warnRule1.setObjectType("123456");
        rules.add(warnRule1);
        WarnRule warnRule5 = new WarnRule();
        warnRule2.setCode(0);
        // warnRule2.setDayThreshold(3);
        warnRule2.setThreshold(70);
        warnRule2.setObjectType("123457");
        rules.add(warnRule2);
        WarnRule warnRule6 = new WarnRule();
        warnRule3.setCode(0);
        //warnRule3.setDayThreshold(3);
        warnRule3.setThreshold(70);
        warnRule3.setObjectType("223458");
        rules.add(warnRule3);
        return rules;
    }

    public List<String> setIpcId() {
        /**
         * 17130NCY0HZ0001-T
         * 17130NCY0HZ0002-T
         * 17130NCY0HZ0003-T
         */
        List<String> list = new ArrayList<>();
        list.add("17130NCY0HZ0009-T");
        return list;
    }

    public static void main(String[] args) {
        DeviceUtilImpl dl = new DeviceUtilImpl();
        System.out.println(dl.isWarnTypeBinding("17130NCY0HZ0003-T"));

    }
}
