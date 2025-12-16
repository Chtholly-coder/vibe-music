package cn.edu.chtholly.service;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.User;
import cn.edu.chtholly.model.param.UserQueryParam;
import cn.edu.chtholly.model.vo.*;
import cn.edu.chtholly.model.vo.*;
import jakarta.servlet.http.Part;

import java.util.List;

public interface UserService {
    Result<String> login(LoginVO loginVO);

    // 获取当前登录用户信息
    Result<UserInfoVO> getUserInfo();

    /**
     * 发送注册验证码
     * @param email 收件人邮箱
     * @return 发送结果
     */
    Result<String> sendVerificationCode(String email);

    /**
     * 用户注册（校验验证码+存储用户）
     * @param registerVO 注册参数
     * @return 注册结果
     */
    Result<String> register(RegisterVO registerVO);

    /**
     * 更新用户信息（需登录，只能更新本人信息）
     */
    Result<String> updateUserInfo(UpdateUserInfoVO vo);

    /**
     * 删除账号（需登录，只能删除本人账号）
     */
    Result<String> deleteAccount();

    /**
     * 重置密码（无需登录，通过邮箱验证码验证）
     */
    Result<String> resetPassword(ResetPasswordVO vo);


    // 更新用户头像
    Result<String> updateUserAvatar(Long userId, Part avatarPart);

    // 分页查询用户
    Result<PageResultVO> getAllUsers(UserQueryParam param);

    // 添加用户
    Result<Void> addUser(User user);

    // 更新用户
    Result<Void> updateUser(User user);

    // 更新用户状态
    Result<Void> updateUserStatus(Long userId, Integer status);

    // 删除用户
    Result<Void> deleteUser(Long userId);

    // 批量删除用户
    Result<Void> deleteUsers(List<Long> userIds);
}