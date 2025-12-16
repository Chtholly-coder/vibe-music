package cn.edu.chtholly.model.vo;

import lombok.Data;
import java.util.List;

/**
 * 歌单详情响应VO（与示例格式完全一致）
 */
@Data
public class PlaylistDetailVO {
    private Long playlistId;     // 歌单ID
    private String title;        // 歌单标题
    private String coverUrl;     // 歌单封面URL
    private String introduction; // 歌单简介
    private List<PlaylistSongItemVO> songs; // 关联的歌曲列表
    private Integer likeStatus;  // 歌单收藏状态（0-未收藏，1-已收藏）
    private CommentVO[] comments; // 评论列表（空数组）
}