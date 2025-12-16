package cn.edu.chtholly.dao;

import cn.edu.chtholly.model.entity.Admin;

public interface AdminDao {
    /**
     * 根据用户名查询管理员
     * @param username 用户名
     * @return 管理员对象，不存在则返回null
     */
    Admin selectByUsername(String username);
}