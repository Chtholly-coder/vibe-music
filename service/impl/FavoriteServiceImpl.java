package cn.edu.chtholly.service.impl;

import cn.edu.chtholly.dao.FavoriteDao;
import cn.edu.chtholly.dao.PlaylistDao;
import cn.edu.chtholly.dao.SongDao;
import cn.edu.chtholly.dao.impl.ArtistDaoImpl;
import cn.edu.chtholly.dao.impl.FavoriteDaoImpl;
import cn.edu.chtholly.dao.impl.PlaylistDaoImpl;
import cn.edu.chtholly.dao.impl.SongDaoImpl;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Song;
import cn.edu.chtholly.model.entity.UserFavorite;
import cn.edu.chtholly.model.param.FavoriteQueryParam;
import cn.edu.chtholly.model.vo.FavoritePlaylistItemVO;
import cn.edu.chtholly.model.param.FavoritePlaylistQueryParam;
import cn.edu.chtholly.model.vo.FavoriteSongItemVO;
import cn.edu.chtholly.model.vo.FavoriteSongsVO;
import cn.edu.chtholly.model.vo.PageResultVO;
import cn.edu.chtholly.service.FavoriteService;
import cn.edu.chtholly.util.RedisUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import cn.edu.chtholly.dao.ArtistDao;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 收藏相关业务逻辑实现
 */
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteDao favoriteDao = new FavoriteDaoImpl();
    private final SongDao songDao = new SongDaoImpl(); // 用于检查歌曲是否存在
    private final PlaylistDao playlistDao = new PlaylistDaoImpl();
    private final String HOT_SONGS_KEY = "hot:songs";
    private final String USER_FAVORITE_KEY_PREFIX = "user:favorite:songs:";
    private final ArtistDao artistDao = new ArtistDaoImpl();

    @Override
    public Result<Void> collectSong(Long userId, Long songId) {
        // 1. 校验歌曲是否存在
        Song song = songDao.selectById(songId);
        if (song == null) {
            return Result.error("歌曲不存在");
        }

        // 2. 校验是否已收藏
        if (favoriteDao.selectByUserAndSong(userId, songId) != null) {
            return Result.error("已收藏该歌曲");
        }

        // 3. 新增收藏记录
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setSongId(songId);
        favorite.setType(0);
        int rows = favoriteDao.insert(favorite);
        if (rows <= 0) {
            return Result.error("收藏失败，请重试");
        }

        //构建完整的FavoriteSongItemVO，缓存到Redis
        try {
            // 3.1 构建和你返回示例一致的VO对象
            FavoriteSongItemVO songVO = new FavoriteSongItemVO();
            songVO.setSongId(songId);
            songVO.setSongName(song.getName());
            songVO.setArtistName(artistDao.selectById(song.getArtistId()).getName()); // 关联查艺术家名
            songVO.setAlbum(song.getAlbum());
            songVO.setDuration(song.getDuration());
            songVO.setCoverUrl(song.getCoverUrl());
            songVO.setAudioUrl(song.getAudioUrl());
            songVO.setLikeStatus(1); // 已收藏
            songVO.setReleaseTime(song.getReleaseTime() != null ? song.getReleaseTime().toString() : "");

            // 3.2 同步Redis：收藏列表+VO缓存+热门分数
            RedisUtil.sAdd(USER_FAVORITE_KEY_PREFIX + userId, songId.toString());
            RedisUtil.setSongVO(songId, songVO); // 缓存完整VO
            RedisUtil.zIncrBy(HOT_SONGS_KEY, 1.0, songId.toString());
        } catch (Exception e) {
            System.err.println("收藏同步Redis失败：" + e.getMessage());
        }

        return Result.success(null);
    }


    @Override
    public Result<Void> cancelCollectSong(Long userId, Long songId) {
        // 1. 校验是否已收藏
        if (favoriteDao.selectByUserAndSong(userId, songId) == null) {
            return Result.error("未收藏该歌曲");
        }

        // 2. 删除数据库收藏记录
        int rows = favoriteDao.deleteByUserIdAndSongId(userId, songId);
        if (rows <= 0) {
            return Result.error("取消收藏失败，请重试");
        }

        // 同步删除Redis缓存
        try {
            RedisUtil.sRem(USER_FAVORITE_KEY_PREFIX + userId, songId.toString()); // 移除收藏列表
            RedisUtil.delSongVO(songId); // 删除VO缓存
            // 调整热门歌曲分数
            // 调整热门歌曲分数
            double currentScore = RedisUtil.zScore(HOT_SONGS_KEY, songId.toString());
            if (currentScore > 1) {
                RedisUtil.zIncrBy(HOT_SONGS_KEY, -1.0, songId.toString());
            } else {
                // 调用RedisUtil封装的zRem方法，解决private访问问题
                RedisUtil.zRem(HOT_SONGS_KEY, songId.toString());
            }
        } catch (Exception e) {
            System.err.println("取消收藏同步Redis失败：" + e.getMessage());
        }

        return Result.success(null);
    }

    @Override
    public Result<FavoriteSongsVO> getFavoriteSongs(Long userId, FavoriteQueryParam param) {
        // 1. 校验分页参数
        if (param.getPageNum() < 1) param.setPageNum(1);
        if (param.getPageSize() < 1 || param.getPageSize() > 100) param.setPageSize(10);
        int pageNum = param.getPageNum();
        int pageSize = param.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 2. 从Redis获取用户收藏的songId列表
        Set<String> favoriteSongIdStrs = RedisUtil.sMembers(USER_FAVORITE_KEY_PREFIX + userId);
        List<Long> favoriteSongIds = new ArrayList<>();
        for (String str : favoriteSongIdStrs) {
            try {
                favoriteSongIds.add(Long.parseLong(str));
            } catch (NumberFormatException e) {
                continue;
            }
        }

        FavoriteSongsVO vo = new FavoriteSongsVO();
        List<FavoriteSongItemVO> songItems = new ArrayList<>();

        if (!favoriteSongIds.isEmpty()) {
            // 3. 分页截取当前页的songId
            int start = offset;
            int end = Math.min(offset + pageSize, favoriteSongIds.size());
            if (start < favoriteSongIds.size()) {
                List<Long> currentPageSongIds = favoriteSongIds.subList(start, end);

                // 4. 批量获取VO对象（优先Redis，无则查数据库）
                for (Long songId : currentPageSongIds) {
                    // 4.1 先查Redis缓存的完整VO
                    FavoriteSongItemVO songVO = RedisUtil.getSongVO(songId, FavoriteSongItemVO.class);
                    if (songVO == null) {
                        // 4.2 Redis无缓存，查数据库并构建VO
                        List<FavoriteSongItemVO> dbItems = favoriteDao.selectFavoriteSongsByPage(userId, param);
                        for (FavoriteSongItemVO item : dbItems) {
                            if (item.getSongId().equals(songId)) {
                                songVO = item;
                                // 同步缓存到Redis，下次查询直接用
                                RedisUtil.setSongVO(songId, songVO);
                                break;
                            }
                        }
                    }
                    if (songVO != null) {
                        songItems.add(songVO);
                    }
                }
            }
            // 5. 设置总数
            vo.setTotal(favoriteSongIds.size());
        } else {
            // Redis无数据，走数据库查询（兼容旧逻辑）
            songItems = favoriteDao.selectFavoriteSongsByPage(userId, param);
            vo.setTotal(favoriteDao.countByCondition(userId, param));

            // 同步到Redis（预热缓存）
            try {
                for (FavoriteSongItemVO item : songItems) {
                    RedisUtil.sAdd(USER_FAVORITE_KEY_PREFIX + userId, item.getSongId().toString());
                    RedisUtil.setSongVO(item.getSongId(), item);
                }
            } catch (Exception e) {
                System.err.println("同步收藏列表到Redis失败：" + e.getMessage());
            }
        }

        vo.setItems(songItems);
        // 返回的Result结构和你的示例完全一致
        return Result.success(vo);
    }

    @Override
    public Result<PageResultVO> getFavoritePlaylists(FavoritePlaylistQueryParam queryVO) {
        // 1. 获取登录用户ID
        Long userId = UserThreadLocal.getUserId();

        // 2. 校验分页参数（避免非法值）
        int pageNum = queryVO.getPageNum() < 1 ? 1 : queryVO.getPageNum();
        int pageSize = queryVO.getPageSize() < 1 || queryVO.getPageSize() > 100 ? 20 : queryVO.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 3. 处理筛选参数（去除首尾空格）
        String title = queryVO.getTitle() != null ? queryVO.getTitle().trim() : "";
        String style = queryVO.getStyle() != null ? queryVO.getStyle().trim() : "";

        // 4. 查询总数和分页列表
        Long total = favoriteDao.selectFavoritePlaylistTotal(userId, title, style);
        List<FavoritePlaylistItemVO> playlistVOList = favoriteDao.selectFavoritePlaylistPage(
                userId, title, style, offset, pageSize);

        // 5. 封装分页响应结果
        PageResultVO pageResult = new PageResultVO();
        pageResult.setTotal(total);
        pageResult.setItems(playlistVOList);

        return Result.success(pageResult);
    }

    @Override
    public Result<Void> collectPlaylist(Long userId, Long playlistId) {
        // 1. 校验歌单是否存在
        if (playlistDao.selectById(playlistId) == null) {
            return Result.error("歌单不存在");
        }

        // 2. 校验是否已收藏（type=1 表示歌单）
        if (favoriteDao.selectByUserAndPlaylist(userId, playlistId) != null) {
            return Result.error("已收藏该歌单");
        }

        // 3. 新增收藏记录（type=1）
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setPlaylistId(playlistId); // 设置歌单ID
        favorite.setType(1); // 明确标记为歌单收藏

        int rows = favoriteDao.insertPlaylist(favorite);
        return rows > 0 ? Result.success(null, "添加成功") : Result.error("收藏失败，请重试");
    }

    @Override
    public Result<Void> cancelCollectPlaylist(Long userId, Long playlistId) {
        // 1. 校验是否已收藏

        // 2. 删除收藏记录（type=1）
        int rows = favoriteDao.deleteByUserIdAndPlaylistId(userId, playlistId);
        return rows > 0 ? Result.success(null, "删除成功") : Result.error("取消收藏失败，请重试");
    }
}