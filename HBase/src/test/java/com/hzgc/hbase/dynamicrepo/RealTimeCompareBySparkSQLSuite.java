package com.hzgc.hbase.dynamicrepo;

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
        SearchResult result = capture.search(option);
        System.out.println(result);
    }
}
