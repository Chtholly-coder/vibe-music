package cn.edu.chtholly.dao.impl;

import cn.edu.chtholly.dao.AdminDao;
import cn.edu.chtholly.model.entity.Admin;
import cn.edu.chtholly.util.JdbcUtil;

import java.sql.SQLException;

public class AdminDaoImpl implements AdminDao {
    @Override
    public Admin selectByUsername(String username) {
        String sql = "SELECT id, username, password FROM tb_admin WHERE username = ?";
        return JdbcUtil.queryOne(sql, rs -> {
            try {
                Admin admin = new Admin();
                admin.setId(rs.getLong("id"));
                admin.setUsername(rs.getString("username"));
                admin.setPassword(rs.getString("password"));
                return admin;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, username);
    }
}