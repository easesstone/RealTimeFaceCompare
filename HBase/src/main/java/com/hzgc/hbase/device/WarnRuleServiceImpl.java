package com.hzgc.hbase.device;

import com.hzgc.dubbo.device.WarnRule;
import com.hzgc.dubbo.device.WarnRuleService;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.util.ObjectUtil;
import com.hzgc.util.StringUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class WarnRuleServiceImpl implements WarnRuleService {
    private static Logger LOG = Logger.getLogger(WarnRuleServiceImpl.class);

    /**
     * 配置布控规则（外）（赵喆）
     * 设置多个设备的对比规则，如果之前存在对比规则，先清除之前的规则，再重新写入
     */
    @Override
    public Map<String, Boolean> configRules(List<String> ipcIDs, List<WarnRule> rules) {
        //初始化
        Table deviceTable = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);//从Hbase读device表
        Map<Integer, Map<String, Integer>> commonRule = new HashMap<>();//初始化设备布控预案对象
        Map<String, Map<String, Integer>> offlineMap = new HashMap<>();  //初始化离线告警对象
        List<Put> putList = new ArrayList<>();
        Map<String, Boolean> reply = new HashMap<>();//reply：是否添加成功

        //把传进来的rules：List<WarnRule> rules转化为commonRule：Map<Integer, Map<String, Integer>>格式
        parseDeviceRule(rules, ipcIDs, commonRule);
        byte[] commonRuleBytes = ObjectUtil.objectToByte(commonRule);
        for (String ipcID : ipcIDs) {
            //以传入的设备ID为行键，device为列族，告警类型w为列，commonRuleBytes为值，的Put对象添加到putList列表
            Put put = new Put(Bytes.toBytes(ipcID));
            put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, commonRuleBytes);
            putList.add(put);
            //对于每一条规则
            for (WarnRule rule : rules) {
                parseOfflineWarn(rule, ipcID, offlineMap);//解析离线告警：在离线告警offlineMap中添加相应的对象类型、ipcID和规则中的离线天数阈值DayThreshold
            }
            reply.put(ipcID, true);
        }
        try {
            deviceTable.put(putList);//把putList列表添加到表device表中
            configOfflineWarn(offlineMap, deviceTable);//config模式下，把离线告警offlineMap对象插入到device表中
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            HBaseUtil.closTable(deviceTable);
        }
        return reply;
    }

    /**
     *添加布控规则（外）（赵喆）
     */
    @Override
    public Map<String, Boolean> addRules(List<String> ipcIDs, List<WarnRule> rules) {
        Table deviceTable = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        Map<Integer, Map<String, Integer>> commonRule = new HashMap<>();
        Map<String, Map<String, Integer>> offlineMap = new HashMap<>();
        List<Put> putList = new ArrayList<>();
        Map<String, Boolean> reply = new HashMap<>();

        //把传进来的rules：List<WarnRule> rules转化为commonRule：Map<Integer, Map<String, Integer>>格式
        parseDeviceRule(rules, ipcIDs, commonRule);
        byte[] commonRuleBytes = ObjectUtil.objectToByte(commonRule);
        for (String ipcID : ipcIDs) {
            Put put = new Put(Bytes.toBytes(ipcID));
            put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, commonRuleBytes);
            putList.add(put);
            for (WarnRule rule : rules) {
                parseOfflineWarn(rule, ipcID, offlineMap);//解析离线告警：在离线告警offlineMap中添加相应的对象类型、ipcID和规则中的离线天数阈值DayThreshold
            }
            reply.put(ipcID, true);
        }
        try { //与config模式不同
            for (String ipcID : ipcIDs) {
                Get get = new Get(Bytes.toBytes(ipcID));
                Result result = deviceTable.get(get);
                if (!result.isEmpty()) { //device表中的值不为空
                    Map<Integer, Map<String, Integer>> tempMap =
                            //告警类型      对象类型,阈值
                            deSerializDevice(result.getValue(DeviceTable.CF_DEVICE, DeviceTable.WARN));
                    for (Integer code : commonRule.keySet()) { //对于每一种告警类型：
                        if (tempMap.containsKey(code)) { //若device表中存在这种告警类型
                            for (String type : commonRule.get(code).keySet()) { //对于布控规则commonRule中每一个告警类型的对象类型
                                tempMap.get(code).put(type, commonRule.get(code).get(type));//把commonRule中的每一个对象类型添加到device表
                            }
                        } else {//若device表中不存在这种告警类型，直接添加
                            tempMap.put(code, commonRule.get(code));
                        }
                    }
                    Put put = new Put(Bytes.toBytes(ipcID));
                    put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, ObjectUtil.objectToByte(tempMap));
                    putList.add(put);
                } else { //device表中的值为空，直接添加
                    Put put = new Put(Bytes.toBytes(ipcID));
                    put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, commonRuleBytes);
                    putList.add(put);
                }
            }
            deviceTable.put(putList);
            configOfflineWarn(offlineMap, deviceTable);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            HBaseUtil.closTable(deviceTable);
        }
        return reply;
    }

    /**
     * 获取设备的对比规则 （外）（赵喆）
     */
    @Override
    public List<WarnRule> getCompareRules(String ipcID) {
        List<WarnRule> reply = new ArrayList<>();
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        if (StringUtil.strIsRight(ipcID)) {
            try {
                Get get = new Get(Bytes.toBytes(ipcID));
                Result result = table.get(get);
                //若device表中存在告警w列
                if (result.containsColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN)) {
                    byte[] deviceByte = result.getValue(DeviceTable.CF_DEVICE, DeviceTable.WARN);//获取告警列中的值
                    Map<Integer, Map<String, Integer>> deviceMap = deSerializDevice(deviceByte);//并转化为Map嵌套格式（反序列化）
                    // 设备布控预案数据类型Map<Integer, Map<String, Integer>>    （即deviceMap）
                    //                                     告警类型,      对象类型,阈值
                    for (Integer code : deviceMap.keySet()) { //对于deviceMap中的每个告警类型
                        //获取tempMap：<String, Integer>
                        //                     对象类型，阈值
                        Map<String, Integer>  tempMap = deviceMap.get(code);
                        for (String type : deviceMap.get(code).keySet()) { //对于tempMap中的每个对象类型
                            WarnRule warnRule = new WarnRule();
                            warnRule.setCode(code);
                            warnRule.setObjectType(type);//把规则中的告警类型code和对象类型type放到warnRule中，用于返回
                            if (code == DeviceTable.OFFLINE) { //对于告警类型中是离线告警的
                                warnRule.setDayThreshold(tempMap.get(type));//获取离线告警天数阈值
                            } else {
                                warnRule.setThreshold(tempMap.get(type));//对于告警类型中不是离线告警的，获取sim阈值
                            }
                            reply.add(warnRule);
                        }
                    }
                    return reply;
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            } finally {
                HBaseUtil.closTable(table);
            }
        }
        return reply;
    }

    /**
     * 删除设备的布控规则（外）（赵喆）
     */
    @Override
    public Map<String, Boolean> deleteRules(List<String> ipcIDs) {
        Table deviceTable = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);//获取device表
        Map<String, Boolean> reply = new HashMap<>();
        List<Delete> deviceDelList = new ArrayList<>();
        List<Put> objPutList = new ArrayList<>();
        String id = "";
        if (ipcIDs != null) { //若传入的设备ID不为空
            try {
                for (String ipc : ipcIDs) { //对于每一个设备ID，删除其在device表中对应的列族、列
                    id = ipc;
                    Delete deviceDelete = new Delete(Bytes.toBytes(ipc));
                    deviceDelete.addColumns(DeviceTable.CF_DEVICE, DeviceTable.WARN);//列族：device，列：w
                    deviceDelList.add(deviceDelete);
                    reply.put(ipc, true); //reply：（设备ID，删除成功）
                    LOG.info("release the rule binding, the device ID is:" + ipc);
                }
                Get offlineGet = new Get(DeviceTable.OFFLINERK);//获取离线告警数据的行键
                Result offlineResult = deviceTable.get(offlineGet);
                if (!offlineResult.isEmpty()) { //若离线告警数据非空
                    //将离线告警数据反序列化
                    Map<String, Map<String, Integer>> offlineMap =
                            deSerializOffLine(offlineResult.getValue(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL));
                    //offlineMap：Map<String, Map<String, Integer>>
                    //                      对象类型      设备ID,离线天数
                    for (String type : offlineMap.keySet()) { //对于离线告警数据中的每个对象类型
                        for (String ipc : ipcIDs) { //对于每个设备ID
                            offlineMap.get(type).remove(ipc); //删除设备ID对应的键值内容
                        }
                    }
                    //把删除后的离线告警数据存入device表中
                    Put offlinePut = new Put(DeviceTable.OFFLINERK);
                    offlinePut.addColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL, ObjectUtil.objectToByte(offlineMap));
                    deviceTable.put(offlinePut);
                }

                deviceTable.delete(deviceDelList);

                return reply;
            } catch (IOException e) {
                reply.put(id,false);
                LOG.error(e.getMessage());
            } finally {
                HBaseUtil.closTable(deviceTable);
            }
        }
        return reply;
    }

    /**
     * 查看有多少设备绑定了此人员类型objectType （外）（赵喆）
     */
    @Override
    public List<String> objectTypeHasRule(String objectType) {
        return null;
    }


    /**
     * objectTypeHasRule(String objectType)的子方法 （外）（赵喆）
     */
    public int deleteObjectTypeOfRules(String objectType, List<String> ipcIDs) {
        return 0;
    }


    /**
     * config模式下配置离线告警（内部方法）（config模式下，把离线告警offlineMap对象插入到device表中）
     * 离线告警数据类型offlineMap：Map<String, Map<String, Integer>>
     *                                                对象类型,   设备ID, 离线天数
     * tempMap：device表中的值
     */
    private void configOfflineWarn(Map<String, Map<String, Integer>> offlineMap, Table deviceTable) {
        try {
            Get offlinGet = new Get(DeviceTable.OFFLINERK);
            Result offlineResult = deviceTable.get(offlinGet); //获取device表中offlineWarnRowKey行键对应的数据
            if (!offlineResult.isEmpty()) { //若device表中offlineWarnRowKey行键对应的数据offlineResult非空（value中有值）
                Map<String, Map<String, Integer>> tempMap = deSerializOffLine(offlineResult.
                        getValue(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL)); //反序列化该值类型（转化为Object）
                for (String type : offlineMap.keySet()) {//对于离线告警offlineMap中的每个对象类型
                    if (tempMap.containsKey(type)) { //假如Hbase数据库中的device表中包含离线告警offlineMap的对象类型
                        for (String ipc : offlineMap.get(type).keySet()) {//对于离线告警offlineMap中的每一个设备ID
                            //覆盖device表中原有的值
                            tempMap.get(type).put(ipc, offlineMap.get(type).get(ipc));//offlineMap.get(type).get(ipc)：离线天数
                        }
                    } else {//假如Hbase数据库中的device表中不包含离线告警offlineMap的对象类型，直接向device表中添加离线告警offlineMap中的值
                        tempMap.put(type, offlineMap.get(type));
                    }
                }
                Put offlinePut = new Put(DeviceTable.OFFLINERK);
                offlinePut.addColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL, ObjectUtil.objectToByte(tempMap));
                deviceTable.put(offlinePut);
            } else {
                //若hbase的device表中offlineWarnRowKey行键对应的数据为空，直接把offlineMap的值加入到device表
                Put offlinePut = new Put(DeviceTable.OFFLINERK);
                offlinePut.addColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL, ObjectUtil.objectToByte(offlineMap));
                deviceTable.put(offlinePut);
            }

        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }


    /**
     * 解析configRules()传入的布控规则，并在解析的同时同步其他相关数据（内部方法）
     */
    //把传进来的rules：List<WarnRule> rules转化为commonRule：Map<Integer, Map<String, Integer>>格式
    private void parseDeviceRule(List<WarnRule> rules,
                                 List<String> ipcIDs,
                                 Map<Integer, Map<String, Integer>> commonRule) {
        if (rules != null && commonRule != null && ipcIDs != null) {//规则不为空，设备ID不为空
            for (WarnRule rule : rules) {
                //code：告警类型。0：识别告警；1：新增告警；2：离线告警
                //rules：传入的规则，List<WarnRule>格式；commonRule：设备布控预案，需转化成的Map<Integer, Map<String, Integer>>格式
                //IDENTIFY：识别告警；ADDED：新增告警。若传入的规则rules中的告警类型为识别告警或新增告警
                if (Objects.equals(rule.getCode(), DeviceTable.IDENTIFY) || Objects.equals(rule.getCode(), DeviceTable.ADDED)) {
                    //判断commonRule的键（告警类型）是否是传入的规则rule中的告警类型。如果之前存在对比规则，先清除之前的规则，再重新写入
                    if (commonRule.containsKey(rule.getCode())) {
                        commonRule.get(rule.getCode()).put(rule.getObjectType(), rule.getThreshold());//直接覆盖之前的规则
                    } else {
                        //如果之前不存在对比规则，直接写入
                        Map<String, Integer> temMap = new HashMap<>();
                        temMap.put(rule.getObjectType(), rule.getThreshold()); //此处不同：getThreshold()
                        commonRule.put(rule.getCode(), temMap);
                    }
                }
                //OFFLINE：离线告警。若传入的规则rules中的告警类型为离线告警
                if (Objects.equals(rule.getCode(), DeviceTable.OFFLINE)) {
                    //如果之前存在对比规则，先清除之前的规则，再重新写入
                    if (commonRule.containsKey(rule.getCode())) {
                        commonRule.get(rule.getCode()).put(rule.getObjectType(), rule.getDayThreshold());
                    } else {
                        //如果之前不存在对比规则，直接写入
                        Map<String, Integer> tempMap = new HashMap<>();
                        tempMap.put(rule.getObjectType(), rule.getDayThreshold());//此处不同：getDayThreshold()
                        commonRule.put(rule.getCode(), tempMap);
                    }
                }
            }
        }
    }

    /**
     * 向参数objectType对应的数据类型中添加成员（内部方法）
     */
    private static void addMembers(Map<String, Map<String, Map<Integer, String>>> objType, WarnRule rule, String ipcID) {
        //假如objType中的键（对象类型）所指向的值为空
        if (objType.get(rule.getObjectType()) == null) {
            Map<String, Map<Integer, String>> ipcMap = new HashMap<>();
            Map<Integer, String> warnMap = new HashMap<>();
            //一层套一层，依次在objType中存入传入规则rule中的：告警类型（code），设备ID（ipcID）到相对应的对象类型（ObjectType）
            warnMap.put(rule.getCode(), "");
            ipcMap.put(ipcID, warnMap);
            objType.put(rule.getObjectType(), ipcMap);
        } else {
            //假如objType中的键（对象类型）所指向的值非空，将objType的键所指向的值赋给ipcMap对象
            Map<String, Map<Integer, String>> ipcMap = objType.get(rule.getObjectType());
            //若ipcMap中有ipcID的键，则在ipcMap已有的ipcID键中传入规则rule中的：告警类型（code）
            if (ipcMap.containsKey(ipcID)) {
                ipcMap.get(ipcID).put(rule.getCode(), "");
            } else {
                //若ipcMap中没有ipcID的键，则依次传入规则rule中的：告警类型（code）、设备ID（ipcID）
                Map<Integer, String> warnMap = new HashMap<>();
                warnMap.put(rule.getCode(), "");
                ipcMap.put(ipcID, warnMap);
            }
        }
    }

    /**
     * 解析离线告警（内部方法）
     * 离线告警数据类型Map<String, Map<String, Integer>>
     *                               对象类型,   设备ID, 离线天数
     */
    private void parseOfflineWarn(WarnRule rule,
                                  String ipcID,
                                  Map<String, Map<String, Integer>> offlineMap) {
        //离线告警中存在传入规则中的对象类型
        if (offlineMap.containsKey(rule.getObjectType())) {
            //在离线告警相应的对象类型中添加ipcID和规则中的离线天数阈值DayThreshold
            offlineMap.get(rule.getObjectType()).put(ipcID, rule.getDayThreshold());
        } else {
            //离线告警中不存在传入规则中的对象类型
            Map<String, Integer> ipcMap = new HashMap<>();
            ipcMap.put(ipcID, rule.getDayThreshold());////////////////// rule.getCode()改成了rule.getDayThreshold()
            offlineMap.put(rule.getObjectType(), ipcMap);
        }
    }

    /**
     * objectType数据类型：Map<Integer, String>（内部方法）
     * 反序列化此数据类型
     */
    private Map<String, Map<Integer, String>> deSerializObjToDevice(byte[] bytes) {
        if (bytes != null) {
            return (Map<String, Map<Integer, String>>) ObjectUtil.byteToObject(bytes);
        }
        return null;
    }

    /**
     * 设备布控预案数据类型：Map<Integer, Map<String, Integer>>（内部方法）
     * 反序列化此数据类型
     */
    private Map<Integer, Map<String, Integer>> deSerializDevice(byte[] bytes) {
        if (bytes != null) {
            return (Map<Integer, Map<String, Integer>>) ObjectUtil.byteToObject(bytes);
        }
        return null;
    }

    /**
     * 离线告警数据类型：Map<String, Map<String, Integer>>（内部方法）
     * 反序列化此数据类型
     */
    private Map<String, Map<String, Integer>> deSerializOffLine(byte[] bytes) {
        if (bytes != null) {
            return (Map<String, Map<String, Integer>>) ObjectUtil.byteToObject(bytes);
        }
        return null;
    }
}
