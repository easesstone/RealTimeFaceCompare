package com.hzgc.hbase.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.hzgc.util.FileUtil;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * JDBC工具类 乔凯峰（内）
 */
public class JDBCUtil {

    private static JDBCUtil instance = null;
    private static Logger log = Logger.getLogger(JDBCUtil.class);
    private static DataSource dataSource = new DruidDataSource();
    private static Connection conn;
    private static Properties propertie = new Properties();
    private static ResultSet rs;
    private static Statement stmt;

    private JDBCUtil() {
    }

    /**
     * 加载数据源配置信息
     *
     */
    static {
        try {
            File resourceFile = FileUtil.loadResourceFile("jdbc.properties");
            if (resourceFile != null) {
                propertie.load(new FileInputStream(resourceFile));
            }
            dataSource = DruidDataSourceFactory.createDataSource(propertie);
        } catch (Exception e) {
            log.info("get jdbc.properties failure");
        }
    }

    /**
     * 获取单例
     *
     * @return 返回JDBCUtil单例对象
     */
    public static JDBCUtil getInstance() {
        if (instance == null) {
            synchronized (JDBCUtil.class) {
                if (instance == null) {
                    instance = new JDBCUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 获取数据库连接池连接
     *
     * @return 返回Connection对象
     */
    public Connection getConnection() {
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 执行查询SQL语句
     *
     * @param sql 查询语句
     * @return rs
     */
    public ResultSet executeQuery(String sql) {
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (conn != null && stmt != null && rs != null) {
            try {
                conn.close();
                stmt.close();
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
