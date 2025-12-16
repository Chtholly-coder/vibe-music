package cn.edu.chtholly.dao;

import cn.edu.chtholly.model.entity.Playlist;
import cn.edu.chtholly.model.vo.FavoritePlaylistItemVO;

import java.util.List;

public interface PlaylistDao {
    List<Playlist> selectRandom10();

    // 根据ID查询歌单（存在则返回非null）
    Object selectById(Long playlistId);

    // 查询所有歌单总数（带筛选）
    Long selectAllPlaylistTotal(String title, String style);

    // 分页查询所有歌单（带筛选）
    List<FavoritePlaylistItemVO> selectAllPlaylistPage(String title, String style, int offset, int pageSize);

    // 插入歌单（返回自增ID）
    int insert(Playlist playlist);
    // 更新歌单信息（不含封面）
    int update(Playlist playlist);
    // 更新歌单封面
    int updateCover(Long id, String coverUrl);
    // 根据id删除歌单
    int deleteById(Long id);
    // 批量删除歌单
    int deleteByIds(List<Long> ids);
    // 根据id查询歌单
    Playlist selectDetailById(Long id);

    // 管理员专用分页查询（返回完整Playlist实体）
    List<Playlist> selectAdminPlaylistPage(String title, String style, int offset, int pageSize);
}