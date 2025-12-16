package cn.edu.chtholly.model.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 歌曲列表查询请求参数（接收前端POST的JSON）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SongQueryParam {
    private Integer pageNum = 1;    // 页码，默认1
    private Integer pageSize = 20;  // 每页条数，默认20
    private Long artistId;          // 歌手ID（新增，用于筛选）
    private String songName = "";   // 歌曲名称（模糊查询）
    private String artistName = ""; // 艺术家名称（模糊查询）
    private String album = "";      // 专辑名称（模糊查询）
}