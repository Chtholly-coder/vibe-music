package cn.edu.chtholly.model.vo;

import lombok.Data;

@Data
public class BannerVO {
    private Long bannerId;       // 对应数据库id
    private String bannerUrl;    // 对应数据库banner_url
}