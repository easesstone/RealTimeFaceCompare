package com.hzgc.hbase.device;

import com.hzgc.dubbo.device.DeviceService;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.util.StringUtil;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

public class DeviceServiceImpl implements DeviceService {
    private static Logger LOG = Logger.getLogger(DeviceServiceImpl.class);

    @Override
    public boolean bindDevice(String platformId, String ipcID, String notes) {
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        if (StringUtil.strIsRight(ipcID) && StringUtil.strIsRight(platformId)) {
            String ipcIDTrim = ipcID.trim();
            try {
                Put put = new Put(Bytes.toBytes(ipcIDTrim));
                put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.PLAT_ID, Bytes.toBytes(platformId));
                put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.NOTES, Bytes.toBytes(notes));
                table.put(put);
                LOG.info("Put data[" + ipcIDTrim + ", " + platformId + "] successful");
                return true;
            } catch (Exception e) {
                LOG.error("Current bind is failed!");
                return false;
            } finally {
                HBaseUtil.closTable(table);
            }
        } else {
            LOG.error("Please check the arguments!");
            return false;
        }
    }

    @Override
    public boolean unbindDevice(String platformId, String ipcID) {
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        if (StringUtil.strIsRight(platformId) && StringUtil.strIsRight(ipcID)) {
            String ipcIDTrim = ipcID.trim();
            try {
                Delete delete = new Delete(Bytes.toBytes(ipcIDTrim)); //根据设备ID（rowkey）删除一整行的绑定数据。
                table.delete(delete);
                LOG.info("Unbind device:" + ipcIDTrim + " and " + platformId + " successful");
                return true;
            } catch (Exception e) {
                LOG.error("Current unbind is failed!");
                return false;
            } finally {
                HBaseUtil.closTable(table);
            }
        } else {
            LOG.error("Please check the arguments!");
            return false;
        }
    }

    @Override
    public boolean renameNotes(String notes, String ipcID) {
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        if (StringUtil.strIsRight(ipcID)) {
            String ipcIDTrim = ipcID.trim();
            try {
                Put put = new Put(Bytes.toBytes(ipcIDTrim));
                put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.NOTES, Bytes.toBytes(notes));
                table.put(put);
                LOG.info("Rename " + ipcIDTrim + "'s notes successful!");
                return true;
            } catch (Exception e) {
                LOG.error("Current renameNotes is failed!");
                return false;
            }
        } else {
            LOG.error("Please check the arguments!");
            return false;
        }
    }
}
