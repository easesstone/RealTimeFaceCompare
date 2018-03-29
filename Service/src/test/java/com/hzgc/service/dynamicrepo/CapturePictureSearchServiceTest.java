package com.hzgc.service.dynamicrepo;

import java.util.ArrayList;
import java.util.List;

public class CapturePictureSearchServiceTest {
    public static void main(String[] args) {
        CapturePictureSearchServiceImpl capturePictureSearchService = new CapturePictureSearchServiceImpl();
        String startTime = "2018-03-12 00:00:00";
        String endTime = "2018-03-28 11:59:59";
        List<String> ipcIdList = new ArrayList<>();
        ipcIdList.add("DS-2DE72XYZIW-ABCVS20160823CCCH641752612");
        ipcIdList.add("3B0383FPAG00883");
        ipcIdList.add("3K01E84PAU00150");
        ipcIdList.add("DS-2CD2T20FD-I320160122AACH571485690");
        Long a = capturePictureSearchService.getCaptureNumber(startTime,endTime,ipcIdList);
        System.out.println(a);
    }
}
