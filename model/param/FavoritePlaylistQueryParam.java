package cn.edu.chtholly.model.param;

import lombok.Data;

/**
 * 收藏歌单查询VO（接收前端请求参数）
 */
@Data
public class FavoritePlaylistQueryParam {
    private Integer pageNum = 1;    // 页码，默认1
    private Integer pageSize = 20;  // 每页条数，默认20
    private String title = "";      // 歌单标题筛选（模糊查询）
    private String style = "";      // 歌单风格筛选（精准查询）
}