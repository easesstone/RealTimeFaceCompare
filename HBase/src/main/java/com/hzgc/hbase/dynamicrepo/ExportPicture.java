package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 导出动态库图片数据到本地（E:\photo）
 */
public class ExportPicture {

    private static void getBatchCaptureMessage(List<String> imageIdList) {
        if (imageIdList != null) {
            Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            List<Get> gets = new ArrayList<>();
            try {
                for (int i = 0, len = imageIdList.size(); i < len; i++) {
                    Get get = new Get(Bytes.toBytes(imageIdList.get(i)));
                    get.addColumn(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                    gets.add(get);
                }
                Result[] results = person.get(gets);
                if (results != null) {
                    System.out.println("开始写入");
                    for (Result result : results) {
                        if (result != null) {
                            String rowKey = Bytes.toString(result.getRow());
                            byte[] imageData = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                            String path1 = "E:\\photo\\" + rowKey + ".jpg";
                            byte2image(imageData, path1);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                HBaseUtil.closTable(person);
                System.out.println("结束...........");
            }
        }

    }

    //byte数组到图片
    private static void byte2image(byte[] data, String path) {
        if (data == null || path.equals("")) return;
        try {
            FileImageOutputStream imageOutput = new FileImageOutputStream(new File(path));
            imageOutput.write(data, 0, data.length);
            imageOutput.close();
            System.out.println("Make Picture success,Please find image in " + path);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SearchOption option = new SearchOption();
        option.setSearchType(SearchType.PERSON);
        FilterByRowkey filterByRowkey = new FilterByRowkey();
        List<String> imageIdList = filterByRowkey.getRowKey(option);
        getBatchCaptureMessage(imageIdList);
    }
}
