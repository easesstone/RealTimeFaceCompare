package com.hzgc.service.staticrepo;

import com.alibaba.druid.pool.DruidDataSource;
import com.hzgc.util.common.FileUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.log4j.Logger;
import org.datanucleus.store.rdbms.identifier.IdentifierFactory;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class PhoenixJDBCHelper {
    private Logger LOG = Logger.getLogger(PhoenixJDBCHelper.class);

    private static volatile PhoenixJDBCHelper instance;

    // druid 数据库连接池
    private volatile static DruidDataSource druidDataSource;


    private PhoenixJDBCHelper() {
        if (druidDataSource == null) {
            initDruidDataSource();
        }
    }

    public DruidDataSource getDruidDataSource() {
        return PhoenixJDBCHelper.druidDataSource;
    }

    public static PhoenixJDBCHelper getInstance() {
        if (instance == null) {
            synchronized (PhoenixJDBCHelper.class) {
                instance = new PhoenixJDBCHelper();
            }
        }
        return instance;
    }

    private static void initDruidDataSource() {
        File file = FileUtil.loadResourceFile("jdbc.properties");
        Properties jdbcProp = new Properties();
        try {
            jdbcProp.load(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // phoenix url
        String phoenixJDBCURL = jdbcProp.getProperty("phoenix.jdbc.url");
        // phoenix driver 名字
        String phoenixJDBCDriver = jdbcProp.getProperty("phoenix.jdbc.driver.name");
        // 当连接池启动时，初始化连接的个数，必须在minPoolSize~maxPoolSize之间，默认为3
        String phoenixJDBCInitialPoolSize = jdbcProp.getProperty("phoenix.jdbc.initialPoolSize");
        // 任何时间连接池中保存的最小连接数，默认3
        String phoenixJDBCMinPoolSize = jdbcProp.getProperty("phoenix.jdbc.minPoolSize");
        // 在任何时间连接池中所能拥有的最大连接数，默认15
        String phoenixJDBCMaxPoolSize = jdbcProp.getProperty("phoenix.jdbc.maxPoolSize");
        // 超过多长时间连接自动销毁，默认为0，即永远不会自动销毁
        String phoenixMaxIdleTime = jdbcProp.getProperty("phoenix.jdbc.maxIdleTime");
        // 获取连接等待超时的时间
        String phoenixMaxWait = jdbcProp.getProperty("phoenix.jdbc.maxWait");
        druidDataSource = new DruidDataSource();
        if (phoenixJDBCURL == null || phoenixJDBCDriver == null) {
            return;
        }
        druidDataSource.setUrl(phoenixJDBCURL);
        druidDataSource.setUrl(phoenixJDBCDriver);
        if (phoenixJDBCMinPoolSize != null) {
            druidDataSource.setMinIdle(Integer.parseInt(phoenixJDBCMinPoolSize));
        }
        if (phoenixJDBCInitialPoolSize != null) {
            druidDataSource.setInitialSize(Integer.parseInt(phoenixJDBCInitialPoolSize));
        }
        if (phoenixJDBCMaxPoolSize != null) {
            druidDataSource.setMaxActive(Integer.parseInt(phoenixJDBCMaxPoolSize));
        }
        if (phoenixMaxWait != null) {
            druidDataSource.setMaxWait(Long.parseLong(phoenixMaxWait));
        }
        if (phoenixMaxIdleTime != null) {
            druidDataSource.setTimeBetweenConnectErrorMillis(Long.parseLong(phoenixMaxIdleTime));
        }

    }


    public static void closeConnection(Connection conn, Statement pstm) {
       closeConnection(conn, pstm, null);
    }

    public static void closeConnection(Connection conn, Statement pstm, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstm != null) {
                    pstm.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
