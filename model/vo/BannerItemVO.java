package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 轮播图列表项VO（对应前端响应格式）
 */
@Data
public class BannerItemVO {
    private Long bannerId;        // 对应数据库id
    private String bannerUrl;     // 轮播图URL
    private String bannerStatus;  // 前端状态（"ENABLE"/"DISABLE"）
}