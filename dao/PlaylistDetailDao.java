package cn.edu.chtholly.dao;

import cn.edu.chtholly.model.entity.Playlist;
import cn.edu.chtholly.model.entity.Style;
import cn.edu.chtholly.model.vo.PlaylistSongItemVO;
import java.util.List;
import java.util.Map;

/**
 * 歌单详情DAO接口（仅操作现有表+tb_style）
 */
public interface PlaylistDetailDao {
    // 1. 根据歌单ID查询歌单基本信息（tb_playlist）
    Playlist selectPlaylistById(Long playlistId);

    // 2. 根据歌单style查询对应的风格表记录（tb_style.name = 歌单style）
    Style selectStyleByName(String styleName);

    // 3. 查询所有歌曲中style包含目标风格的歌曲（关联艺术家）
    List<PlaylistSongItemVO> selectSongsByStyle(String styleName);

    // 4. 检查用户是否收藏该歌单（tb_user_favorite，type=1表示歌单收藏）
    Integer checkPlaylistCollectStatus(Long userId, Long playlistId);
    Map<Long, Integer> batchCheckSongCollectStatus(Long userId, List<Long> songIds);
}