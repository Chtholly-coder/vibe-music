package cn.edu.chtholly.model.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class User {
    @JsonAlias("userId")
    private Long id;                 // tb_user.id（bigint）
    private String username;         // tb_user.username（char20）
    private String password;         // tb_user.password（char64，MD5加密后存储）
    private String phone;            // tb_user.phone（char11）
    private String email;            // tb_user.email（char128）
    private String userAvatar;       // tb_user.user_avatar（char255）
    private String introduction;     // tb_user.introduction（char255）
    private Date createTime;         // tb_user.create_time（datetime）
    private Date updateTime;         // tb_user.update_time（datetime）
    @JsonAlias("userStatus")
    private Integer status;          // tb_user.status（tinyint：0-启用，1-禁止）
}