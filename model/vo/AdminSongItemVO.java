package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 管理员查询歌曲列表VO（与前端示例字段完全匹配）
 */
@Data
public class AdminSongItemVO {
    private Long songId;         // 歌曲ID
    private String artistName;   // 艺术家名称
    private String songName;     // 歌曲名称
    private String album;        // 专辑名称
    private String lyric;        // 歌词
    private String duration;     // 时长
    private String style;        // 歌曲风格
    private String coverUrl;     // 歌曲封面URL
    private String audioUrl;     // 音频URL
    private String releaseTime;  // 发行时间
}