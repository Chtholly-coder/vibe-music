package cn.edu.chtholly.model.entity;

import lombok.Data;
import java.util.Date;

@Data
public class UserFavorite {
    private Long id;             // id（bigint）
    private Long userId;         // user_id（bigint）
    private Integer type;        // type（tinyint：0-歌曲，1-歌单）
    private Long songId;         // song_id（bigint，关联tb_song.id）
    private Long playlistId;     // playlist_id（bigint，暂不使用）
    private Date createTime;     // create_time（datetime，对应createtime）
}