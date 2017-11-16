package com.hzgc.hbase.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.hzgc.util.FileUtil;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * JDBC工具类 乔凯峰（内）
 */
public class JDBCUtil {
    private static Logger LOG = Logger.getLogger(JDBCUtil.class);
//    private static JDBCUtil instance = null;
//    private static DataSource dataSource = new DruidDataSource();
//    private static Properties propertie = new Properties();

//    private JDBCUtil() {
//    }

    /*
      加载数据源配置信息
     */
    static {
//        try {
//            File resourceFile = FileUtil.loadResourceFile("jdbc.properties");
//            if (resourceFile != null) {
//                propertie.load(new FileInputStream(resourceFile));
//            }
//            dataSource = DruidDataSourceFactory.createDataSource(propertie);
//            dataSource.getConnection().close();
//        } catch (Exception e) {
//            log.info("get jdbc.properties failure");
//        }
    }

    /**
     * 获取单例
     *
     * @return 返回JDBCUtil单例对象
     */
//    public static JDBCUtil getInstance() {
//        if (instance == null) {
//            synchronized (JDBCUtil.class) {
//                if (instance == null) {
//                    instance = new JDBCUtil();
//                }
//            }
//        }
//        return instance;
//    }

    /**
     * 获取数据库连接池连接
     *
     * @return 返回Connection对象
     */
    public static Connection getConnection() {
        long start = System.currentTimeMillis();
        Connection conn = null;
//        try {
//            return dataSource.getConnection();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection("jdbc:hive2://172.18.18.105:23040/");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        LOG.info("get jdbc connection time is:" + (System.currentTimeMillis() - start));
        return conn;
    }
}
