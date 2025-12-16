package cn.edu.chtholly.util;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import java.io.InputStream;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * Druid连接池初始化工具
 */
public class DruidPoolUtil {
    private static DataSource dataSource;

    static {
        try {
            // 加载Druid配置文件（resources/druid.properties）
            InputStream is = DruidPoolUtil.class.getClassLoader().getResourceAsStream("druid.properties");
            Properties props = new Properties();
            props.load(is);

            // 初始化Druid数据源
            dataSource = DruidDataSourceFactory.createDataSource(props);
        } catch (Exception e) {
            throw new RuntimeException("Druid连接池初始化失败", e);
        }
    }

    /**
     * 获取数据源
     */
    public static DataSource getDataSource() {
        return dataSource;
    }
}