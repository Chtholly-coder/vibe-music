package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 添加歌单评论请求参数（对应type=1）
 */
@Data
public class AddPlaylistCommentVO {
    private Long playlistId; // 歌单ID（对应tb_comment.play_list）
    private String content;  // 评论内容
}