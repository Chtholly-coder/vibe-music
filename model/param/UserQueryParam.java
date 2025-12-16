package cn.edu.chtholly.model.param;

import lombok.Data;

/**
 * 用户查询参数（对应前端请求体）
 */
@Data
public class UserQueryParam {
    private Integer pageNum;      // 页码
    private Integer pageSize;     // 每页条数
    private String username;      // 用户名（模糊查询）
    private String phone;         // 手机号（模糊查询）
    private String userStatus;    // 前端状态（"ENABLE"/"DISABLE"，需转换为数据库status）
}