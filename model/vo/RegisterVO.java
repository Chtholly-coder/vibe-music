package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 注册请求参数封装（对应前端提交的JSON）
 */
@Data
public class RegisterVO {
    private String username;         // 用户名
    private String email;            // 注册邮箱
    private String password;         // 明文密码（后端加密）
    private String verificationCode; // 验证码
}