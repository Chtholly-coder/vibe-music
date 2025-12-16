package cn.edu.chtholly.model.vo;

import lombok.Data;
import java.util.Date;

/**
 * 反馈列表项VO（对应前端响应格式）
 */
@Data
public class FeedbackItemVO {
    private Long feedbackId;      // 对应数据库id
    private Long userId;          // 用户id
    private String feedback;      // 反馈内容
    private Date createTime;      // 创建时间（格式：yyyy-MM-dd HH:mm:ss）
}