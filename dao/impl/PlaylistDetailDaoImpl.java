package cn.edu.chtholly.dao.impl;

import cn.edu.chtholly.dao.PlaylistDetailDao;
import cn.edu.chtholly.model.entity.Playlist;
import cn.edu.chtholly.model.entity.Style;
import cn.edu.chtholly.model.vo.PlaylistSongItemVO;
import cn.edu.chtholly.util.JdbcUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PlaylistDetailDaoImpl implements PlaylistDetailDao {

    // 1. 查询歌单基本信息
    @Override
    public Playlist selectPlaylistById(Long playlistId) {
        String sql = """
            SELECT id, title, cover_url, introduction,style 
            FROM tb_playlist 
            WHERE id = ?
        """;
        return JdbcUtil.queryOne(sql, rs -> {
            try {
                return mapToPlaylist(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, playlistId);
    }

    // 2. 根据歌单style查询风格表（验证风格是否存在）
    @Override
    public Style selectStyleByName(String styleName) {
        String sql = """
            SELECT id, name 
            FROM tb_style 
            WHERE name = ?
        """;
        return JdbcUtil.queryOne(sql, rs -> {
            try {
                return mapToStyle(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, styleName);
    }

    // 3. 核心：查询所有style包含目标风格的歌曲（用FIND_IN_SET适配逗号分割的字符串）
    @Override
    public List<PlaylistSongItemVO> selectSongsByStyle(String styleName) {
        // FIND_IN_SET(目标风格, 歌曲style字段)：判断风格是否在逗号分割的字符串中
        String sql = """
            SELECT 
                s.id AS songId,
                s.name AS songName,
                a.name AS artistName,
                s.album AS album,
                s.duration AS duration,
                s.cover_url AS coverUrl,
                s.audio_url AS audioUrl,
                s.release_time AS releaseTime
            FROM tb_song s
            JOIN tb_artist a ON s.artist_id = a.id  -- 歌曲关联艺术家
            WHERE FIND_IN_SET(?, s.style) > 0  -- 适配歌曲style逗号分割格式
            ORDER BY s.id ASC
        """;
        return JdbcUtil.queryList(sql, rs -> {
            try {
                return mapToSongItemVO(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, styleName);
    }

    // 4. 检查歌单收藏状态
    @Override
    public Integer checkPlaylistCollectStatus(Long userId, Long playlistId) {
        String sql = """
            SELECT COUNT(*) AS count 
            FROM tb_user_favorite 
            WHERE user_id = ? AND playlist_id = ? AND type = 1
        """;
        return JdbcUtil.queryOne(sql, rs -> {
            try {
                return rs.getInt("count") > 0 ? 1 : 0; // 1-已收藏，0-未收藏
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, userId, playlistId);
    }

    // 映射Playlist实体
    private Playlist mapToPlaylist(ResultSet rs) throws SQLException {
        Playlist playlist = new Playlist();
        playlist.setId(rs.getLong("id"));
        playlist.setTitle(rs.getString("title"));
        playlist.setCoverUrl(rs.getString("cover_url"));
        playlist.setIntroduction(rs.getString("introduction"));
        playlist.setStyle(rs.getString("style"));
        return playlist;
    }

    // 映射Style实体
    private Style mapToStyle(ResultSet rs) throws SQLException {
        Style style = new Style();
        style.setId(rs.getLong("id"));
        style.setName(rs.getString("name"));
        return style;
    }

    // 映射歌曲VO
    private PlaylistSongItemVO mapToSongItemVO(ResultSet rs) throws SQLException {
        PlaylistSongItemVO item = new PlaylistSongItemVO();
        item.setSongId(rs.getLong("songId"));
        item.setSongName(rs.getString("songName"));
        item.setArtistName(rs.getString("artistName"));
        item.setAlbum(rs.getString("album"));
        item.setDuration(rs.getString("duration"));
        item.setCoverUrl(rs.getString("coverUrl"));
        item.setAudioUrl(rs.getString("audioUrl"));
        item.setReleaseTime(rs.getString("releaseTime"));
        item.setLikeStatus(0); // 默认未收藏
        return item;
    }
    // 批量查询歌曲收藏状态（修正后，适配Function接口）
    @Override
    public Map<Long, Integer> batchCheckSongCollectStatus(Long userId, List<Long> songIds) {
        if (userId == null || songIds.isEmpty()) {
            return Map.of();
        }

        StringBuilder sql = new StringBuilder("""
        SELECT song_id, IF(COUNT(*) > 0, 1, 0) AS likeStatus 
        FROM tb_user_favorite 
        WHERE user_id = ? 
          AND type = 0  -- type=0 表示歌曲收藏
          AND song_id IN (""");
        sql.append(String.join(",", songIds.stream().map(id -> "?").toArray(String[]::new)));
        sql.append(") GROUP BY song_id");

        // 关键修改：用 Function<ResultSet, Long> 和 Function<ResultSet, Integer> 替代 ResultSetMapper
        return JdbcUtil.queryMap(
                sql.toString(),
                rs -> {
                    try {
                        return rs.getLong("song_id");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },  // keyMapper：提取songId作为key
                rs -> {
                    try {
                        return rs.getInt("likeStatus");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },// valueMapper：提取likeStatus作为value
                getParamsArray(userId, songIds)
        );
    }

    // 辅助方法：构建SQL参数数组（不变）
    private Object[] getParamsArray(Long userId, List<Long> songIds) {
        Object[] params = new Object[songIds.size() + 1];
        params[0] = userId;
        for (int i = 0; i < songIds.size(); i++) {
            params[i + 1] = songIds.get(i);
        }
        return params;
    }
}