package cn.edu.chtholly.model.param;

import lombok.Data;

/**
 * 收藏列表查询参数（对应请求体）
 */
@Data
public class FavoriteQueryParam {
    private Integer pageNum = 1;      // 页码，默认第1页
    private Integer pageSize = 10;    // 每页条数，默认10条
    private String songName = "";     // 歌曲名模糊查询
    private String artistName = "";   // 艺术家名模糊查询
    private String album = "";        // 专辑名模糊查询
}