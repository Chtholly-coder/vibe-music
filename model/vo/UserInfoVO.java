package cn.edu.chtholly.model.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    private Long userId;       // 用户ID（对应tb_user.id）
    private String username;   // 用户名（tb_user.username）
    private String phone;      // 手机号（tb_user.phone）
    private String email;      // 邮箱（tb_user.email）
    private String userAvatar; // 头像URL（tb_user.user_avatar）
    private String introduction; // 简介（tb_user.introduction）
}