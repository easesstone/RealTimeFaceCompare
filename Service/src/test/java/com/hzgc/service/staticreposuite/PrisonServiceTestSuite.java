package com.hzgc.service.staticreposuite;

import com.hzgc.dubbo.staticrepo.PrisonSearchOpts;
import com.hzgc.dubbo.staticrepo.PrisonService;
import com.hzgc.service.staticrepo.PrisonServiceImpl;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrisonServiceTestSuite {
    PrisonService service = new PrisonServiceImpl();
    @Test
    public void testUpdateLocation() throws SQLException {
        PrisonSearchOpts prisonSearchOpts = new PrisonSearchOpts();
        Map<String, List<String>> pkeysUpdate = new HashMap<>();
        List<String> ids = new ArrayList<>();
        ids.add("060f8bcfe78d4dd48398aa9e8891ce17");
        ids.add("15724dcc8e0241daaf5749b401108a19");
        pkeysUpdate.put("00011", ids);
        prisonSearchOpts.setPkeysUpate(pkeysUpdate);
        System.out.printf("final count: " + service.updateLocation(prisonSearchOpts));

    }

    @Test
    public void testResetLocation() {
        PrisonSearchOpts prisonSearchOpts = new PrisonSearchOpts();
        List<String> pkeysReset = new ArrayList<>();
        pkeysReset.add("0001004");
        prisonSearchOpts.setPkeysReset(pkeysReset);

        System.out.printf("result: " + service.resetLocation(prisonSearchOpts));
    }

    @Test
    public void testCountByLocation() {
        PrisonSearchOpts prisonSearchOpts = new PrisonSearchOpts();
        List<String> pkeysCount = new ArrayList<>();
//        pkeysCount.add("0001004");
//        pkeysCount.add("0001001");
//        pkeysCount.add("0001002");
//        pkeysCount.add("0001003");
        prisonSearchOpts.setPkeysCount(pkeysCount);
        System.out.printf("result: " + service.countByLocation(prisonSearchOpts));
    }
}
