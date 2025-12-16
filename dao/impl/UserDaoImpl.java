package cn.edu.chtholly.dao.impl;

import cn.edu.chtholly.dao.UserDao;
import cn.edu.chtholly.model.entity.User;
import cn.edu.chtholly.util.JdbcUtil;
import cn.edu.chtholly.model.param.UserQueryParam;
import cn.edu.chtholly.model.vo.UserItemVO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserDaoImpl implements UserDao {

    // 登录时根据邮箱查询（只返回登录所需字段）
    @Override
    public User selectByEmail(String email) {
        String sql = "SELECT id, username, password, status FROM tb_user WHERE email = ?";

        return JdbcUtil.queryOne(sql, rs -> {
            try {
                // 只映射SQL返回的字段（id、username、password、status）
                return mapToLoginUser(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, email);
    }

    // 获取用户信息时根据ID查询
    @Override
    public User selectById(Long id) {
        String sql = """
            SELECT id, username, phone, email, user_avatar, introduction 
            FROM tb_user 
            WHERE id = ?
        """;

        return JdbcUtil.queryOne(sql, rs -> {
            try {
                return mapToUserInfo(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, id);
    }

    // 登录查询专用映射（只处理id、username、password、status）
    private User mapToLoginUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password")); // 登录需要验证密码
        user.setStatus(rs.getInt("status")); // 登录需要验证账号状态
        // 不处理phone、email等字段（SQL没查，结果集没有）
        return user;
    }

    // 用户信息查询专用映射
    private User mapToUserInfo(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPhone(rs.getString("phone"));      // 用户信息需要
        user.setEmail(rs.getString("email"));      // 用户信息需要
        user.setUserAvatar(rs.getString("user_avatar")); // 用户信息需要
        user.setIntroduction(rs.getString("introduction")); // 用户信息需要
        return user;
    }

    @Override
    public boolean checkEmailExists(String email) {
        // 校验邮箱是否已存在
        String sql = "SELECT COUNT(*) AS count FROM tb_user WHERE email = ?";
        Integer count = JdbcUtil.queryOne(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, email);
        return count != null && count > 0;
    }

    @Override
    public int insertUser(User user) {
        // 插入新用户
        String sql = """
            INSERT INTO tb_user (
                username, password, email, user_avatar, introduction, 
                create_time, update_time, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        Date now = new Date();
        return JdbcUtil.update(sql,
                user.getUsername().trim(), // 用户名去空格
                user.getPassword(), // 已加密的密码
                user.getEmail(),
                user.getUserAvatar() == null ? "" : user.getUserAvatar(), // 头像默认空
                user.getIntroduction() == null ? "" : user.getIntroduction(), // 简介默认空
                new Timestamp(now.getTime()), // 创建时间
                new Timestamp(now.getTime()), // 更新时间
                0 // 状态：0-启用，1-禁用
        );
    }

    @Override
    public int updateUserInfo(User user) {
        // 动态拼接SQL：只更新非null的字段，同时更新update_time
        StringBuilder sql = new StringBuilder("UPDATE tb_user SET update_time = ? ");
        List<Object> paramList = new ArrayList<>();
        paramList.add(new Timestamp(new Date().getTime())); // 第一个参数：update_time

        // 用户名非null则更新
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            sql.append(", username = ? ");
            paramList.add(user.getUsername().trim());
        }
        // 手机号非null则更新
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            sql.append(", phone = ? ");
            paramList.add(user.getPhone().trim());
        }
        // 邮箱非null则更新
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            sql.append(", email = ? ");
            paramList.add(user.getEmail().trim());
        }
        // 个人简介非null则更新
        if (user.getIntroduction() != null) {
            sql.append(", introduction = ? ");
            paramList.add(user.getIntroduction().trim());
        }
        // 关键新增：密码非null则更新（重置密码时会设置该字段）
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            sql.append(", password = ? ");
            paramList.add(user.getPassword().trim());
        }

        // 条件：只更新当前用户（防止越权）
        if (user.getId() == null) {
            throw new IllegalArgumentException("更新用户信息时，用户ID不能为空");
        }
        sql.append("WHERE id = ?");
        paramList.add(user.getId());

        // 执行更新
        return JdbcUtil.update(sql.toString(), paramList.toArray());
    }


    @Override
    public int deleteUserById(Long userId) {
        String sql = "DELETE FROM tb_user WHERE id = ?";
        return JdbcUtil.update(sql, userId);
    }

    @Override
    public User getUserByEmail(String email) {
        // 根据邮箱查询用户（用于重置密码校验）
        String sql = """
            SELECT id, username, password, email, status 
            FROM tb_user 
            WHERE email = ? AND status = 0
            """;
        return JdbcUtil.queryOne(sql, rs -> {
            User user = new User();
            try {
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setStatus(rs.getInt("status"));
                return user;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, email);
    }

    @Override
    public String queryUserAvatarById(Long userId) {
        String sql = "SELECT user_avatar FROM tb_user WHERE id = ?";
        return JdbcUtil.queryOne(
                sql,
                (ResultSet rs) -> {
                    try {
                        return rs.getString("user_avatar");
                    } catch (Exception e) {
                        return null;
                    }
                },
                userId
        );
    }

    @Override
    public int updateUserAvatar(Long userId, String newAvatarUrl, Timestamp updateTime) {
        String sql = "UPDATE tb_user SET user_avatar = ?, update_time = ? WHERE id = ?";
        return JdbcUtil.update(sql, newAvatarUrl, updateTime, userId);
    }

    // 分页查询用户列表
    @Override
    public List<UserItemVO> selectUserPage(UserQueryParam param, int offset) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, username, phone, email, user_avatar, introduction,
                   create_time, update_time, status
            FROM tb_user
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        // 1. 动态拼接查询条件
        // 用户名模糊查询
        if (param.getUsername() != null && !param.getUsername().trim().isEmpty()) {
            sql.append(" AND username LIKE ?");
            params.add("%" + param.getUsername().trim() + "%");
        }
        // 手机号模糊查询
        if (param.getPhone() != null && !param.getPhone().trim().isEmpty()) {
            sql.append(" AND phone LIKE ?");
            params.add("%" + param.getPhone().trim() + "%");
        }
        // 状态筛选（前端userStatus→数据库status）
        if (param.getUserStatus() != null) {
            int status = "1".equals(param.getUserStatus()) ? 1 : 0;
            sql.append(" AND status = ?");
            params.add(status);
        }

        // 2. 排序与分页
        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");
        params.add(param.getPageSize()); // 每页条数
        params.add(offset); // 偏移量（(pageNum-1)*pageSize）

        // 3. 执行查询并映射为UserItemVO
        return JdbcUtil.queryList(sql.toString(), rs -> {
            try {
                return mapToUserItemVO(rs);
            } catch (SQLException e) {
                throw new RuntimeException("用户列表映射失败", e);
            }
        }, params.toArray());
    }

    //  查询用户总数
    @Override
    public Long selectUserTotal(UserQueryParam param) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM tb_user WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 复用查询条件（与分页查询一致）
        if (param.getUsername() != null && !param.getUsername().trim().isEmpty()) {
            sql.append(" AND username LIKE ?");
            params.add("%" + param.getUsername().trim() + "%");
        }
        if (param.getPhone() != null && !param.getPhone().trim().isEmpty()) {
            sql.append(" AND phone LIKE ?");
            params.add("%" + param.getPhone().trim() + "%");
        }
        if (param.getUserStatus() != null) {
            int status = "ENABLE".equals(param.getUserStatus()) ? 0 : 1;
            sql.append(" AND status = ?");
            params.add(status);
        }

        return JdbcUtil.queryOne(sql.toString(), rs -> {
            try {
                return rs.getLong("total");
            } catch (SQLException e) {
                return 0L;
            }
        }, params.toArray());
    }

    // ========== 新增：更新用户状态 ==========
    @Override
    public int updateUserStatus(Long userId, Integer status) {
        String sql = "UPDATE tb_user SET status = ?, update_time = NOW() WHERE id = ?";
        return JdbcUtil.update(sql, status, userId);
    }

    // ========== 工具方法：ResultSet→UserItemVO映射 ==========
    private UserItemVO mapToUserItemVO(ResultSet rs) throws SQLException {
        UserItemVO vo = new UserItemVO();
        vo.setUserId(rs.getLong("id")); // 数据库id→前端userId
        vo.setUsername(rs.getString("username"));
        vo.setPhone(rs.getString("phone"));
        vo.setEmail(rs.getString("email"));
        vo.setUserAvatar(rs.getString("user_avatar"));
        vo.setIntroduction(rs.getString("introduction"));
        vo.setCreateTime(rs.getTimestamp("create_time"));
        vo.setUpdateTime(rs.getTimestamp("update_time"));
        // 数据库status（0/1）→ 前端userStatus（"ENABLE"/"DISABLE"）
        vo.setUserStatus(rs.getInt("status") == 0 ? "ENABLE" : "DISABLE");
        return vo;
    }

    @Override
    public int deleteUserByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        // 构建IN子句：?的数量匹配ID个数
        StringBuilder sql = new StringBuilder("DELETE FROM tb_user WHERE id IN (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            sql.append("?,");
            params.add(ids.get(i));
        }
        // 移除最后一个逗号
        sql.setLength(sql.length() - 1);
        sql.append(")");
        return JdbcUtil.update(sql.toString(), params.toArray());
    }
}