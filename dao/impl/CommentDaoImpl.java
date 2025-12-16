package cn.edu.chtholly.dao.impl;

import cn.edu.chtholly.dao.CommentDao;
import cn.edu.chtholly.model.entity.Comment;
import cn.edu.chtholly.util.JdbcUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.sql.Timestamp;
import java.util.Date;

public class CommentDaoImpl implements CommentDao {

    // 原有方法：仅查询评论基础信息（不变）
    @Override
    public List<Comment> selectBySongId(Long songId) {
        String sql = """
            SELECT id, user_id, song_id, content, create_time, like_count 
            FROM tb_comment 
            WHERE song_id = ? AND type = 0 
            ORDER BY create_time DESC
        """;

        return JdbcUtil.queryList(sql, rs -> {
            try {
                return mapToComment(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, songId);
    }

    // 修复：补全「评论+用户信息」查询逻辑（核心）
    @Override
    public List<Map<String, Object>> selectCommentWithUserBySongId(Long songId) {
        // SQL：关联tb_comment和tb_user，查询评论信息+用户名+头像
        String sql = """
            SELECT 
                c.id AS commentId,
                c.content AS content,
                c.create_time AS createTime,
                c.like_count AS likeCount,
                u.username AS username,  -- 用户名（来自tb_user）
                u.user_avatar AS userAvatar  -- 用户头像（来自tb_user）
            FROM tb_comment c
            LEFT JOIN tb_user u ON c.user_id = u.id  -- 左连接：即使用户不存在也显示评论
            WHERE c.song_id = ?  -- 按歌曲ID筛选
              AND c.type = 0  -- 0=歌曲评论（确认type字段含义，若不同需修改）
            ORDER BY c.create_time DESC  -- 最新评论在前
        """;

        // 执行查询并映射结果为Map（包含评论和用户信息）
        return JdbcUtil.queryList(sql, rs -> {
            try {
                Map<String, Object> commentMap = new HashMap<>();
                // 评论信息
                commentMap.put("commentId", rs.getLong("commentId"));
                commentMap.put("content", rs.getString("content"));
                commentMap.put("createTime", rs.getTimestamp("createTime"));
                commentMap.put("likeCount", rs.getLong("likeCount"));
                // 用户信息（左连接可能为null，需兼容）
                commentMap.put("username", rs.getString("username") != null ? rs.getString("username") : "匿名用户");
                commentMap.put("userAvatar", rs.getString("userAvatar") != null ? rs.getString("userAvatar") : "");
                return commentMap;
            } catch (SQLException e) {
                throw new RuntimeException("评论查询失败：" + e.getMessage(), e);
            }
        }, songId); // 传入歌曲ID参数
    }

    // 新增方法：查询歌单评论+用户信息（核心）
    @Override
    public List<Map<String, Object>> selectCommentWithUserByPlaylistId(Long playlistId) {
        String sql = """
            SELECT 
                c.id AS commentId,
                c.content AS content,
                c.create_time AS createTime,
                c.like_count AS likeCount,
                u.username AS username,
                u.user_avatar AS userAvatar
            FROM tb_comment c
            LEFT JOIN tb_user u ON c.user_id = u.id  -- 关联用户表，获取用户名和头像
            WHERE c.playlist_id = ?  -- 按歌单ID筛选
              AND c.type = 1  -- type=1 表示歌单评论（与歌曲评论区分）
            ORDER BY c.create_time DESC  -- 最新评论在前
        """;
        return JdbcUtil.queryList(sql, rs -> {
            try {
                return buildCommentMap(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, playlistId);
    }

    // 复用：构建评论+用户信息的Map（避免重复代码）
    private Map<String, Object> buildCommentMap(ResultSet rs) throws SQLException {
        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("commentId", rs.getLong("commentId"));
        commentMap.put("content", rs.getString("content") != null ? rs.getString("content") : "");
        commentMap.put("createTime", rs.getTimestamp("createTime"));
        commentMap.put("likeCount", rs.getLong("likeCount") != 0 ? rs.getLong("likeCount") : 0);
        commentMap.put("username", rs.getString("username") != null ? rs.getString("username") : "匿名用户");
        commentMap.put("userAvatar", rs.getString("userAvatar") != null ? rs.getString("userAvatar") : "");
        return commentMap;
    }

    @Override
    public int insertComment(Comment comment) {
        // 通用插入方法
        String sql = """
            INSERT INTO tb_comment (
                user_id, song_id, playlist_id, content, create_time, type, like_count
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        Date now = new Date();
        return JdbcUtil.update(sql,
                comment.getUserId(),
                comment.getSongId(),       // 歌曲ID（歌曲评论非空，歌单评论为null）
                comment.getPlaylistId(),   // 歌单ID（对应数据表play_list字段，歌单评论非空）
                comment.getContent(),
                new Timestamp(now.getTime()),
                comment.getType(),         // 0=歌曲，1=歌单
                0                          // 初始点赞数
        );
    }

    // 原有方法
    @Override
    public int deleteCommentByIdAndUserId(Long commentId, Long userId) {
        String sql = "DELETE FROM tb_comment WHERE id = ? AND user_id = ?";
        return JdbcUtil.update(sql, commentId, userId);
    }

    @Override
    public int incrementLikeCount(Long commentId) {
        String sql = "UPDATE tb_comment SET like_count = like_count + 1 WHERE id = ?";
        return JdbcUtil.update(sql, commentId);
    }

    @Override
    public boolean existsCommentById(Long commentId) {
        String sql = "SELECT COUNT(*) AS count FROM tb_comment WHERE id = ?";
        Integer count = JdbcUtil.queryOne(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, commentId);
        return count != null && count > 0;
    }

    @Override
    public boolean isCommentOwner(Long commentId, Long userId) {
        String sql = "SELECT COUNT(*) AS count FROM tb_comment WHERE id = ? AND user_id = ?";
        Integer count = JdbcUtil.queryOne(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, commentId, userId);
        return count != null && count > 0;
    }

    // 新增：校验歌曲是否存在（查询tb_song）
    @Override
    public boolean existsSongById(Long songId) {
        String sql = "SELECT COUNT(*) AS count FROM tb_song WHERE id = ?";
        Integer count = JdbcUtil.queryOne(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, songId);
        return count != null && count > 0;
    }

    // 新增：校验歌单是否存在（查询tb_playlist）
    @Override
    public boolean existsPlaylistById(Long playlistId) {
        String sql = "SELECT COUNT(*) AS count FROM tb_playlist WHERE id = ?";
        Integer count = JdbcUtil.queryOne(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, playlistId);
        return count != null && count > 0;
    }

    // 映射方法
    private Comment mapToComment(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getLong("id"));
        comment.setUserId(rs.getLong("user_id"));
        comment.setSongId(rs.getLong("song_id"));
        comment.setContent(rs.getString("content"));
        comment.setCreateTime(rs.getTimestamp("create_time"));
        comment.setLikeCount(rs.getLong("like_count"));
        return comment;
    }
}