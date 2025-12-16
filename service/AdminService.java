package cn.edu.chtholly.service;

import cn.edu.chtholly.model.Result;

public interface AdminService {
    /**
     * 管理员登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含JWT令牌
     */
    Result<String> login(String username, String password);

    /**
     * 管理员登出
     * @param token JWT令牌
     * @return 登出结果
     */
    Result<Void> logout(String token);
}