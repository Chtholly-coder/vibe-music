package cn.edu.chtholly.model.param;

import lombok.Data;

/**
 * 反馈查询参数（对应前端请求体）
 */
@Data
public class FeedbackQueryParam {
    private Integer pageNum;      // 页码（默认1）
    private Integer pageSize;     // 每页条数（默认20）
    private String keyword;       // 关键词搜索（模糊匹配feedback字段）
}