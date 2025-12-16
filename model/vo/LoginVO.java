package cn.edu.chtholly.model.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String email;    // 前端传入的邮箱（对应tb_user.email）
    private String password; // 前端传入的明文密码（需MD5加密后验证）
}