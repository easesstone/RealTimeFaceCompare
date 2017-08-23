package com.hzgc.streaming.addData;

import com.hzgc.dubbo.device.WarnRule;
import com.hzgc.hbase.device.DeviceUtilImpl;

import java.util.ArrayList;
import java.util.List;

public class PutDataDemo {
    public List<WarnRule> setwarnRuleData() {
        List<WarnRule> rules = new ArrayList<WarnRule>();
//        WarnRule warnRule1 = new WarnRule();
//        warnRule1.setCode(0);
//        warnRule1.setDayThreshold(3);
//        warnRule1.setThreshold(70);
//        warnRule1.setObjectType("123456");
//        rules.add(warnRule1);
//        WarnRule warnRule2 = new WarnRule();
//        warnRule2.setCode(0);
//        warnRule2.setDayThreshold(3);
//        warnRule2.setThreshold(70);
//        warnRule2.setObjectType("123457");
//        rules.add(warnRule2);
//        WarnRule warnRule3 = new WarnRule();
//        warnRule3.setCode(0);
//        warnRule3.setDayThreshold(3);
//        warnRule3.setThreshold(70);
//        warnRule3.setObjectType("223458");
//        rules.add(warnRule3);
        WarnRule warnRule1 = new WarnRule();
        warnRule1.setCode(0);
        // warnRule1.setDayThreshold(3);
        warnRule1.setThreshold(70);
        warnRule1.setObjectType("123456");
        rules.add(warnRule1);
        WarnRule warnRule2 = new WarnRule();
        warnRule2.setCode(0);
        // warnRule2.setDayThreshold(3);
        warnRule2.setThreshold(70);
        warnRule2.setObjectType("123457");
        rules.add(warnRule2);
        WarnRule warnRule3 = new WarnRule();
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
//        WarnRuleServiceImpl warnRuleService = new WarnRuleServiceImpl();
//        PutDataDemo pd = new PutDataDemo();
//        warnRuleService.addRules(pd.setIpcId(),pd.setwarnRuleData());
//       DeviceUtilImpl deviceUtilI = new DeviceUtilImpl();
//        Map<Integer, Map<String, Integer>> mp = deviceUtilI.isWarnTypeBinding("17130NCY0HZ0006-T");
//
//        System.out.println(mp.get(2));
//        System.out.println(mp.get(2)==null);
//        System.out.println(mp.get(2).keySet());
//        Set set = mp.get(2).keySet();
//        Iterator it = set.iterator();
//        for (Object str : set) {
//            System.out.println(mp.get(2).get(str));
//            System.out.println(str);
//        }
//
//        while(it.hasNext()){
//            Object obj = it.next();
//           System.out.println(obj);
//           System.out.println(mp.get(0).get(obj));
//        }
//        DeviceServiceImpl dl = new DeviceServiceImpl();
//        dl.bindDevice("0002","17130NCY0HZ0009-T","");
////        "223458","123456","123457"
//        List list = new ArrayList();
//        list.add("223458");
//        list.add("123456");
//        list.add("123457");
//        ObjectInfoInnerHandlerImpl ol = new ObjectInfoInnerHandlerImpl();
//        List l=ol.searchByPkeys(list);
//        System.out.println("数量："+l.size());
//        List l = ol.searchByPkeys(null);
//        System.out.println(l);
//        ObjectInfoInnerHandlerImpl ol = new ObjectInfoInnerHandlerImpl();
//        System.out.println(ol.totalNumIsChange());
//        System.out.println(map);
//        Set set = map.keySet();
//        System.out.println(set);
        //5570b7b3b9b14a108e01931544ed49d5
//        ObjectInfoInnerHandlerImpl objectInfoInnerHandlerImpl = new ObjectInfoInnerHandlerImpl();
//        int ss = objectInfoInnerHandlerImpl.updateObjectInfoTime("5570b7b3b9b14a108e01931544ed49d5");
//        System.out.println(ss);
//        DeviceUtilImpl deviceUtilI = new DeviceUtilImpl();
//        Map<Integer, Map<String, Integer>> mp = deviceUtilI.isWarnTypeBinding("17130NCY0HZ0009-T");
//        Map m = mp.get(1);
////      Map ms = mp.get(0).get("123457");
//        System.out.println(mp);
//        System.out.println(m);
//        DeviceUtilImpl dl = new DeviceUtilImpl();
//        Map<String, Map<String, Integer>> map = dl.getThreshold();
//        System.out.println(map);
//        System.out.println(map.keySet());
//        List list = new ArrayList(map.keySet());
//        System.out.println("--------------------");
//        System.out.println(list);
//        ObjectInfoInnerHandlerImpl ol = new ObjectInfoInnerHandlerImpl();
//        List list  = new ArrayList<String>();
//        //list.add("123456");
//        list.add("223458");
//        System.out.println(ol.searchByPkeysUpdateTime(list).size());
//        String ss= "12345678";
//        System.out.println(ss.substring(0,4));



        DeviceUtilImpl dl = new DeviceUtilImpl();
        System.out.println(dl.isWarnTypeBinding("17130NCY0HZ0003-T"));
        
    }
}
