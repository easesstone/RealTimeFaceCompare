package com.hzgc.service.staticrepo;

import com.hzgc.dubbo.staticrepo.*;
import com.hzgc.util.common.ObjectUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class SearchRecordHandlerImpl implements SearchRecordHandler {
    @Override
    public ObjectSearchResult getRocordOfObjectInfo(SearchRecordOpts searchRecordOpts) {
        List<SubQueryOpts> subQueryOpts = searchRecordOpts.getSubQueryOptsList();
        // sql
        String sql = "select " + SearchRecordTable.ID + ", " + SearchRecordTable.RESULT + " from "
                + SearchRecordTable.TABLE_NAME + " where " + SearchRecordTable.ID + " = ?";
        Connection conn = PhoenixJDBCHelper.getPhoenixJdbcConn();
        ObjectSearchResult finnalObjectSearchResult = new ObjectSearchResult();
        PreparedStatement pstm = null;
        if (subQueryOpts != null && subQueryOpts.size() == 0) {
            try {
                pstm = conn.prepareStatement(sql);
                pstm.setString(1, searchRecordOpts.getTotalSearchId());
                ResultSet resultSet = pstm.executeQuery();
                while (resultSet.next()) {
                    ObjectSearchResult originObjectSearchResult = (ObjectSearchResult) ObjectUtil.byteToObject(resultSet
                            .getBytes(SearchRecordTable.RESULT));
                    if (originObjectSearchResult != null) {
                        finnalObjectSearchResult.setSearchStatus(originObjectSearchResult.getSearchStatus());
                        finnalObjectSearchResult.setSearchTotalId(originObjectSearchResult.getSearchTotalId());

                        PersonSingleResult personSingleResult = new PersonSingleResult();
                        if (originObjectSearchResult.getFinalResults() != null) {
                            personSingleResult.setSearchNums(0);
                            PersonSingleResult personSingleResultOrigin;
                            if (originObjectSearchResult.getFinalResults().get(0) != null) {
                                personSingleResultOrigin = originObjectSearchResult.getFinalResults().get(0);
                                personSingleResult.setSearchRowkey(personSingleResultOrigin.getSearchRowkey());
                                personSingleResult.setSearchPhotos(personSingleResultOrigin.getSearchPhotos());
                                List<PersonObject>  personObjects = personSingleResultOrigin.getPersons();
                                Map<String, List<PersonObject>> orderByPkeys = personObjects.stream()
                                        .collect(groupingBy(PersonObject::getPkey));
                                List<GroupByPkey> groupByPkeys = new ArrayList<>();
                                GroupByPkey groupByPkey;
                                for(Map.Entry<String, List<PersonObject>> entry : orderByPkeys.entrySet()) {
                                    groupByPkey = new GroupByPkey();
                                    groupByPkey.setPkey(entry.getKey());
                                    List<PersonObject> personObjectList = entry.getValue();
                                    groupByPkey.setPersons(personObjectList);
                                    if (personObjectList != null) {
                                        groupByPkey.setTotal(personObjectList.size());
                                    }
                                    groupByPkeys.add(groupByPkey);
                                }
                                personSingleResult.setPersons(null);
                                personSingleResult.setGroupByPkeys(groupByPkeys);
                                List<PersonSingleResult> personSingleResults = new ArrayList<>();
                                personSingleResults.add(personSingleResult);
                                finnalObjectSearchResult.setFinalResults(personSingleResults);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return  null;
            } finally {
                PhoenixJDBCHelper.closeConnection(conn, pstm);
            }
        } else if (subQueryOpts != null){
            try {
                pstm = conn.prepareStatement(sql);
                pstm.setString(1, searchRecordOpts.getSubQueryOptsList().get(0).getQueryId());
                ResultSet resultSet = pstm.executeQuery();
                while (resultSet.next()) {
                    ObjectSearchResult originObjectSearchResult = (ObjectSearchResult) ObjectUtil.byteToObject(resultSet
                            .getBytes(SearchRecordTable.RESULT));
                    if (originObjectSearchResult != null) {
                        List<PersonSingleResult> personSingleResults = originObjectSearchResult.getFinalResults();
                        String queryId = subQueryOpts.get(0).getQueryId();
                        finnalObjectSearchResult.setSearchStatus(originObjectSearchResult.getSearchStatus());
                        finnalObjectSearchResult.setSearchTotalId(originObjectSearchResult.getSearchTotalId());
                        PersonSingleResult personSingleResultOrigin = null;
                        for (PersonSingleResult tmp : personSingleResults) {
                            if (queryId != null && queryId.equals(tmp.getSearchRowkey())) {
                                personSingleResultOrigin = tmp;
                                break;
                            }
                        }

                        PersonSingleResult personSingleResult = new PersonSingleResult();
                        personSingleResult.setSearchNums(0);
                        if (personSingleResultOrigin != null) {
                            personSingleResult.setSearchRowkey(personSingleResultOrigin.getSearchRowkey());
                            personSingleResult.setSearchPhotos(personSingleResultOrigin.getSearchPhotos());
                            List<PersonObject>  personObjects = personSingleResultOrigin.getPersons();
                            Map<String, List<PersonObject>> orderByPkeys = personObjects.stream()
                                    .collect(groupingBy(PersonObject::getPkey));
                            List<GroupByPkey> groupByPkeys = new ArrayList<>();
                            GroupByPkey groupByPkey;
                            for(Map.Entry<String, List<PersonObject>> entry : orderByPkeys.entrySet()) {
                                groupByPkey = new GroupByPkey();
                                groupByPkey.setPkey(entry.getKey());
                                List<PersonObject> personObjectList = entry.getValue();
                                groupByPkey.setPersons(personObjectList);
                                if (personObjectList != null) {
                                    groupByPkey.setTotal(personObjectList.size());
                                }
                                groupByPkeys.add(groupByPkey);
                            }
                            personSingleResult.setPersons(null);
                            personSingleResult.setGroupByPkeys(groupByPkeys);
                            List<PersonSingleResult> personSingleResultsFinale = new ArrayList<>();
                            personSingleResultsFinale.add(personSingleResult);
                            finnalObjectSearchResult.setFinalResults(personSingleResultsFinale);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return  null;
            } finally {
                PhoenixJDBCHelper.closeConnection(conn, pstm);
            }
        }
        return finnalObjectSearchResult;
    }

    @Override
    public byte[] getSearchPhoto(String rowkey) {
        return  null;
    }
}
