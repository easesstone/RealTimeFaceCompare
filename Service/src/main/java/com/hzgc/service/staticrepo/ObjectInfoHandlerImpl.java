package com.hzgc.service.staticrepo;

import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.dubbo.staticrepo.*;
import com.hzgc.util.common.ObjectUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class ObjectInfoHandlerImpl implements ObjectInfoHandler {

    private static Logger LOG = Logger.getLogger(ObjectInfoHandlerImpl.class);

    public ObjectInfoHandlerImpl() {
    }

    @Override
    public byte addObjectInfo(String platformId, Map<String, Object> personObject) {
        LOG.info("personObject: " + personObject.entrySet().toString());
        java.sql.Connection conn;
        long start = System.currentTimeMillis();
        PersonObject person = PersonObject.mapToPersonObject(personObject);
        person.setPlatformid(platformId);
        LOG.info("the rowkey off this add person is: " + person.getId());

        String sql = "upsert into objectinfo(" + ObjectInfoTable.ROWKEY+ ", " + ObjectInfoTable.NAME  + ", "
                + ObjectInfoTable.PLATFORMID + ", " + ObjectInfoTable.TAG + ", " + ObjectInfoTable.PKEY + ", "
                + ObjectInfoTable.IDCARD + ", " + ObjectInfoTable.SEX + ", " + ObjectInfoTable.PHOTO + ", "
                + ObjectInfoTable.FEATURE + ", " + ObjectInfoTable.REASON + ", " + ObjectInfoTable.CREATOR + ", "
                + ObjectInfoTable.CPHONE + ", " + ObjectInfoTable.CREATETIME + ", " + ObjectInfoTable.UPDATETIME + ", "
                + ObjectInfoTable.IMPORTANT + ", "  + ObjectInfoTable.STATUS
                + ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstm = null;
        try {
            conn = PhoenixJDBCHelper.getConnection();
            pstm = new ObjectInfoHandlerTool().getStaticPrepareStatementV1(conn, person, sql);
            pstm.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, null);
        }
        LOG.info("添加一条数据到静态库花费时间： " + (System.currentTimeMillis() - start));
        //数据变动，更新objectinfo table 中的一条数据,表示静态库中的数据有变动
        new ObjectInfoHandlerTool().updateTotalNumOfHbase();
        return 0;
    }

    @Override
    public int deleteObjectInfo(List<String> rowkeys) {
        LOG.info("rowKeys: " + rowkeys);
        // 获取table 对象，通过封装HBaseHelper 来获取
        long start = System.currentTimeMillis();
        java.sql.Connection conn;
        String sql = "delete from objectinfo where " + ObjectInfoTable.ROWKEY  +" = ?";
        PreparedStatement pstm = null;
        try {
            conn = PhoenixJDBCHelper.getConnection();
            pstm = conn.prepareStatement(sql);
            for (int i = 0; i< rowkeys.size(); i++) {
                pstm.setString(1, rowkeys.get(i));
                pstm.executeUpdate();
                if (i % 10 == 0) {
                    conn.commit();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, null);
        }
        LOG.info("删除静态信息库的" + rowkeys.size() + "条数据花费时间： " + (System.currentTimeMillis() - start));
        //数据变动，更新objectinfo table 中的一条数据,表示静态库中的数据有变动
        new ObjectInfoHandlerTool().updateTotalNumOfHbase();
        return 0;
    }

    /**
     * @param personObject K-V 对，里面存放的是字段和值之间的一一对应关系，参考添加里的描述
     * @return 更新成功与否的标志，0成功，1失败
     */
    @Override
    public int updateObjectInfo(Map<String, Object> personObject) {
        LOG.info("personObject: " + personObject.entrySet().toString());
        long start = System.currentTimeMillis();
        String thePassId = (String) personObject.get(ObjectInfoTable.ROWKEY);
        java.sql.Connection conn;
        if (thePassId == null) {
            LOG.info("the pass Id can not be null....");
            return 1;
        }
        PreparedStatement pstm = null;
        try {
            conn = PhoenixJDBCHelper.getConnection();
            Map<String, List<Object>> sqlAndSetValues = ParseByOption.getUpdateSqlFromPersonMap(personObject);
            String sql = null;
            List<Object> setValues = new ArrayList<>();
            for (Map.Entry<String, List<Object>> entry : sqlAndSetValues.entrySet()) {
                sql = entry.getKey();
                setValues = entry.getValue();
            }
            pstm = conn.prepareStatement(sql);
            for (int i = 0; i < setValues.size(); i++) {
                pstm.setObject(i+1, setValues.get(i));
            }
            pstm.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, null);
        }
        LOG.info("更新rowkey为: " + thePassId +  "数据花费的时间是: " + (System.currentTimeMillis() - start));
        //数据变动，更新objectinfo table 中的一条数据,表示静态库中的数据有变动
        new ObjectInfoHandlerTool().updateTotalNumOfHbase();
        return 0;
    }

    @Override
    public ObjectSearchResult searchByRowkey(String id) {
        long start = System.currentTimeMillis();
        java.sql.Connection conn;
        PreparedStatement pstm = null;
        ObjectSearchResult result = new ObjectSearchResult();
        PersonObject person;
        ResultSet resultSet = null;
        try {
            String sql = "select * from " + ObjectInfoTable.TABLE_NAME + " where id = ?";
            conn = PhoenixJDBCHelper.getConnection();
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, id);
            resultSet = pstm.executeQuery();
            person = new ObjectInfoHandlerTool().getPersonObjectFromResultSet(resultSet);
            result.setSearchStatus(0);
            List<PersonSingleResult> personSingleResults = new ArrayList<>();
            PersonSingleResult personSingleResult = new PersonSingleResult();
            personSingleResult.setSearchNums(1);
            List<PersonObject> persons = new ArrayList<>();
            persons.add(person);
            personSingleResult.setPersons(persons);
            personSingleResults.add(personSingleResult);
            result.setFinalResults(personSingleResults);
        } catch (SQLException e) {
            result.setSearchStatus(1);
            e.printStackTrace();
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, resultSet);
        }
        LOG.info("获取一条数据的时间是：" + (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public ObjectSearchResult getObjectInfo(PSearchArgsModel pSearchArgsModel) {
        LOG.info("pSearchArgsModel: " + pSearchArgsModel);
        long start = System.currentTimeMillis();
        java.sql.Connection conn;
        // 总的结果
        ObjectSearchResult objectSearchResult = new ObjectSearchResult();
        String searchTotalId = UUID.randomUUID().toString().replace("-", "");
        objectSearchResult.setSearchTotalId(searchTotalId);
        List<PersonSingleResult> finalResults = new ArrayList<>();
        conn = PhoenixJDBCHelper.getConnection();
        if (conn == null) {
            ObjectSearchResult objectSearchResultError = new ObjectSearchResult();
            objectSearchResult.setSearchStatus(1);
            objectSearchResult.setSearchTotalId(UUID.randomUUID().toString().replace("-", ""));
            objectSearchResult.setFinalResults(finalResults);
            return objectSearchResultError;
        }

        //封装的sql 以及需要设置的值
        Map<String, List<Object>> finalSqlAndValues = ParseByOption.getSqlFromPSearchArgsModel(conn, pSearchArgsModel);

        PreparedStatement pstm = null;
        ResultSet resultSet = null;

        // 取出封装的sql 以及需要设置的值，进行sql 查询
        if (finalSqlAndValues == null) {
            objectSearchResult.setSearchStatus(1);
            LOG.info("创建sql 失败，请检查代码");
            return objectSearchResult;
        }
        for (Map.Entry<String, List<Object>> entry : finalSqlAndValues.entrySet()) {
            String sql = entry.getKey();
            List<Object> setValues = entry.getValue();
            try {
                // 实例化pstm 对象，并且设置值，
                pstm = conn.prepareStatement(sql);
                for (int i = 0; i< setValues.size(); i++) {
                    pstm.setObject(i + 1, setValues.get(i));
                }
                resultSet = pstm.executeQuery();
                Map<String, byte[]> photos = pSearchArgsModel.getImages();
                Map<String, FaceAttribute> faceAttributeMap = pSearchArgsModel.getFaceAttributeMap();
                // 有图片的情况下
                if (photos != null && photos.size() != 0
                        && faceAttributeMap != null && faceAttributeMap.size() != 0
                        && faceAttributeMap.size() == photos.size()) {
                    if (!pSearchArgsModel.isTheSameMan()) {  // 不是同一个人
                        // 分类的人
                        Map<String, List<PersonObject>> personObjectsMap = new HashMap<>();

                        List<PersonObject> personObjects = new ArrayList<>();
                        PersonObject personObject;
                        List<String> types = new ArrayList<>();
                        int lable = 0;
                        while (resultSet.next()) {
                            String type = resultSet.getString("type");
                            if (!types.contains(type)){
                                types.add(type);
                                if (types.size() > 1) {
                                    personObjectsMap.put(types.get(lable), personObjects);
                                    personObjects = new ArrayList<>();
                                    lable++;
                                }
                            }
                            personObject = new PersonObject();
                            personObject.setId(resultSet.getString(ObjectInfoTable.ROWKEY));
                            personObject.setPkey(resultSet.getString(ObjectInfoTable.PKEY));
                            personObject.setPlatformid(resultSet.getString(ObjectInfoTable.PLATFORMID));
                            personObject.setName(resultSet.getString(ObjectInfoTable.NAME));
                            personObject.setSex(resultSet.getInt(ObjectInfoTable.SEX));
                            personObject.setIdcard(resultSet.getString(ObjectInfoTable.IDCARD));
                            personObject.setCreator(resultSet.getString(ObjectInfoTable.CREATOR));
                            personObject.setCphone(resultSet.getString(ObjectInfoTable.CPHONE));
                            personObject.setUpdatetime(resultSet.getTimestamp(ObjectInfoTable.UPDATETIME));
                            personObject.setCreatetime(resultSet.getTimestamp(ObjectInfoTable.CREATETIME));
                            personObject.setReason(resultSet.getString(ObjectInfoTable.REASON));
                            personObject.setTag(resultSet.getString(ObjectInfoTable.TAG));
                            personObject.setImportant(resultSet.getInt(ObjectInfoTable.IMPORTANT));
                            personObject.setStatus(resultSet.getInt(ObjectInfoTable.STATUS));
                            personObject.setSim(resultSet.getFloat(ObjectInfoTable.RELATED));
                            personObject.setLocation(resultSet.getString(ObjectInfoTable.LOCATION));
                            personObjects.add(personObject);
                        }
                        // 封装最后需要返回的结果
                        for (Map.Entry<String, List<PersonObject>> entryVV : personObjectsMap.entrySet()) {
                            String key = entryVV.getKey();
                            List<PersonObject> persons = entryVV.getValue();
                            PersonSingleResult personSingleResult = new PersonSingleResult();
                            personSingleResult.setSearchRowkey(searchTotalId + key);
                            personSingleResult.setSearchNums(persons.size());
                            personSingleResult.setPersons(persons);
                            List<byte[]> photosTmp = new ArrayList<>();
                            photosTmp.add(photos.get(key));
                            personSingleResult.setSearchPhotos(photosTmp);
                            if (personSingleResult.getPersons() != null || personSingleResult.getPersons().size() != 0) {
                                finalResults.add(personSingleResult);
                            }
                        }
                    } else {  // 是同一个人
                        PersonSingleResult personSingleResult = new PersonSingleResult();
                        personSingleResult.setSearchRowkey(searchTotalId);

                        List<byte[]> searchPhotos = new ArrayList<>();
                        for (Map.Entry<String, byte[]> entryV1 : photos.entrySet()) {
                            searchPhotos.add(entryV1.getValue());
                        }
                        personSingleResult.setSearchPhotos(searchPhotos);
                        // 封装personSingleResult
                        new ObjectInfoHandlerTool().getPersonSingleResult(personSingleResult, resultSet, true);
                        if (personSingleResult.getPersons() != null || personSingleResult.getPersons().size() != 0) {
                            finalResults.add(personSingleResult);
                        }
                    }
                } else { // 没有图片的情况下
                   PersonSingleResult personSingleResult = new PersonSingleResult();   // 需要进行修改
                   personSingleResult.setSearchRowkey(searchTotalId);
                    //封装personSingleResult
                   new ObjectInfoHandlerTool().getPersonSingleResult(personSingleResult, resultSet, false);
                    if (personSingleResult.getPersons() != null || personSingleResult.getPersons().size() != 0) {
                        finalResults.add(personSingleResult);
                    }
                }
            } catch (SQLException e) {
                objectSearchResult.setSearchStatus(1);
                e.printStackTrace();
                return objectSearchResult;
            }
        }

        objectSearchResult.setFinalResults(finalResults);
        objectSearchResult.setSearchStatus(0);

        LOG.info("总的搜索时间是: " + (System.currentTimeMillis() - start));
        new ObjectInfoHandlerTool().saveSearchRecord(conn, objectSearchResult);
        Integer pageSize = pSearchArgsModel.getPageSize();
        Integer startCount = pSearchArgsModel.getStart();
        if (startCount != null && pageSize != null) {
            new ObjectInfoHandlerTool().formatTheObjectSearchResult(objectSearchResult, startCount, pageSize);
        }
        PhoenixJDBCHelper.closeConnection(null, pstm, resultSet);
        LOG.info("***********************");
        LOG.info(objectSearchResult);
        LOG.info("***********************");
        return objectSearchResult;
    }


    @Override
    public byte[] getPhotoByKey(String rowkey) {
        String sql = "select photo from " + ObjectInfoTable.TABLE_NAME + " where id = ?";
        java.sql.Connection conn;
        PreparedStatement pstm = null;
        ResultSet resultSet = null;
        byte[] photo;
        try {
            conn = PhoenixJDBCHelper.getConnection();
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, rowkey);
            resultSet = pstm.executeQuery();
            resultSet.next();
            photo = resultSet.getBytes(ObjectInfoTable.PHOTO);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, resultSet);
        }
        return photo;
    }

    private static ObjectSearchResult getObjectSearchResultError(String errorMsg) {
        ObjectSearchResult objectSearchResultError = new ObjectSearchResult();
        objectSearchResultError.setSearchStatus(1);
        LOG.info(errorMsg);
        return objectSearchResultError;
    }

    /**
     * 根据传进来的参数，进行及实际路查询
     * @param  searchRecordOpts 历史查询参数
     * @return ObjectSearchResult 返回结果，封装好的历史数据。
     */
    @Override
    public ObjectSearchResult getRocordOfObjectInfo(SearchRecordOpts searchRecordOpts) {
        LOG.info("searchRecordOpts: " + searchRecordOpts);
        // 传过来的参数中为空，或者子查询为空，或者子查询大小为0，都返回查询错误。
        if (searchRecordOpts == null) {
           return getObjectSearchResultError("SearchRecordOpts 为空，请确认参数是否正确.");
        }
        // 总的searchId
        List<SubQueryOpts> subQueryOptsList = searchRecordOpts.getSubQueryOptsList();
        if (subQueryOptsList == null || subQueryOptsList.size() == 0) {
            return getObjectSearchResultError("子查询列表为空，请确认参数是否正确.");
        }

        SubQueryOpts subQueryOpts = subQueryOptsList.get(0);
        if (subQueryOpts == null) {
            return getObjectSearchResultError("子查询对象SubQueryOpts 对象为空，请确认参数是否正确.");
        }

        // 子查询Id
        String subQueryId = subQueryOpts.getQueryId();
        if (subQueryId == null) {
            LOG.info("子查询Id 为空");
            return getObjectSearchResultError("子查询Id 为空，请确认参数是否正确.");
        }

        // 需要分组的pkeys
        List<String> pkeys = subQueryOptsList.get(0).getPkeys();
        // 排序参数
        List<StaticSortParam> staticSortParams = searchRecordOpts.getStaticSortParams();

        // sql 查询语句
        String sql = "select " + SearchRecordTable.ID + ", " + SearchRecordTable.RESULT + " from "
                + SearchRecordTable.TABLE_NAME + " where " + SearchRecordTable.ID + " = ?";

        ObjectSearchResult finnalObjectSearchResult = new ObjectSearchResult();
        List<PersonSingleResult> personSingleResults = new ArrayList<>();

        java.sql.Connection conn;
        PreparedStatement pstm = null;
        ResultSet resultSet = null;
        try {
            conn = PhoenixJDBCHelper.getConnection();
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, subQueryId);
            resultSet = pstm.executeQuery();
            resultSet.next();
            PersonSingleResult personSingleResult = (PersonSingleResult) ObjectUtil
                    .byteToObject(resultSet.getBytes(SearchRecordTable.RESULT));
            if (personSingleResult != null) {
                List<PersonObject> personObjects = personSingleResult.getPersons();
                List<GroupByPkey> groupByPkeys = new ArrayList<>();
                GroupByPkey groupByPkey;
                if (personObjects != null && staticSortParams != null && staticSortParams.contains(StaticSortParam.PEKEY)) {
                    Map<String, List<PersonObject>> groupingByPkeys = personObjects.stream()
                            .collect(Collectors.groupingBy(PersonObject::getPkey));
                    for (Map.Entry<String, List<PersonObject>> entry : groupingByPkeys.entrySet()) {
                        groupByPkey = new GroupByPkey();
                        String pkey = entry.getKey();
                        groupByPkey.setPkey(pkey);
                        List<PersonObject> personObjectList = entry.getValue();
                        // 对结果进行排序
                        new ObjectInfoHandlerTool().sortPersonObject(personObjectList, staticSortParams);

                        // 如果指定了需要返回的Pkey
                        if (pkeys != null && pkeys.size() > 0 && pkeys.contains(pkey)) {
                            groupByPkey.setPersons(personObjectList);
                            groupByPkeys.add(groupByPkey);
                            continue;
                        }
                        if (pkeys == null || pkeys.size() == 0) {
                            groupByPkey.setPersons(personObjectList);
                            groupByPkeys.add(groupByPkey);
                        }
                    }
                    personSingleResult.setGroupByPkeys(groupByPkeys);
                    personSingleResult.setPersons(null);
                } else if (personObjects != null && staticSortParams != null && !staticSortParams.contains(StaticSortParam.PEKEY)){
                    personSingleResult.setGroupByPkeys(null);
                    new ObjectInfoHandlerTool().sortPersonObject(personObjects, staticSortParams);
                    personSingleResult.setPersons(personObjects);
                }
            }
            personSingleResults.add(personSingleResult);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, resultSet);
        }
        finnalObjectSearchResult.setSearchStatus(0);
        finnalObjectSearchResult.setFinalResults(personSingleResults);
        int pageSize = searchRecordOpts.getSize();
        int start = searchRecordOpts.getStart();
        new ObjectInfoHandlerTool().formatTheObjectSearchResult(finnalObjectSearchResult, start, pageSize);
        LOG.info("***********************");
        LOG.info(finnalObjectSearchResult);
        LOG.info("***********************");
        return finnalObjectSearchResult;
    }

    @Override
    public byte[] getSearchPhoto(String rowkey) {
        return  null;
    }
}
