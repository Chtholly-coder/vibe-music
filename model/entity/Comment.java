package cn.edu.chtholly.model.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Comment {
    private Long id;             // 评论id（bigint）
    private Long userId;         // 用户id（bigint）
    private Long songId;         // 歌曲id（bigint，关联tb_song.id）
    private Long playlistId;     // 歌单id（bigint，暂不使用）
    private String content;      // 评论内容（char255）
    private Date createTime;     // 创建时间（datetime，对应create_time）
    private Integer type;        // 类型（tinyint：0-歌曲评论，1-歌单评论）
    private Long likeCount;      // 点赞数（bigint，对应like_count）
}