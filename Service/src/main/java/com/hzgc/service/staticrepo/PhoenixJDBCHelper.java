package com.hzgc.service.staticrepo;

import com.hzgc.util.common.FileUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.log4j.Logger;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class PhoenixJDBCHelper {
    private Logger LOG = Logger.getLogger(PhoenixJDBCHelper.class);

    // 数据库连接池
    private volatile static ComboPooledDataSource comboPooledDataSource;

    // 数据库连接对象
    private volatile static Connection conn;

    private PhoenixJDBCHelper() {}

    public static ComboPooledDataSource getComboPooledDataSource() {
        if (comboPooledDataSource == null) {
            synchronized (PhoenixJDBCHelper.class) {
                if (comboPooledDataSource == null) {
                    initDBSource();
                }
            }
        }
        return comboPooledDataSource;
    }

    public static Connection getConnection() {
        if (conn == null) {
            synchronized (PhoenixJDBCHelper.class) {
                if (conn == null) {
                    initConnection();
                }
            }
        }
        return  conn;
    }

    private static void initConnection() {
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
        try {
            Class.forName(phoenixJDBCDriver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(phoenixJDBCURL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initDBSource() {
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
        // 声明当连接池中连接耗尽时再一次新生成多少个连接，默认为3个
        String phoenixJDBCAcquireIncrement = jdbcProp.getProperty("phoenix.jdbc.acquireIncrement");
        // 当连接池启动时，初始化连接的个数，必须在minPoolSize~maxPoolSize之间，默认为3
        String phoenixJDBCInitialPoolSize = jdbcProp.getProperty("phoenix.jdbc.initialPoolSize");
        // 任何时间连接池中保存的最小连接数，默认3
        String phoenixJDBCMinPoolSize = jdbcProp.getProperty("phoenix.jdbc.minPoolSize");
        // 在任何时间连接池中所能拥有的最大连接数，默认15
        String phoenixJDBCMaxPoolSize = jdbcProp.getProperty("phoenix.jdbc.maxPoolSize");
        // 超过多长时间连接自动销毁，默认为0，即永远不会自动销毁
        String phoenixMaxIdleTime = jdbcProp.getProperty("phoenix.jdbc.maxIdleTime");

        if (phoenixJDBCURL ==null || phoenixJDBCDriver == null) {
            return;
        }


        comboPooledDataSource = new ComboPooledDataSource();
        try {
            comboPooledDataSource.setDriverClass(phoenixJDBCDriver);
            comboPooledDataSource.setJdbcUrl(phoenixJDBCURL);
            if (phoenixJDBCMaxPoolSize != null) {
                comboPooledDataSource.setMaxPoolSize(Integer.parseInt(phoenixJDBCMaxPoolSize));
            }
            if (phoenixJDBCAcquireIncrement != null) {
                comboPooledDataSource.setAcquireIncrement(Integer.parseInt(phoenixJDBCAcquireIncrement));
            }
            if (phoenixJDBCInitialPoolSize != null) {
                comboPooledDataSource.setInitialPoolSize(Integer.parseInt(phoenixJDBCInitialPoolSize));
            }
            if (phoenixJDBCMinPoolSize != null) {
                comboPooledDataSource.setMinPoolSize(Integer.parseInt(phoenixJDBCMinPoolSize));
            }
            if (phoenixMaxIdleTime != null) {
                comboPooledDataSource.setMaxIdleTime(Integer.parseInt(phoenixMaxIdleTime));
            }

        } catch (PropertyVetoException e) {
            e.printStackTrace();
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
