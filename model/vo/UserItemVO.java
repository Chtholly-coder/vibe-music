package cn.edu.chtholly.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Date;

/**
 * 用户列表项VO（对应前端响应格式）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UserItemVO {
    private Long userId;          // 对应数据库id
    private String username;      // 用户名
    private String phone;         // 手机号
    private String email;         // 邮箱
    private String userAvatar;    // 头像URL
    private String introduction;  // 简介
    private Date createTime;      // 创建时间
    private Date updateTime;      // 更新时间
    private String userStatus;    // 前端状态（"ENABLE"/"DISABLE"）
}