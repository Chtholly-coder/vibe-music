package cn.edu.chtholly.service.impl;

import cn.edu.chtholly.dao.CommentDao;
import cn.edu.chtholly.dao.PlaylistDetailDao;
import cn.edu.chtholly.dao.impl.CommentDaoImpl;
import cn.edu.chtholly.dao.impl.PlaylistDetailDaoImpl;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Comment;
import cn.edu.chtholly.model.entity.Playlist;
import cn.edu.chtholly.model.entity.Style;
import cn.edu.chtholly.model.vo.CommentVO;
import cn.edu.chtholly.model.vo.PlaylistDetailVO;
import cn.edu.chtholly.model.vo.PlaylistSongItemVO;
import cn.edu.chtholly.service.PlaylistDetailService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaylistDetailServiceImpl implements PlaylistDetailService {

    private final PlaylistDetailDao playlistDetailDao = new PlaylistDetailDaoImpl();
    private final CommentDao commentDao = new CommentDaoImpl();

    @Override
    public Result<PlaylistDetailVO> getPlaylistDetail(Long playlistId, Long userId) {
        // 1. 查询歌单基本信息
        Playlist playlist = playlistDetailDao.selectPlaylistById(playlistId);
        if (playlist == null) {
            return Result.error("歌单不存在");
        }

        // 2. 验证风格并查询歌曲
        String playlistStyle = playlist.getStyle();
        Style style = playlistDetailDao.selectStyleByName(playlistStyle);
        if (style == null) {
            return Result.error("歌单风格不存在");
        }
        List<PlaylistSongItemVO> songs = playlistDetailDao.selectSongsByStyle(style.getName());


        // 3. 批量查询歌曲收藏状态（不变）
        if (!songs.isEmpty() && userId != null) {
            List<Long> songIds = songs.stream().map(PlaylistSongItemVO::getSongId).toList();
            Map<Long, Integer> songLikeStatusMap = playlistDetailDao.batchCheckSongCollectStatus(userId, songIds);
            songs.forEach(song -> song.setLikeStatus(songLikeStatusMap.getOrDefault(song.getSongId(), 0)));
        }

        // 4. 查询歌单收藏状态（不变）
        Integer playlistLikeStatus = 0;
        if (userId != null) {
            playlistLikeStatus = playlistDetailDao.checkPlaylistCollectStatus(userId, playlistId);
        }

        // 5. 查询歌单评论并转换为CommentVO
        List<Map<String, Object>> commentWithUserList = commentDao.selectCommentWithUserByPlaylistId(playlistId);
        List<CommentVO> commentVOList = commentWithUserList.stream()
                .map(map -> {
                    Comment comment = new Comment();
                    comment.setId((Long) map.get("commentId"));
                    comment.setContent((String) map.get("content"));
                    comment.setCreateTime((java.sql.Timestamp) map.get("createTime"));
                    comment.setLikeCount((Long) map.get("likeCount"));
                    // 调用CommentVO.fromEntity转换（复用歌曲评论的VO逻辑）
                    return CommentVO.fromEntity(
                            comment,
                            (String) map.get("username"),
                            (String) map.get("userAvatar")
                    );
                })
                .collect(Collectors.toList());

        // 6. 封装响应VO（
        PlaylistDetailVO detailVO = new PlaylistDetailVO();
        detailVO.setPlaylistId(playlist.getId());
        detailVO.setTitle(playlist.getTitle());
        detailVO.setCoverUrl(playlist.getCoverUrl());
        detailVO.setIntroduction(playlist.getIntroduction());
        detailVO.setSongs(songs);
        detailVO.setLikeStatus(playlistLikeStatus);
        detailVO.setComments(commentVOList.toArray(new CommentVO[0])); // 转换为数组，匹配VO字段类型

        return Result.success(detailVO);
    }
}