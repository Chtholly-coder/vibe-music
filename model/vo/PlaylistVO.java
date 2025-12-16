package cn.edu.chtholly.model.vo;

import lombok.Data;

@Data
public class PlaylistVO {
    private Long playlistId;     // 对应数据库id
    private String title;        // 对应数据库title
    private String coverUrl;     // 对应数据库cover_url
}