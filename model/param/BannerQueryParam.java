package cn.edu.chtholly.model.param;

import lombok.Data;

/**
 * 轮播图查询参数（对应前端请求体）
 */
@Data
public class BannerQueryParam {
    private Integer pageNum;      // 页码（默认1）
    private Integer pageSize;     // 每页条数（默认5）
    private String bannerStatus;  // 前端状态（"ENABLE"/"DISABLE"）
    private Boolean background;   // 前端背景参数（预留，可用于筛选）
}