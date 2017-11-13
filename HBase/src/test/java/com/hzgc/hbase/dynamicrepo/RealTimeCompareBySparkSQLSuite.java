package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.CapturedPicture;
import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchResult;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.jni.FaceFunction;

public class RealTimeCompareBySparkSQLSuite {
    public static void main(String[] args) {
        CapturePictureSearchServiceImpl capture = new CapturePictureSearchServiceImpl();
        byte[] image = FaceFunction.inputPicture("/opt/GsFaceLib/example/picture.jpg");
        SearchOption option = new SearchOption();
        option.setImage(image);
        option.setSearchType(SearchType.PERSON);
        option.setThreshold(30);
        option.setStartDate("1990-10-22 12:30:20");
        option.setEndDate("2017-11-10 18:35:20");
        option.setCount(30);
        SearchResult result = capture.search(option);
        for (CapturedPicture capturedPicture: result.getPictures()) {
            System.out.println(capturedPicture.getBurl());
        }
        System.out.println(result);
    }
}
