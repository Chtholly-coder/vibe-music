package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 歌单内单首歌曲VO（与示例歌曲字段完全匹配）
 */
@Data
public class PlaylistSongItemVO {
    private Long songId;         // 歌曲ID
    private String songName;     // 歌曲名称
    private String artistName;   // 艺术家名称
    private String album;        // 专辑名称
    private String duration;     // 时长
    private String coverUrl;     // 歌曲封面URL
    private String audioUrl;     // 音频URL
    private Integer likeStatus;  // 歌曲收藏状态（0-未收藏）
    private String releaseTime;  // 发行时间
}