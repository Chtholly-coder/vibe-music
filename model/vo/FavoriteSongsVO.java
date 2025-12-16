package cn.edu.chtholly.model.vo;

import lombok.Data;
import java.util.List;

/**
 * 收藏歌曲列表响应VO（对应/getFavoriteSongs接口的data字段）
 */
@Data
public class FavoriteSongsVO {
    private Integer total; // 收藏总数
    private List<FavoriteSongItemVO> items; // 收藏歌曲列表
}