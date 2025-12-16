package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 重置密码请求参数
 */
@Data
public class ResetPasswordVO {
    private String email;          // 绑定的邮箱
    private String verificationCode; // 验证码
    private String newPassword;    // 新密码
    private String repeatPassword; // 重复新密码
}