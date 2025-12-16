package cn.edu.chtholly.dao.impl;

import cn.edu.chtholly.dao.SongDao;
import cn.edu.chtholly.model.entity.Song;
import cn.edu.chtholly.model.vo.AdminSongItemVO;
import cn.edu.chtholly.model.vo.ArtistSongItemVO;
import cn.edu.chtholly.model.vo.PlaylistSongItemVO;
import cn.edu.chtholly.util.JdbcUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.*;

public class SongDaoImpl implements SongDao {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Song selectById(Long id) {
        String sql = """
            SELECT id, artist_id, name, album, lyric, duration, 
                   cover_url, audio_url, release_time 
            FROM tb_song 
            WHERE id = ?
        """;

        return JdbcUtil.queryOne(sql, rs -> {
            try {
                return mapToSong(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, id);
    }

    @Override
    public List<ArtistSongItemVO> selectByArtistId(Long artistId) {
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
            JOIN tb_artist a ON s.artist_id = a.id
            WHERE s.artist_id = ?
            ORDER BY s.id DESC
        """;
        return JdbcUtil.queryList(sql, rs -> {
            try {
                ArtistSongItemVO vo = new ArtistSongItemVO();
                vo.setSongId(rs.getLong("songId"));
                vo.setSongName(rs.getString("songName"));
                vo.setArtistName(rs.getString("artistName"));
                vo.setAlbum(rs.getString("album"));
                vo.setDuration(rs.getString("duration"));
                vo.setCoverUrl(rs.getString("coverUrl"));
                vo.setAudioUrl(rs.getString("audioUrl"));
                vo.setReleaseTime(rs.getDate("releaseTime") != null ? sdf.format(rs.getDate("releaseTime")) : null);
                vo.setLikeStatus(0); // 初始默认未收藏，后续Service层覆盖
                return vo;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, artistId);
    }

    // 结果集映射为Song实体
    private Song mapToSong(ResultSet rs) throws SQLException {
        Song song = new Song();
        song.setId(rs.getLong("id"));
        song.setArtistId(rs.getLong("artist_id"));
        song.setName(rs.getString("name"));
        song.setAlbum(rs.getString("album"));
        song.setLyric(rs.getString("lyric"));
        song.setDuration(rs.getString("duration"));
        song.setCoverUrl(rs.getString("cover_url"));
        song.setAudioUrl(rs.getString("audio_url"));
        song.setReleaseTime(rs.getDate("release_time"));
        return song;
    }

    // 分页+多条件查询歌曲列表（核心）
    @Override
    public List<PlaylistSongItemVO> selectSongPage(
            String songName, String artistName, String album,
            Integer offset, Integer pageSize) {

        // 动态拼接SQL条件（模糊查询）
        StringBuilder sql = new StringBuilder("""
            SELECT 
                s.id AS songId,
                s.name AS songName,
                a.name AS artistName,
                s.album AS album,
                s.duration AS duration,
                s.cover_url AS coverUrl,
                s.audio_url AS audioUrl,
                DATE_FORMAT(s.release_time, '%Y-%m-%d') AS releaseTime  -- 格式化日期
            FROM tb_song s
            JOIN tb_artist a ON s.artist_id = a.id  -- 关联艺术家表查名称
            WHERE 1=1  -- 占位符，方便拼接条件
        """);

        // 拼接模糊查询条件（空字符串不拼接）
        if (songName != null && !songName.trim().isEmpty()) {
            sql.append(" AND s.name LIKE CONCAT('%', ?, '%')");
        }
        if (artistName != null && !artistName.trim().isEmpty()) {
            sql.append(" AND a.name LIKE CONCAT('%', ?, '%')");
        }
        if (album != null && !album.trim().isEmpty()) {
            sql.append(" AND s.album LIKE CONCAT('%', ?, '%')");
        }

        // 分页+排序
        sql.append(" ORDER BY s.id ASC LIMIT ?, ?");

        // 构建参数数组（按条件顺序拼接）
        List<Object> params = new java.util.ArrayList<>();
        if (songName != null && !songName.trim().isEmpty()) {
            params.add(songName.trim());
        }
        if (artistName != null && !artistName.trim().isEmpty()) {
            params.add(artistName.trim());
        }
        if (album != null && !album.trim().isEmpty()) {
            params.add(album.trim());
        }
        params.add(offset); // 分页偏移量
        params.add(pageSize); // 每页条数

        // 执行查询并映射VO
        return JdbcUtil.queryList(sql.toString(), rs -> {
            try {
                PlaylistSongItemVO item = new PlaylistSongItemVO();
                item.setSongId(rs.getLong("songId"));
                item.setSongName(rs.getString("songName"));
                item.setArtistName(rs.getString("artistName"));
                item.setAlbum(rs.getString("album"));
                item.setDuration(rs.getString("duration"));
                item.setCoverUrl(rs.getString("coverUrl"));
                item.setAudioUrl(rs.getString("audioUrl"));
                item.setReleaseTime(rs.getString("releaseTime"));
                item.setLikeStatus(0); // 临时默认值，后续替换为真实状态
                return item;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, params.toArray());
    }

    // 查询符合条件的总条数
    @Override
    public Long selectSongTotal(String songName, String artistName, String album) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) AS total
            FROM tb_song s
            JOIN tb_artist a ON s.artist_id = a.id
            WHERE 1=1
        """);

        List<Object> params = new java.util.ArrayList<>();
        if (songName != null && !songName.trim().isEmpty()) {
            sql.append(" AND s.name LIKE CONCAT('%', ?, '%')");
            params.add(songName.trim());
        }
        if (artistName != null && !artistName.trim().isEmpty()) {
            sql.append(" AND a.name LIKE CONCAT('%', ?, '%')");
            params.add(artistName.trim());
        }
        if (album != null && !album.trim().isEmpty()) {
            sql.append(" AND s.album LIKE CONCAT('%', ?, '%')");
            params.add(album.trim());
        }

        return JdbcUtil.queryOne(sql.toString(), rs -> {
            try {
                return rs.getLong("total");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, params.toArray());
    }

    // 批量查询歌曲收藏状态（精准匹配收藏逻辑）
    @Override
    public Map<Long, Integer> batchCheckSongLikeStatus(Long userId, List<Long> songIds) {
        if (userId == null || songIds.isEmpty()) {
            return Map.of();
        }

        // 构建IN条件（适配歌曲ID列表）
        StringBuilder sql = new StringBuilder("""
            SELECT song_id, IF(COUNT(*) > 0, 1, 0) AS likeStatus
            FROM tb_user_favorite
            WHERE user_id = ?
              AND type = 0  -- 0=歌曲收藏
              AND song_id IN (""");
        sql.append(String.join(",", songIds.stream().map(id -> "?").toArray(String[]::new)));
        sql.append(") GROUP BY song_id");

        // 构建参数数组（userId + 歌曲ID列表）
        Object[] params = new Object[songIds.size() + 1];
        params[0] = userId;
        for (int i = 0; i < songIds.size(); i++) {
            params[i + 1] = songIds.get(i);
        }

        // 执行查询，返回 songId -> likeStatus 映射
        return JdbcUtil.queryMap(
                sql.toString(),
                rs -> {
                    try {
                        return rs.getLong("song_id");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                rs -> {
                    try {
                        return rs.getInt("likeStatus");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                params
        );
    }

    // 新增：管理员专用分页查询（带歌手ID筛选）
    @Override
    public List<AdminSongItemVO> selectAdminSongPage(Long artistId, String songName, String album, Integer offset, Integer pageSize) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                s.id AS songId,
                a.name AS artistName,
                s.name AS songName,
                s.album AS album,
                s.lyric AS lyric,
                s.duration AS duration,
                s.style AS style,
                s.cover_url AS coverUrl,
                s.audio_url AS audioUrl,
                DATE_FORMAT(s.release_time, '%Y-%m-%d') AS releaseTime
            FROM tb_song s
            JOIN tb_artist a ON s.artist_id = a.id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        // 歌手ID筛选（精确匹配）
        if (artistId != null) {
            sql.append(" AND s.artist_id = ?");
            params.add(artistId);
        }

        // 歌曲名模糊筛选
        if (songName != null && !songName.trim().isEmpty()) {
            sql.append(" AND s.name LIKE CONCAT('%', ?, '%')");
            params.add(songName.trim());
        }

        // 专辑名模糊筛选
        if (album != null && !album.trim().isEmpty()) {
            sql.append(" AND s.album LIKE CONCAT('%', ?, '%')");
            params.add(album.trim());
        }

        // 排序与分页
        sql.append(" ORDER BY s.id DESC LIMIT ?, ?");
        params.add(offset);
        params.add(pageSize);

        return JdbcUtil.queryList(sql.toString(), rs -> {
            try {
                AdminSongItemVO item = new AdminSongItemVO();
                item.setSongId(rs.getLong("songId"));
                item.setArtistName(rs.getString("artistName"));
                item.setSongName(rs.getString("songName"));
                item.setAlbum(rs.getString("album"));
                item.setLyric(rs.getString("lyric"));
                item.setDuration(rs.getString("duration"));
                item.setStyle(rs.getString("style"));
                item.setCoverUrl(rs.getString("coverUrl"));
                item.setAudioUrl(rs.getString("audioUrl"));
                item.setReleaseTime(rs.getString("releaseTime"));
                return item;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, params.toArray());
    }

    // 新增：查询符合条件的歌曲总数（带歌手ID筛选）
    @Override
    public Long selectAdminSongTotal(Long artistId, String songName, String album) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) AS total
            FROM tb_song s
            JOIN tb_artist a ON s.artist_id = a.id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        // 歌手ID筛选
        if (artistId != null) {
            sql.append(" AND s.artist_id = ?");
            params.add(artistId);
        }

        // 歌曲名模糊筛选
        if (songName != null && !songName.trim().isEmpty()) {
            sql.append(" AND s.name LIKE CONCAT('%', ?, '%')");
            params.add(songName.trim());
        }

        // 专辑名模糊筛选
        if (album != null && !album.trim().isEmpty()) {
            sql.append(" AND s.album LIKE CONCAT('%', ?, '%')");
            params.add(album.trim());
        }

        return JdbcUtil.queryOne(sql.toString(), rs -> {
            try {
                return rs.getLong("total");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, params.toArray());
    }

    // 新增：插入歌曲
    @Override
    public int insert(Song song) {
        String sql = """
            INSERT INTO tb_song (artist_id, name, album, style, release_time)
            VALUES (?, ?, ?, ?, ?)
        """;
        return JdbcUtil.update(sql,
                song.getArtistId(),
                song.getName(),
                song.getAlbum(),
                song.getStyle(),
                song.getReleaseTime()
        );
    }

    // 新增：更新歌曲信息
    @Override
    public int update(Song song) {
        String sql = """
            UPDATE tb_song
            SET artist_id = ?, name = ?, album = ?, style = ?, release_time = ?
            WHERE id = ?
        """;
        return JdbcUtil.update(sql,
                song.getArtistId(),
                song.getName(),
                song.getAlbum(),
                song.getStyle(),
                song.getReleaseTime(),
                song.getId()
        );
    }

    // 新增：更新歌曲封面
    @Override
    public int updateCover(Long id, String coverUrl) {
        String sql = "UPDATE tb_song SET cover_url = ? WHERE id = ?";
        return JdbcUtil.update(sql, coverUrl, id);
    }

    // 新增：更新歌曲音频和时长
    @Override
    public int updateAudio(Long id, String audioUrl, String duration) {
        String sql = "UPDATE tb_song SET audio_url = ?, duration = ? WHERE id = ?";
        return JdbcUtil.update(sql, audioUrl, duration, id);
    }

    // 新增：根据id删除歌曲
    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM tb_song WHERE id = ?";
        return JdbcUtil.update(sql, id);
    }

    // 新增：批量删除歌曲
    @Override
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("DELETE FROM tb_song WHERE id IN (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            sql.append("?");
            if (i < ids.size() - 1) {
                sql.append(",");
            }
            params.add(ids.get(i));
        }
        sql.append(")");

        return JdbcUtil.update(sql.toString(), params.toArray());
    }

    // 新增：查询随机歌曲（返回指定数量的随机歌曲）
    @Override
    public List<PlaylistSongItemVO> selectRandomSongs(int limit) {
        String sql = """
        SELECT 
            s.id AS songId,
            s.name AS songName,
            a.name AS artistName,
            s.album AS album,
            s.duration AS duration,
            s.cover_url AS coverUrl,
            s.audio_url AS audioUrl,
            DATE_FORMAT(s.release_time, '%Y-%m-%d') AS releaseTime
        FROM tb_song s
        JOIN tb_artist a ON s.artist_id = a.id
        ORDER BY RAND()  -- MySQL随机排序
        LIMIT ?
    """;

        return JdbcUtil.queryList(sql, rs -> {
            try {
                PlaylistSongItemVO item = new PlaylistSongItemVO();
                item.setSongId(rs.getLong("songId"));
                item.setSongName(rs.getString("songName"));
                item.setArtistName(rs.getString("artistName"));
                item.setAlbum(rs.getString("album"));
                item.setDuration(rs.getString("duration"));
                item.setCoverUrl(rs.getString("coverUrl"));
                item.setAudioUrl(rs.getString("audioUrl"));
                item.setReleaseTime(rs.getString("releaseTime"));
                item.setLikeStatus(null); // 推荐歌曲收藏状态为null（匹配示例）
                return item;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, limit);
    }

    // 获取用户收藏歌曲的所有风格（拆分逗号、去重）
    @Override
    public List<String> selectStylesByFavoriteSongs(Long userId) {
        // 1. 查询用户收藏歌曲的所有style字符串
        String sql = """
        SELECT DISTINCT s.style
        FROM tb_user_favorite uf
        JOIN tb_song s ON uf.song_id = s.id
        WHERE uf.user_id = ? AND uf.type = 0  -- type=0：歌曲收藏
          AND s.style IS NOT NULL AND s.style != ''
    """;

        List<String> styleStrings = JdbcUtil.queryList(sql, rs -> {
            try {
                return rs.getString("style");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, userId);

        // 2. 拆分逗号分隔的风格，去重
        Set<String> styleSet = new HashSet<>();
        for (String styleStr : styleStrings) {
            String[] styles = styleStr.split(",");
            for (String style : styles) {
                String trimedStyle = style.trim();
                if (!trimedStyle.isEmpty()) {
                    styleSet.add(trimedStyle);
                }
            }
        }

        return new ArrayList<>(styleSet);
    }

    // 根据风格列表查询歌曲（排除用户已收藏的）
    @Override
    public List<PlaylistSongItemVO> selectSongsByStyles(List<String> styles, Long excludeUserId, int limit) {
        if (styles.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建风格匹配条件（MySQL正则匹配：风格1|风格2|...）
        StringBuilder styleRegex = new StringBuilder();
        for (int i = 0; i < styles.size(); i++) {
            if (i > 0) styleRegex.append("|");
            styleRegex.append(styles.get(i));
        }

        // SQL：查询指定风格的歌曲，排除用户已收藏的
        String sql = """
        SELECT 
            s.id AS songId,
            s.name AS songName,
            a.name AS artistName,
            s.album AS album,
            s.duration AS duration,
            s.cover_url AS coverUrl,
            s.audio_url AS audioUrl,
            DATE_FORMAT(s.release_time, '%Y-%m-%d') AS releaseTime
        FROM tb_song s
        JOIN tb_artist a ON s.artist_id = a.id
        LEFT JOIN tb_user_favorite uf 
            ON s.id = uf.song_id AND uf.user_id = ? AND uf.type = 0
        WHERE s.style REGEXP ?  -- 匹配任意目标风格
          AND uf.id IS NULL     -- 排除已收藏的歌曲
        ORDER BY RAND()
        LIMIT ?
    """;

        return JdbcUtil.queryList(sql, rs -> {
            try {
                PlaylistSongItemVO item = new PlaylistSongItemVO();
                item.setSongId(rs.getLong("songId"));
                item.setSongName(rs.getString("songName"));
                item.setArtistName(rs.getString("artistName"));
                item.setAlbum(rs.getString("album"));
                item.setDuration(rs.getString("duration"));
                item.setCoverUrl(rs.getString("coverUrl"));
                item.setAudioUrl(rs.getString("audioUrl"));
                item.setReleaseTime(rs.getString("releaseTime"));
                item.setLikeStatus(null);
                return item;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, excludeUserId, styleRegex.toString(), limit);
    }
}