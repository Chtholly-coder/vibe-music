package cn.edu.chtholly.model.vo;

import lombok.Data;

@Data
public class SongDetailVO {
    private Long songId;         // 歌曲id（对应tb_song.id）
    private String songName;     // 歌曲名称（对应tb_song.name）
    private String artistName;   // 艺术家名称（对应tb_artist.name）
    private String album;        // 专辑（对应tb_song.album）
    private String lyric;        // 歌词（对应tb_song.lyric）
    private String duration;     // 时长（对应tb_song.duration）
    private String coverUrl;     // 封面URL（对应tb_song.cover_url）
    private String audioUrl;     // 音频URL（对应tb_song.audio_url）
    private String releaseTime;  // 发行时间（格式化后，对应tb_song.release_time）
    private Integer likeStatus;  // 当前用户是否喜欢（true/false/null）
    private CommentVO[] comments; // 评论列表
}