package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.attribute.Attribute;
import com.hzgc.dubbo.attribute.AttributeValue;
import com.hzgc.dubbo.attribute.Logistic;
import com.hzgc.dubbo.dynamicrepo.*;

import java.util.ArrayList;
import java.util.List;

public class CptureNumberImpTest1 {
    public static void main(String[] args) {
        SearchOption searchOption = new SearchOption();
        List<String> deviceIds = new ArrayList<>();
        //deviceIds.add("3B0383FPAG00883");
        deviceIds.add("DS-2DE72XYZIW-ABCVS20160823CCCH641752612");
        deviceIds.add("3K01E84PAU00150");
        //deviceIds.add("2L04129PAU01933");
        deviceIds.add("DS-2CD2T20FD-I320160122AACH571485690");
        searchOption.setDeviceIds(deviceIds);
        List<SortParam> sortParams = new ArrayList<>();
        sortParams.add(SortParam.IPC);
        sortParams.add(SortParam.TIMEDESC);
        searchOption.setSortParams(sortParams);
        searchOption.setStartDate("2018-03-12 00:00:00");
        searchOption.setEndDate("2018-03-27 18:00:00");
        searchOption.setOffset(0);
        searchOption.setCount(10);

       List<AttributeValue> list1 = new ArrayList<>();
       AttributeValue attributeValue1 = new AttributeValue();
       attributeValue1.setValue(0);
       attributeValue1.setDesc("无");
       attributeValue1.setCount(0);
       list1.add(attributeValue1);
       Attribute attribute1 = new Attribute();
       attribute1.setLogistic(Logistic.AND);
       attribute1.setDesc("发色");
       attribute1.setIdentify("HairColor");
       attribute1.setValues(list1);

        List<AttributeValue> list2 = new ArrayList<>();
        AttributeValue attributeValue2 = new AttributeValue();
        attributeValue2.setValue(0);
        attributeValue2.setDesc("无");
        attributeValue2.setCount(0);
        list2.add(attributeValue1);
        Attribute attribute2 = new Attribute();
        attribute2.setLogistic(Logistic.AND);
        attribute2.setDesc("发型");
        attribute2.setIdentify("HairStyle");
        attribute2.setValues(list2);

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(attribute1);
        attributes.add(attribute2);
        searchOption.setAttributes(attributes);

        CapturePictureSearchServiceImpl capturePictureSearchService = new CapturePictureSearchServiceImpl();
        List<SearchResult> list = capturePictureSearchService.getCaptureHistory(searchOption);
        for (SearchResult s : list) {
            List<SingleResult> singlelist = s.getResults();
            for (SingleResult ss : singlelist) {
                System.out.println(ss.getTotal()+"============");
                List<CapturedPicture> capturedPictures = ss.getPictures();
                for (CapturedPicture c : capturedPictures) {
                    String ab = c.getSurl();
                    System.out.println(ab);
                }
            }
        }
    }
}
