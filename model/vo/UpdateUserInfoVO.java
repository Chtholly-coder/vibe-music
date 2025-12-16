package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 更新用户信息请求参数
 */
@Data
public class UpdateUserInfoVO {
    private Long userId;         // 用户ID（必须与登录用户一致）
    private String username;     // 用户名（可选）
    private String phone;        // 手机号（可选）
    private String email;        // 邮箱（可选）
    private String introduction; // 个人简介（可选）
}