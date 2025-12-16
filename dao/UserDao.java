package cn.edu.chtholly.dao;

import cn.edu.chtholly.model.entity.User;
import cn.edu.chtholly.model.param.UserQueryParam;
import cn.edu.chtholly.model.vo.UserItemVO;

import java.sql.Timestamp;
import java.util.List;

public interface UserDao {
    // 根据邮箱查询用户（用于登录验证）
    User selectByEmail(String email);

    // 新增：根据ID查询用户信息（用于获取用户详情）
    User selectById(Long id);


    // 检查邮箱是否已注册
    boolean checkEmailExists(String email);

    // 新增用户
    int insertUser(User user);


    // 更新用户信息（只更新非null字段）
    int updateUserInfo(User user);


    // 根据用户ID删除账号（级联删除关联数据，如评论、收藏）
    int deleteUserById(Long userId);


    // 根据邮箱查询用户（用于重置密码）
    User getUserByEmail(String email);

    // 查询用户旧头像
    String queryUserAvatarById(Long userId);

    // 更新用户头像
    int updateUserAvatar(Long userId, String newAvatarUrl, Timestamp updateTime);

    // 新增：分页查询用户列表（返回VO）
    List<UserItemVO> selectUserPage(UserQueryParam param, int offset);

    // 新增：查询符合条件的用户总数
    Long selectUserTotal(UserQueryParam param);

    // 新增：更新用户状态（单独提取，避免与updateUserInfo耦合）
    int updateUserStatus(Long userId, Integer status);

    // 批量删除用户
    int deleteUserByIds(List<Long> ids);

}