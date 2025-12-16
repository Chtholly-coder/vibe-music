package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 歌单列表项VO（对应前端响应格式）
 */
@Data
public class PlaylistItemVO {
    private Long playlistId;      // 对应数据库id
    private String title;         // 歌单标题
    private String coverUrl;      // 封面URL
    private String introduction;  // 歌单简介
    private String style;         // 歌单风格
}