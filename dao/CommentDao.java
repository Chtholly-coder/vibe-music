package cn.edu.chtholly.dao;

import cn.edu.chtholly.model.entity.Comment;
import java.util.List;
import java.util.Map;

public interface CommentDao {
    // 查询歌曲的评论
    List<Comment> selectBySongId(Long songId);

    List<Map<String, Object>> selectCommentWithUserBySongId(Long songId);

    List<Map<String, Object>> selectCommentWithUserByPlaylistId(Long playlistId);

    int insertComment(Comment comment); // 合并歌曲/歌单评论插入（通用方法）
    int deleteCommentByIdAndUserId(Long commentId, Long userId);
    int incrementLikeCount(Long commentId);
    boolean existsCommentById(Long commentId);
    boolean isCommentOwner(Long commentId, Long userId);
    boolean existsSongById(Long songId);
    boolean existsPlaylistById(Long playlistId);
}