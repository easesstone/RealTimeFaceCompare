package com.hzgc.collect.expand.merge;

import com.hzgc.collect.expand.conf.CommonConf;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FindDiffRowsTest2 {

    private List<String> allContent;
    private MergeUtil mergeUtil;
    private CommonConf conf;

    @Before
    public void before() {
        mergeUtil = new MergeUtil();
        conf = new CommonConf();
        String processLogDir = conf.getProcessLogDir();
        List<String> processLogPaths = mergeUtil.listAllFileAbsPath(processLogDir);
        for (String processPath : processLogPaths) {
            String receiveLogPath = mergeUtil.getRecFilePathFromProFile(processPath);
            allContent = mergeUtil.getAllContentFromFile(processPath, receiveLogPath);
        }
    }

    @Test
    public void getAllDifRows() {
        System.out.println("=====第1组测试,合并后集合中只有receive文件中的一条数据=====");
        System.out.println("合并后集合大小：" + allContent.size());
        FindDiffRows findDiffRows = new FindDiffRows();
        List<String> allDiffRows = findDiffRows.getAllDiffRows(allContent);
        System.out.println("=====第一组测试中所有不同的数据有======");
        for (String row : allDiffRows) {
            System.out.println(row);
        }

    }

    @Test
    public void getAllDifRows2() {

        System.out.println("=====第2组测试,合并后集合中只有最后一条元素为未处理数据=====");
        System.out.println("合并后集合大小：" + allContent.size());
        FindDiffRows findDiffRows = new FindDiffRows();
        List<String> allDiffRows = findDiffRows.getAllDiffRows(allContent);
        System.out.println("=====第2组测试中所有不同的数据有======");
        for (String row : allDiffRows) {
            System.out.println(row);
        }

    }

    @Test
    public void getAllDifRows3() {

        System.out.println("=====第3组测试,合并后集合中未处理数据的序号为乱序的=====");
        System.out.println("合并后集合大小：" + allContent.size());
        FindDiffRows findDiffRows = new FindDiffRows();
        List<String> allDiffRows = findDiffRows.getAllDiffRows(allContent);
        System.out.println("=====第3组测试中所有不同的数据有======");
        for (String row : allDiffRows) {
            System.out.println(row);
        }

    }

    @Test
    public void getAllDifRows4() {

        System.out.println("=====第4组测试,入参为空=====");
        System.out.println("合并后集合大小：" + allContent.size());
        FindDiffRows findDiffRows = new FindDiffRows();
        allContent = new ArrayList<>();
        //allContent = null;
        List<String> allDiffRows = findDiffRows.getAllDiffRows(allContent);
        System.out.println("=====第4组测试中所有不同的数据有======");
        for (String row : allDiffRows) {
            System.out.println(row);
        }

    }

}
