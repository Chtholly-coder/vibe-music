package cn.edu.chtholly.service;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.param.FavoriteQueryParam;
import cn.edu.chtholly.model.param.FavoritePlaylistQueryParam;
import cn.edu.chtholly.model.vo.FavoriteSongsVO;
import cn.edu.chtholly.model.vo.PageResultVO;

/**
 * 收藏相关业务逻辑接口
 */
public interface FavoriteService {
    // 收藏歌曲（POST /favorite/collectSong）
    Result<Void> collectSong(Long userId, Long songId);

    // 取消收藏（DELETE /favorite/cancelCollectSong）
    Result<Void> cancelCollectSong(Long userId, Long songId);

    // 获取收藏列表（GET /favorite/getFavoriteSongs）
    Result<FavoriteSongsVO> getFavoriteSongs(Long userId, FavoriteQueryParam param);

    // 查询用户收藏歌单（分页+筛选）
    Result<PageResultVO> getFavoritePlaylists(FavoritePlaylistQueryParam queryVO);

    // 收藏歌单
    Result<Void> collectPlaylist(Long userId, Long playlistId);

    // 取消收藏歌单
    Result<Void> cancelCollectPlaylist(Long userId, Long playlistId);


}