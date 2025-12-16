package cn.edu.chtholly.util;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * JDBC工具类
 * 提供通用的CRUD操作，自动管理连接资源
 */
public class JdbcUtil {
    // 从Druid连接池获取数据源
    private static DataSource dataSource = DruidPoolUtil.getDataSource();

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭资源（Connection、PreparedStatement、ResultSet）
     */
    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (ps != null) {
            try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (conn != null) {
            try {
                // 归还连接到连接池（并非真正关闭）
                conn.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /**
     * 通用查询（返回单条记录）
     * @param sql SQL语句
     * @param rowMapper 结果集映射器（将ResultSet转换为实体类）
     * @param params SQL参数
     * @return 实体对象（T）
     */
    public static <T> T queryOne(String sql, Function<ResultSet, T> rowMapper, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setParams(ps, params); // 设置SQL参数
            rs = ps.executeQuery();
            if (rs.next()) {
                return rowMapper.apply(rs); // 映射结果集
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, ps, rs); // 释放资源
        }
        return null;
    }

    /**
     * 通用查询（返回多条记录）
     * @param sql SQL语句
     * @param rowMapper 结果集映射器
     * @param params SQL参数
     * @return 实体列表（List<T>）
     */
    public static <T> List<T> queryList(String sql, Function<ResultSet, T> rowMapper, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<T> list = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setParams(ps, params);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rowMapper.apply(rs)); // 映射并添加到列表
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, ps, rs);
        }
        return list;
    }

    /**
     * 通用更新（插入/更新/删除）
     * @param sql SQL语句
     * @param params SQL参数
     * @return 影响行数
     */
    public static int update(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setParams(ps, params);
            return ps.executeUpdate(); // 执行更新
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, ps, null);
        }
        return 0;
    }

    /**
     * 为PreparedStatement设置参数
     */
    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]); // SQL参数索引从1开始
            }
        }
    }

    /**
     * 执行查询，返回键值对映射（用于批量查询ID对应的状态）
     * 兼容现有 Function<ResultSet, T> 接口，复用原有 close 方法
     * @param sql SQL语句
     * @param keyMapper 从ResultSet提取key的逻辑（用现有Function接口）
     * @param valueMapper 从ResultSet提取value的逻辑（用现有Function接口）
     * @param params SQL参数
     * @return 键值对映射
     */
    public static <K, V> Map<K, V> queryMap(String sql,
                                            Function<ResultSet, K> keyMapper,
                                            Function<ResultSet, V> valueMapper,
                                            Object... params) {
        Map<K, V> resultMap = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setParams(ps, params); // 复用现有 setParams 方法，避免重复代码
            rs = ps.executeQuery();
            // 映射结果到Map（用现有Function接口，无需新增ResultSetMapper）
            while (rs.next()) {
                K key = keyMapper.apply(rs);
                V value = valueMapper.apply(rs);
                resultMap.put(key, value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("查询Map失败：" + e.getMessage(), e);
        } finally {
            close(conn, ps, rs);
        }
        return resultMap;
    }
}