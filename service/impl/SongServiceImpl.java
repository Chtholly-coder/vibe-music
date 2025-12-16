package cn.edu.chtholly.service.impl;

import cn.edu.chtholly.dao.ArtistDao;
import cn.edu.chtholly.dao.CommentDao;
import cn.edu.chtholly.dao.FavoriteDao;
import cn.edu.chtholly.dao.SongDao;
import cn.edu.chtholly.dao.impl.ArtistDaoImpl;
import cn.edu.chtholly.dao.impl.CommentDaoImpl;
import cn.edu.chtholly.dao.impl.FavoriteDaoImpl;
import cn.edu.chtholly.dao.impl.SongDaoImpl;
import cn.edu.chtholly.model.entity.Artist;
import cn.edu.chtholly.model.entity.Comment;
import cn.edu.chtholly.model.entity.Song;
import cn.edu.chtholly.model.entity.UserFavorite;
import cn.edu.chtholly.model.vo.*;
import cn.edu.chtholly.model.PageResult;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.param.SongQueryParam;
import cn.edu.chtholly.service.SongService;
import cn.edu.chtholly.util.MinioUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SongServiceImpl implements SongService {

    // 初始化DAO
    private final SongDao songDao = new SongDaoImpl();
    private final ArtistDao artistDao = new ArtistDaoImpl();
    private final CommentDao commentDao = new CommentDaoImpl();
    private final FavoriteDao favoriteDao = new FavoriteDaoImpl();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Result<SongDetailVO> getSongDetail(Long songId) {
        // 1. 查询歌曲信息（不变）
        Song song = songDao.selectById(songId);
        if (song == null) {
            return Result.error("歌曲不存在");
        }

        // 2. 查询艺术家名称（不变）
        Artist artist = artistDao.selectById(song.getArtistId());
        String artistName = (artist != null && artist.getName() != null)
                ? artist.getName()
                : "未知艺术家";

        // 3. 查询评论+用户信息，转换为CommentVO
        List<Map<String, Object>> commentWithUserList = commentDao.selectCommentWithUserBySongId(songId);
        CommentVO[] commentVOs = commentWithUserList.stream()
                .map(map -> {
                    // 构建Comment实体（从Map中提取评论信息）
                    Comment comment = new Comment();
                    comment.setId((Long) map.get("commentId"));
                    comment.setContent((String) map.get("content"));
                    comment.setCreateTime((java.sql.Timestamp) map.get("createTime"));
                    comment.setLikeCount((Long) map.get("likeCount"));
                    // 调用修改后的fromEntity方法，传入用户名和头像
                    return CommentVO.fromEntity(
                            comment,
                            (String) map.get("username"),
                            (String) map.get("userAvatar")
                    );
                })
                .toArray(CommentVO[]::new);

        // 4. 查询当前用户的喜欢状态（不变）
        Long userId = UserThreadLocal.getUserId();
        Integer likeStatus = 0; // 未登录/未收藏默认0
        if (userId != null) {
            UserFavorite favorite = favoriteDao.selectByUserAndSong(userId, songId);
            likeStatus = (favorite != null) ? 1 : 0;
        }

        // 5. 组装响应VO（不变）
        SongDetailVO vo = new SongDetailVO();
        vo.setSongId(song.getId());
        vo.setSongName(song.getName());
        vo.setArtistName(artistName);
        vo.setAlbum(song.getAlbum());
        vo.setLyric(song.getLyric());
        vo.setDuration(song.getDuration());
        vo.setCoverUrl(song.getCoverUrl());
        vo.setAudioUrl(song.getAudioUrl());
        vo.setReleaseTime(song.getReleaseTime() != null ? sdf.format(song.getReleaseTime()) : null);
        vo.setLikeStatus(likeStatus);
        vo.setComments(commentVOs);

        return Result.success(vo);
    }

    // 新增：分页查询所有歌曲（核心实现）
    @Override
    public Result<PageResultVO> getAllSongs(SongQueryParam queryVO) {
        // 1. 校验分页参数（避免非法值）
        int pageNum = queryVO.getPageNum() < 1 ? 1 : queryVO.getPageNum();
        int pageSize = queryVO.getPageSize() < 1 || queryVO.getPageSize() > 100 ? 20 : queryVO.getPageSize();
        int offset = (pageNum - 1) * pageSize; // 计算分页偏移量

        // 2. 查询符合条件的总条数（用于分页）
        Long total = songDao.selectSongTotal(
                queryVO.getSongName(),
                queryVO.getArtistName(),
                queryVO.getAlbum()
        );

        // 3. 分页查询歌曲列表（含艺术家名称）
        List<PlaylistSongItemVO> songVOList = songDao.selectSongPage(
                queryVO.getSongName(),
                queryVO.getArtistName(),
                queryVO.getAlbum(),
                offset,
                pageSize
        );

        // 4. 精准查询收藏状态（核心：登录用户才查询，未登录默认0）
        Long userId = UserThreadLocal.getUserId();
        if (userId != null && !songVOList.isEmpty()) {
            // 提取所有歌曲ID
            List<Long> songIds = songVOList.stream()
                    .map(PlaylistSongItemVO::getSongId)
                    .toList();
            // 批量查询收藏状态（songId -> likeStatus）
            Map<Long, Integer> likeStatusMap = songDao.batchCheckSongLikeStatus(userId, songIds);
            // 给每首歌设置真实收藏状态
            songVOList.forEach(songVO -> {
                int likeStatus = likeStatusMap.getOrDefault(songVO.getSongId(), 0);
                songVO.setLikeStatus(likeStatus);
            });
        }

        // 5. 封装分页响应结果
        PageResultVO pageResult = new PageResultVO();
        pageResult.setTotal(total);
        pageResult.setItems(songVOList);

        return Result.success(pageResult);
    }

    @Override
    public Result<PageResult<AdminSongItemVO>> adminGetAllSongsByArtist(SongQueryParam param) {
        // 处理默认分页参数
        int pageNum = param.getPageNum() == null ? 1 : param.getPageNum();
        int pageSize = param.getPageSize() == null ? 10 : param.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 查询总数和分页列表（返回AdminSongItemVO）
        Long total = songDao.selectAdminSongTotal(
                param.getArtistId(),
                param.getSongName(),
                param.getAlbum()
        );
        List<AdminSongItemVO> songVOList = songDao.selectAdminSongPage(
                param.getArtistId(),
                param.getSongName(),
                param.getAlbum(),
                offset,
                pageSize
        );

        // 封装分页结果
        PageResult<AdminSongItemVO> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setItems(songVOList);
        return Result.success(pageResult, "操作成功");
    }

    @Override
    public Result<Void> addSong(Song song) {
        // 参数校验
        if (song.getArtistId() == null) {
            return Result.error("歌手ID不能为空");
        }
        if (song.getName() == null || song.getName().trim().isEmpty()) {
            return Result.error("歌曲名称不能为空");
        }
        if (song.getAlbum() == null || song.getAlbum().trim().isEmpty()) {
            return Result.error("专辑名称不能为空");
        }
        if (song.getStyle() == null || song.getStyle().trim().isEmpty()) {
            return Result.error("歌曲风格不能为空");
        }
        if (song.getReleaseTime() == null) {
            return Result.error("发行时间不能为空");
        }

        // 执行插入
        int rows = songDao.insert(song);
        return rows > 0 ? Result.success(null, "添加成功") : Result.error("添加失败");
    }

    @Override
    public Result<Void> updateSong(Song song) {
        // 参数校验
        if (song.getId() == null) {
            return Result.error("歌曲ID不能为空");
        }
        if (song.getArtistId() == null) {
            return Result.error("歌手ID不能为空");
        }
        if (song.getName() == null || song.getName().trim().isEmpty()) {
            return Result.error("歌曲名称不能为空");
        }
        if (song.getAlbum() == null || song.getAlbum().trim().isEmpty()) {
            return Result.error("专辑名称不能为空");
        }
        if (song.getStyle() == null || song.getStyle().trim().isEmpty()) {
            return Result.error("歌曲风格不能为空");
        }
        if (song.getReleaseTime() == null) {
            return Result.error("发行时间不能为空");
        }

        // 校验歌曲是否存在
        Song oldSong = songDao.selectById(song.getId());
        if (oldSong == null) {
            return Result.error("歌曲不存在");
        }

        // 执行更新
        int rows = songDao.update(song);
        return rows > 0 ? Result.success(null, "更新成功") : Result.error("更新失败");
    }

    @Override
    public Result<Void> deleteSong(Long songId) {
        // 参数校验
        if (songId == null) {
            return Result.error("歌曲ID不能为空");
        }

        // 校验歌曲是否存在
        Song song = songDao.selectById(songId);
        if (song == null) {
            return Result.error("歌曲不存在");
        }

        try {
            // 删除数据库记录
            int rows = songDao.deleteById(songId);
            if (rows <= 0) {
                return Result.error("删除失败");
            }

            // 删除封面文件（如果存在）
            if (song.getCoverUrl() != null && !song.getCoverUrl().isEmpty()) {
                MinioUtil.deleteFile(song.getCoverUrl());
            }

            // 删除音频文件（如果存在）
            if (song.getAudioUrl() != null && !song.getAudioUrl().isEmpty()) {
                MinioUtil.deleteFile(song.getAudioUrl());
            }

            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteSongs(List<Long> songIds) {
        // 参数校验
        if (songIds == null || songIds.isEmpty()) {
            return Result.error("歌曲ID列表不能为空");
        }

        try {
            // 查询所有要删除的歌曲（用于删除文件）
            List<Song> songs = new ArrayList<>();
            for (Long id : songIds) {
                Song song = songDao.selectById(id);
                if (song != null) {
                    songs.add(song);
                }
            }

            // 批量删除数据库记录
            int rows = songDao.deleteByIds(songIds);
            if (rows <= 0) {
                return Result.error("删除失败");
            }

            // 批量删除文件
            for (Song song : songs) {
                // 删除封面
                if (song.getCoverUrl() != null && !song.getCoverUrl().isEmpty()) {
                    MinioUtil.deleteFile(song.getCoverUrl());
                }
                // 删除音频
                if (song.getAudioUrl() != null && !song.getAudioUrl().isEmpty()) {
                    MinioUtil.deleteFile(song.getAudioUrl());
                }
            }

            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateSongCover(Long songId, Part coverPart) {
        // 参数校验
        if (songId == null) {
            return Result.error("歌曲ID不能为空");
        }
        if (coverPart == null || coverPart.getSize() <= 0) {
            return Result.error("封面文件不能为空");
        }

        // 校验歌曲是否存在
        Song song = songDao.selectById(songId);
        if (song == null) {
            return Result.error("歌曲不存在");
        }

        try {
            // 上传新封面
            String newCoverUrl = MinioUtil.uploadSongCover(coverPart);

            // 更新数据库
            int rows = songDao.updateCover(songId, newCoverUrl);
            if (rows <= 0) {
                // 回滚文件
                MinioUtil.deleteFile(newCoverUrl);
                return Result.error("更新失败");
            }

            // 删除旧封面
            if (song.getCoverUrl() != null && !song.getCoverUrl().isEmpty()) {
                MinioUtil.deleteFile(song.getCoverUrl());
            }

            return Result.success(null, "更新成功");
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateSongAudio(Long songId, Part audioPart, String duration) {
        // 参数校验
        if (songId == null) {
            return Result.error("歌曲ID不能为空");
        }
        if (audioPart == null || audioPart.getSize() <= 0) {
            return Result.error("音频文件不能为空");
        }
        if (duration == null || duration.trim().isEmpty()) {
            return Result.error("时长不能为空");
        }

        // 校验歌曲是否存在
        Song song = songDao.selectById(songId);
        if (song == null) {
            return Result.error("歌曲不存在");
        }

        try {
            // 上传新音频
            String newAudioUrl = MinioUtil.uploadSongAudio(audioPart);

            // 更新数据库
            int rows = songDao.updateAudio(songId, newAudioUrl, duration);
            if (rows <= 0) {
                // 回滚文件
                MinioUtil.deleteFile(newAudioUrl);
                return Result.error("更新失败");
            }

            // 删除旧音频
            if (song.getAudioUrl() != null && !song.getAudioUrl().isEmpty()) {
                MinioUtil.deleteFile(song.getAudioUrl());
            }

            return Result.success(null, "更新成功");
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    // 推荐歌曲数量（与示例返回20条一致）
    private static final int RECOMMEND_LIMIT = 20;

    // 新增：获取推荐歌曲核心逻辑
    @Override
    public Result<List<PlaylistSongItemVO>> getRecommendedSongs(HttpServletRequest request) {
        // 1. 获取登录用户ID
        Long userId = UserThreadLocal.getUserId();
        List<PlaylistSongItemVO> recommendedSongs = new ArrayList<>();

        if (userId == null) {
            // 2. 未登录：直接返回随机歌曲
            recommendedSongs = songDao.selectRandomSongs(RECOMMEND_LIMIT);
        } else {
            // 3. 已登录：基于用户收藏歌曲的风格推荐
            List<String> userStyles = songDao.selectStylesByFavoriteSongs(userId);

            if (userStyles.isEmpty()) {
                // 3.1 无收藏歌曲：返回随机歌曲
                recommendedSongs = songDao.selectRandomSongs(RECOMMEND_LIMIT);
            } else {
                // 3.2 按风格查询歌曲（排除已收藏）
                recommendedSongs = songDao.selectSongsByStyles(userStyles, userId, RECOMMEND_LIMIT);

                // 3.3 风格匹配的歌曲不足20条，补充随机歌曲
                if (recommendedSongs.size() < RECOMMEND_LIMIT) {
                    int needMore = RECOMMEND_LIMIT - recommendedSongs.size();
                    List<PlaylistSongItemVO> randomSongs = songDao.selectRandomSongs(needMore);

                    // 避免重复添加同一首歌
                    for (PlaylistSongItemVO randomSong : randomSongs) {
                        boolean isDuplicate = recommendedSongs.stream()
                                .anyMatch(s -> s.getSongId().equals(randomSong.getSongId()));
                        if (!isDuplicate) {
                            recommendedSongs.add(randomSong);
                            if (recommendedSongs.size() >= RECOMMEND_LIMIT) break;
                        }
                    }
                }
            }
        }

        // 确保返回数量不超过20条
        if (recommendedSongs.size() > RECOMMEND_LIMIT) {
            recommendedSongs = recommendedSongs.subList(0, RECOMMEND_LIMIT);
        }

        return Result.success(recommendedSongs, "操作成功");
    }
}
