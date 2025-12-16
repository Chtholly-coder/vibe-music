package cn.edu.chtholly.model.vo;

import lombok.Data;
import java.util.List;

/**
 * 通用分页响应数据封装（支持任意类型的分页数据）
 * @param <T> 分页数据的类型（如 PlaylistSongItemVO、FavoritePlaylistItemVO 等）
 */
@Data
public class PageResultVO<T> {
    private Long total;          // 总记录数（不变）
    private List<T> items;       // 分页数据（泛型 T，适配任意类型）
}