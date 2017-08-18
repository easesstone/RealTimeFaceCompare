package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.SearchResult;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.util.ObjectListSort.ListUtils;
import com.hzgc.util.ObjectListSort.SortParam;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CapturePictureSearchServiceImpl test class
 */
public class CapturePictureTest {

    private static final String searchId = "11c32d1be6d64caf8f6d98e10394d633";
    private static final int offse = 1;
    private static final int count = 10;
    private static final String sortParams = "+ipcId,-similarity,+timeStamp";


    @Test
    public void insertPerson() {
        Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
        try {
            List<Put> putList = new ArrayList<>();
            Put put = new Put(Bytes.toBytes("17130NCY0HZ0004-0_00000000000000_170801160015_0000001111_00"));
            put.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE, Bytes.toBytes("这是图片"));
            put.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID, Bytes.toBytes("17130NCY0HZ0004-0"));
            put.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA, Bytes.toBytes("附加信息"));
            put.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE, Bytes.toBytes("描述信息"));
            put.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_TIMESTAMP, Bytes.toBytes("1111111111111"));
            put.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA, Bytes.toBytes("2015"));

            Put put1 = new Put(Bytes.toBytes("17130NCY0HZ0004-0_00000000000000_170801160015_0000001111_01"));
            put1.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE, Bytes.toBytes("这是图片"));
            put1.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID, Bytes.toBytes("17130NCY0HZ0004-0"));
            put1.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA, Bytes.toBytes("附加信息"));
            put1.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE, Bytes.toBytes("描述信息"));
            put1.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_TIMESTAMP, Bytes.toBytes("2222222222222"));
            put1.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA, Bytes.toBytes("2016"));

            Put put2 = new Put(Bytes.toBytes("17130NCY0HZ0004-0_00000000000000_170801160015_0000001111_02"));
            put2.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE, Bytes.toBytes("这是图片"));
            put2.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID, Bytes.toBytes("17130NCY0HZ0004-0"));
            put2.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA, Bytes.toBytes("附加信息"));
            put2.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE, Bytes.toBytes("描述信息"));
            put2.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_TIMESTAMP, Bytes.toBytes("3333333333333"));
            put2.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA, Bytes.toBytes("2017"));

            Put put3 = new Put(Bytes.toBytes("4SFDSF5185FD15F-0_00000000000000_170801160015_0000001111_00"));
            put3.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE, Bytes.toBytes("这是图片"));
            put3.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID, Bytes.toBytes("4SFDSF5185FD15F-0"));
            put3.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA, Bytes.toBytes("附加信息"));
            put3.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE, Bytes.toBytes("描述信息"));
            put3.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_TIMESTAMP, Bytes.toBytes("1111111111111"));
            put3.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA, Bytes.toBytes("2018"));

            Put put4 = new Put(Bytes.toBytes("4SFDSF5185FD15F-0_00000000000000_170801160015_0000001111_01"));
            put4.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE, Bytes.toBytes("这是图片"));
            put4.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID, Bytes.toBytes("4SFDSF5185FD15F-0"));
            put4.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA, Bytes.toBytes("附加信息"));
            put4.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE, Bytes.toBytes("描述信息"));
            put4.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_TIMESTAMP, Bytes.toBytes("2222222222222"));
            put4.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA, Bytes.toBytes("2019"));

            Put put5 = new Put(Bytes.toBytes("4SFDSF5185FD15F-0_00000000000000_170801160015_0000001111_02"));
            put5.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE, Bytes.toBytes("这是图片"));
            put5.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID, Bytes.toBytes("4SFDSF5185FD15F-0"));
            put5.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA, Bytes.toBytes("附加信息"));
            put5.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE, Bytes.toBytes("描述信息"));
            put5.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_TIMESTAMP, Bytes.toBytes("3333333333333"));
            put5.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_FEA, Bytes.toBytes("2020"));

            putList.add(put);
            putList.add(put1);
            putList.add(put2);
            putList.add(put3);
            putList.add(put4);
            putList.add(put5);

            person.put(putList);

            System.out.println("--------------------------插入成功--------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("--------------------------失败--------------------------");
        } finally {
            HBaseUtil.closTable(person);
        }
    }


    @Test
    public void insertSearchResTest() {
        Map<String, Float> map = new HashMap<>();
        map.put("4SFDSF5185FD15F-0_00000000000000_170801160015_0000001111_00", 0.66224F);
        map.put("4SFDSF5185FD15F-0_00000000000000_170801160015_0000001111_01", 0.66224F);
        map.put("4SFDSF5185FD15F-0_00000000000000_170801160015_0000001111_02", 0.49546F);

        map.put("17130NCY0HZ0004-0_00000000000000_170801160015_0000001111_00", 0.12546F);
        map.put("17130NCY0HZ0004-0_00000000000000_170801160015_0000001111_01", 0.55465F);
        map.put("17130NCY0HZ0004-0_00000000000000_170801160015_0000001111_02", 0.55465F);

        DynamicPhotoServiceImpl dynamicPhotoService = new DynamicPhotoServiceImpl();
        boolean bb = dynamicPhotoService.insertSearchRes(searchId, "156156", map);
        System.out.println(bb);
    }

    @Test
    public void getSearchResultTest() {
        CapturePictureSearchServiceImpl capturePictureSearchService = new CapturePictureSearchServiceImpl();
        SearchResult searchResult = capturePictureSearchService.getSearchResult(searchId, offse, count, sortParams);
        System.out.println(searchResult);
    }

    @Test
    public void getOrderStringBySortTest() {
        String sortParams = "+ipcId,-similarity,+timeStamp";
        SortParam sortParam = ListUtils.getOrderStringBySort(sortParams);
        System.out.println(sortParam);
    }

}
