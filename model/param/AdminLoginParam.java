package cn.edu.chtholly.model.param;

import lombok.Data;

@Data
public class AdminLoginParam {
    private String username;     // 用户名
    private String password;     // 密码
}