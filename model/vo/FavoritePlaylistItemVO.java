package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 收藏歌单列表项VO（响应给前端）
 */
@Data
public class FavoritePlaylistItemVO {
    private Long playlistId;  // 歌单ID（对应tb_playlist.id）
    private String title;     // 歌单标题
    private String coverUrl;  // 歌单封面URL
}