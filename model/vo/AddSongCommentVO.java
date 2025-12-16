package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 添加歌曲评论请求参数（对应type=0）
 */
@Data
public class AddSongCommentVO {
    private Long songId;    // 歌曲ID（对应tb_comment.song_id）
    private String content; // 评论内容
}