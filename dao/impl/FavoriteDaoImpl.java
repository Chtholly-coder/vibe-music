package cn.edu.chtholly.dao.impl;

import cn.edu.chtholly.dao.FavoriteDao;
import cn.edu.chtholly.model.entity.UserFavorite;
import cn.edu.chtholly.model.param.FavoriteQueryParam;
import cn.edu.chtholly.model.vo.FavoritePlaylistItemVO;
import cn.edu.chtholly.model.vo.FavoriteSongItemVO;
import cn.edu.chtholly.util.JdbcUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDaoImpl implements FavoriteDao {

    @Override
    public UserFavorite selectByUserAndSong(Long userId, Long songId) {
        String sql = """
            SELECT id, user_id, song_id, type, create_time 
            FROM tb_user_favorite 
            WHERE user_id = ? AND song_id = ? AND type = 0
        """;
        return JdbcUtil.queryOne(sql, rs -> {
            try {
                return mapToUserFavorite(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, userId, songId);
    }

    @Override
    public int insert(UserFavorite favorite) {
        String sql = "INSERT INTO tb_user_favorite (user_id, song_id, type, create_time) VALUES (?, ?, 0, NOW())";
        return JdbcUtil.update(sql, favorite.getUserId(), favorite.getSongId());
    }

    @Override
    public int deleteByUserIdAndSongId(Long userId, Long songId) {
        String sql = "DELETE FROM tb_user_favorite WHERE user_id = ? AND song_id = ? AND type = 0";
        return JdbcUtil.update(sql, userId, songId);
    }

    // 带条件和分页的收藏列表查询
    @Override
    public List<FavoriteSongItemVO> selectFavoriteSongsByPage(Long userId, FavoriteQueryParam param) {
        // 强制修正分页参数
        int pageNum = param.getPageNum() == null ? 1 : Math.max(param.getPageNum(), 1);
        int pageSize = param.getPageSize() == null ? 10 : Math.max(Math.min(param.getPageSize(), 100), 1);
        int offset = (pageNum - 1) * pageSize;

        // 核心修正：严格匹配实体类对应的数据表字段
        StringBuilder sql = new StringBuilder("""
            SELECT 
                s.id AS songId,
                s.name AS songName,  -- 歌曲名：tb_song.name
                a.name AS artistName,  -- 艺术家名：tb_artist.name
                s.album AS album,  -- 专辑：tb_song.album
                s.duration AS duration,  -- 时长：tb_song.duration
                s.cover_url AS coverUrl,  -- 封面URL：tb_song.cover_url（对应实体类coverUrl）
                s.audio_url AS audioUrl,  -- 音频URL：tb_song.audio_url（对应实体类audioUrl）
                s.release_time AS releaseTime  -- 发行时间：tb_song.release（对应实体类releaseTime）
            FROM tb_user_favorite f
            JOIN tb_song s ON f.song_id = s.id  -- 收藏表关联歌曲表（f.song_id -> s.id）
            JOIN tb_artist a ON s.artist_id = a.id  -- 歌曲表关联艺术家表（s.artist_id -> a.id）
            WHERE f.user_id = ? AND f.type = 0  -- 只查用户的歌曲收藏（type=0）
        """);

        // 动态条件：严格匹配字段名
        List<Object> params = new ArrayList<>();
        params.add(userId);
        // 歌曲名模糊查询（tb_song.name）
        if (param.getSongName() != null && !param.getSongName().trim().isEmpty()) {
            sql.append(" AND s.name LIKE ?");
            params.add("%" + param.getSongName() + "%");
        }
        // 艺术家名模糊查询（tb_artist.name）
        if (param.getArtistName() != null && !param.getArtistName().trim().isEmpty()) {
            sql.append(" AND a.name LIKE ?");
            params.add("%" + param.getArtistName() + "%");
        }
        // 专辑名模糊查询（tb_song.album）
        if (param.getAlbum() != null && !param.getAlbum().trim().isEmpty()) {
            sql.append(" AND s.album LIKE ?");
            params.add("%" + param.getAlbum() + "%");
        }

        // 排序（按收藏时间倒序）+ 分页
        sql.append(" ORDER BY f.create_time DESC LIMIT ?, ?");  // 收藏表的create_time字段
        params.add(offset);
        params.add(pageSize);

        String finalSql = sql.toString();
        Object[] finalParams = params.toArray(new Object[0]);

        return JdbcUtil.queryList(finalSql, rs -> {
            try {
                return mapToFavoriteSongItemVO(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, finalParams);
    }

    // 带条件的收藏总数查询（同步修正）
    @Override
    public int countByCondition(Long userId, FavoriteQueryParam param) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) AS total
            FROM tb_user_favorite f
            JOIN tb_song s ON f.song_id = s.id
            JOIN tb_artist a ON s.artist_id = a.id
            WHERE f.user_id = ? AND f.type = 0
        """);

        List<Object> params = new ArrayList<>();
        params.add(userId);
        if (param.getSongName() != null && !param.getSongName().trim().isEmpty()) {
            sql.append(" AND s.name LIKE ?");
            params.add("%" + param.getSongName() + "%");
        }
        if (param.getArtistName() != null && !param.getArtistName().trim().isEmpty()) {
            sql.append(" AND a.name LIKE ?");
            params.add("%" + param.getArtistName() + "%");
        }
        if (param.getAlbum() != null && !param.getAlbum().trim().isEmpty()) {
            sql.append(" AND s.album LIKE ?");
            params.add("%" + param.getAlbum() + "%");
        }

        // 打印计数SQL（调试用）
        String finalSql = sql.toString();
        Object[] finalParams = params.toArray(new Object[0]);


        return JdbcUtil.queryOne(finalSql, rs -> {
            try {
                return rs.getInt("total");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, finalParams);
    }

    // 映射UserFavorite实体
    private UserFavorite mapToUserFavorite(ResultSet rs) throws SQLException {
        UserFavorite favorite = new UserFavorite();
        favorite.setId(rs.getLong("id"));
        favorite.setUserId(rs.getLong("user_id"));
        favorite.setSongId(rs.getLong("song_id"));
        favorite.setType(rs.getInt("type"));
        favorite.setCreateTime(rs.getTimestamp("create_time"));  // 收藏表的create_time字段
        return favorite;
    }

    // 映射FavoriteSongItemVO（严格匹配查询结果的别名）
    private FavoriteSongItemVO mapToFavoriteSongItemVO(ResultSet rs) throws SQLException {
        FavoriteSongItemVO item = new FavoriteSongItemVO();
        item.setSongId(rs.getLong("songId"));
        item.setSongName(rs.getString("songName"));  // 对应s.name AS songName
        item.setArtistName(rs.getString("artistName"));  // 对应a.name AS artistName
        item.setAlbum(rs.getString("album"));  // 对应s.album AS album
        item.setDuration(rs.getString("duration"));  // 对应s.duration AS duration
        item.setCoverUrl(rs.getString("coverUrl"));  // 对应s.cover_url AS coverUrl
        item.setAudioUrl(rs.getString("audioUrl"));  // 对应s.audio_url AS audioUrl
        item.setReleaseTime(rs.getString("releaseTime"));  // 对应s.release AS releaseTime
        item.setLikeStatus(1);  // 收藏状态为已收藏
        return item;
    }

    // 新增方法1：查询收藏歌单总数（支持筛选）
    @Override
    public Long selectFavoritePlaylistTotal(Long userId, String title, String style) {
        // 动态拼接SQL条件（模糊查询title，精准查询style）
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(DISTINCT p.id) AS total
            FROM tb_user_favorite uf
            JOIN tb_playlist p ON uf.playlist_id = p.id
            WHERE uf.user_id = ? AND uf.type = 1  -- type=1：歌单收藏
        """);

        // 标题筛选（模糊查询）
        if (title != null && !title.trim().isEmpty()) {
            sql.append(" AND p.title LIKE CONCAT('%', ?, '%')");
        }
        // 风格筛选（精准查询）
        if (style != null && !style.trim().isEmpty()) {
            sql.append(" AND p.style = ?");
        }

        // 构建参数数组
        int paramIndex = 0;
        Object[] params = new Object[3]; // 最多3个参数（userId, title, style）
        params[paramIndex++] = userId;
        if (title != null && !title.trim().isEmpty()) {
            params[paramIndex++] = title.trim();
        }
        if (style != null && !style.trim().isEmpty()) {
            params[paramIndex++] = style.trim();
        }

        // 执行查询（截取有效参数长度）
        Object[] validParams = new Object[paramIndex];
        System.arraycopy(params, 0, validParams, 0, paramIndex);

        return JdbcUtil.queryOne(sql.toString(), rs -> {
            try {
                return rs.getLong("total");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, validParams);
    }

    // 分页查询收藏歌单列表（支持筛选）
    @Override
    public List<FavoritePlaylistItemVO> selectFavoritePlaylistPage(
            Long userId, String title, String style, int offset, int pageSize) {
        // 动态拼接SQL条件
        StringBuilder sql = new StringBuilder("""
            SELECT 
                p.id AS playlistId,
                p.title AS title,
                p.cover_url AS coverUrl
            FROM tb_user_favorite uf
            JOIN tb_playlist p ON uf.playlist_id = p.id
            WHERE uf.user_id = ? AND uf.type = 1  -- type=1：歌单收藏
        """);

        // 标题筛选（模糊查询）
        if (title != null && !title.trim().isEmpty()) {
            sql.append(" AND p.title LIKE CONCAT('%', ?, '%')");
        }
        // 风格筛选（精准查询）
        if (style != null && !style.trim().isEmpty()) {
            sql.append(" AND p.style = ?");
        }

        // 分页+排序（按收藏时间倒序，最新收藏在前）
        sql.append(" ORDER BY uf.create_time DESC LIMIT ?, ?");

        // 构建参数数组
        int paramIndex = 0;
        Object[] params = new Object[5]; // 最多5个参数（userId, title, style, offset, pageSize）
        params[paramIndex++] = userId;
        if (title != null && !title.trim().isEmpty()) {
            params[paramIndex++] = title.trim();
        }
        if (style != null && !style.trim().isEmpty()) {
            params[paramIndex++] = style.trim();
        }
        params[paramIndex++] = offset;
        params[paramIndex++] = pageSize;

        // 截取有效参数长度
        Object[] validParams = new Object[paramIndex];
        System.arraycopy(params, 0, validParams, 0, paramIndex);

        // 执行查询并映射为VO
        return JdbcUtil.queryList(sql.toString(), rs -> {
            FavoritePlaylistItemVO vo = new FavoritePlaylistItemVO();
            try {
                vo.setPlaylistId(rs.getLong("playlistId"));
                vo.setTitle(rs.getString("title"));
                vo.setCoverUrl(rs.getString("coverUrl"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return vo;
        }, validParams);
    }

    @Override
    public UserFavorite selectByUserAndPlaylist(Long userId, Long playlistId) {
        String sql = """
            SELECT id, user_id, playlist_id, type, create_time 
            FROM tb_user_favorite 
            WHERE user_id = ? AND playlist_id = ? AND type = 1
        """;
        return JdbcUtil.queryOne(sql, rs -> {
            try {
                return mapToUserFavorite(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, userId, playlistId);
    }

    @Override
    public int insertPlaylist(UserFavorite favorite) {
        String sql = "INSERT INTO tb_user_favorite (user_id, playlist_id, type, create_time) VALUES (?, ?, 1, NOW())";
        return JdbcUtil.update(sql, favorite.getUserId(), favorite.getPlaylistId());
    }

    @Override
    public int deleteByUserIdAndPlaylistId(Long userId, Long playlistId) {
        String sql = "DELETE FROM tb_user_favorite WHERE user_id = ? AND playlist_id = ? AND type = 1";
        return JdbcUtil.update(sql, userId, playlistId);
    }
}