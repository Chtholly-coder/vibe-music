package cn.edu.chtholly.dao;

import cn.edu.chtholly.model.entity.UserFavorite;
import cn.edu.chtholly.model.param.FavoriteQueryParam;
import cn.edu.chtholly.model.vo.FavoritePlaylistItemVO;
import cn.edu.chtholly.model.vo.FavoriteSongItemVO;

import java.util.List;

public interface FavoriteDao {
    // 查询用户是否收藏了歌曲（type=0）
    UserFavorite selectByUserAndSong(Long userId, Long songId);

    // 新增歌曲收藏记录（type=0）
    int insert(UserFavorite favorite);

    // 取消歌曲收藏（删除type=0的记录）
    int deleteByUserIdAndSongId(Long userId, Long songId);

    List<FavoriteSongItemVO> selectFavoriteSongsByPage(Long userId, FavoriteQueryParam param);

    // 带条件的收藏总数查询
    int countByCondition(Long userId, FavoriteQueryParam param);

    // 查询用户收藏歌单总数（支持筛选）
    Long selectFavoritePlaylistTotal(Long userId, String title, String style);

    // 分页查询用户收藏歌单列表（支持筛选）
    List<FavoritePlaylistItemVO> selectFavoritePlaylistPage(
            Long userId, String title, String style, int offset, int pageSize);

    // 查询用户是否已收藏歌单
    UserFavorite selectByUserAndPlaylist(Long userId, Long playlistId);

    // 插入歌单收藏记录
    int insertPlaylist(UserFavorite favorite);

    // 删除用户的歌单收藏记录
    int deleteByUserIdAndPlaylistId(Long userId, Long playlistId);

}