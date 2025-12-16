package cn.edu.chtholly.model.entity;

import lombok.Data;

@Data
public class Banner {
    private Long id;             // 对应表中id
    private String bannerUrl;    // 对应表中banner_url
    private Integer status;      // 对应表中status
}