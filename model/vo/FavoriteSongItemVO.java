package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 收藏列表中的单首歌曲信息
 */
@Data
public class FavoriteSongItemVO {
    private Long songId;         // 歌曲ID
    private String songName;     // 歌曲名称
    private String artistName;   // 艺术家名称
    private String album;        // 专辑名称
    private String duration;     // 时长
    private String coverUrl;     // 封面URL
    private String audioUrl;     // 音频URL
    private Integer likeStatus;  // 收藏状态（1-已收藏）
    private String releaseTime;  // 发行时间
}