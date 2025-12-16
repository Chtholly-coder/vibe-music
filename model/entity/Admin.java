package cn.edu.chtholly.model.entity;

import lombok.Data;

@Data
public class Admin {
    private Long id;             // ID
    private String username;     // username
    private String password;     // password（MD5加密存储）
}