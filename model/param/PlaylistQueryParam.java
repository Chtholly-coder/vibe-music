package cn.edu.chtholly.model.param;

import lombok.Data;

@Data
public class PlaylistQueryParam {
    private Integer pageNum = 1;    // 页码，默认1
    private Integer pageSize = 10;  // 每页条数，默认10
    private String title;           // 歌单标题（模糊查询）
    private String style;           // 歌单风格（精准查询）
}