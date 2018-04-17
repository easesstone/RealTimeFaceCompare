package com.hzgc.service.staticrepo;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class PhoenixJDBCHelper {
    private Logger LOG = Logger.getLogger(PhoenixJDBCHelper.class);

    private static volatile PhoenixJDBCHelper instance;

//    // druid 数据库连接池
//    private volatile static DruidDataSource druidDataSource;
//

    private static volatile Connection connection;

    private PhoenixJDBCHelper() {
//        if (druidDataSource == null) {
//            initDruidDataSource();
//        }
        if (connection == null) {
            initConnection();
        }
    }

    public Connection getConnection() {
        return PhoenixJDBCHelper.connection;
    }
    public static PhoenixJDBCHelper getInstance() {
        if (instance == null) {
            synchronized (PhoenixJDBCHelper.class) {
                if (instance == null) {
                    instance = new PhoenixJDBCHelper();
                }
            }
        }
        return instance;
    }

    private void initConnection() {
        File file = FileUtil.loadResourceFile("jdbc.properties");
        Properties jdbcProp = new Properties();
        try {
            jdbcProp.load(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // phoenix url
        String jdbcUrl = jdbcProp.getProperty("phoenix.jdbcUrl");
        // phoenix driver 名字
        String driverClassName = jdbcProp.getProperty("phoenix.driverClassName");
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(jdbcUrl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


//    public DruidDataSource getDruidDataSource() {
//        return PhoenixJDBCHelper.druidDataSource;
//    }
//
//
//    private static void initDruidDataSource() {
//        File file = FileUtil.loadResourceFile("jdbc.properties");
//        Properties jdbcProp = new Properties();
//        try {
//            jdbcProp.load(new FileInputStream(file));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        // phoenix url
//        String jdbcUrl = jdbcProp.getProperty("phoenix.jdbcUrl");
//        // phoenix driver 名字
//        String driverClassName = jdbcProp.getProperty("phoenix.driverClassName");
//        // 当连接池启动时，初始化连接的个数，minIdle~maxActive，默认为3
//        String initialSize = jdbcProp.getProperty("phoenix.initialSize");
//        // 任何时间连接池中保存的最小连接数，默认3
//        String minIdle = jdbcProp.getProperty("phoenix.minIdle");
//        // 在任何时间连接池中所能拥有的最大连接数，默认15
//        String maxActive = jdbcProp.getProperty("phoenix.maxActive");
//        // 超过多长时间连接自动销毁，默认为0，即永远不会自动销毁
//        String timeBetweenEvictionRunsMillis = jdbcProp.getProperty("phoenix.timeBetweenEvictionRunsMillis");
//        // 获取连接等待超时的时间
//        String maxWait = jdbcProp.getProperty("phoenix.maxWait");
//        // 配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁
//        String useUnfairLock = jdbcProp.getProperty("phoenix.useUnfairLock");
//
//        druidDataSource = new DruidDataSource();
//        if (jdbcUrl == null || driverClassName == null) {
//            return;
//        }
//        druidDataSource.setUrl(jdbcUrl);
//        druidDataSource.setDriverClassName(driverClassName);
//        if (minIdle != null) {
//            druidDataSource.setMinIdle(Integer.parseInt(minIdle));
//        }
//        if (initialSize != null) {
//            druidDataSource.setInitialSize(Integer.parseInt(initialSize));
//        }
//        if (maxActive != null) {
//            druidDataSource.setMaxActive(Integer.parseInt(maxActive));
//        }
//        if (maxWait != null) {
//            druidDataSource.setMaxWait(Long.parseLong(maxWait));
//        }
//        if (timeBetweenEvictionRunsMillis != null) {
//            druidDataSource.setTimeBetweenConnectErrorMillis(Long.parseLong(timeBetweenEvictionRunsMillis));
//        }
//        if (useUnfairLock != null) {
//            druidDataSource.setUseUnfairLock(Boolean.parseBoolean(useUnfairLock));
//        }
//    }


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
