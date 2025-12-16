package cn.edu.chtholly.model.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Feedback {
    private Long id;             // id（bigint）
    private Long userId;         // user_id（bigint）
    private String feedback;     // feedback（char255）
    private Date createTime;     // create_time（datetime）
}