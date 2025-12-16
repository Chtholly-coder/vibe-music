package cn.edu.chtholly.model.vo;

import lombok.Data;

@Data
public class ArtistSongItemVO {
    private Long songId;
    private String songName;
    private String artistName;
    private String album;
    private String duration;
    private String coverUrl;
    private String audioUrl;
    private Integer likeStatus; // 0-未收藏，1-已收藏
    private String releaseTime; // 格式：yyyy-MM-dd
}