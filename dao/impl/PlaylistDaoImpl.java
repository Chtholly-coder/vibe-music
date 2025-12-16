package cn.edu.chtholly.dao.impl;

import cn.edu.chtholly.dao.PlaylistDao;
import cn.edu.chtholly.model.entity.Playlist;
import cn.edu.chtholly.model.vo.FavoritePlaylistItemVO;
import cn.edu.chtholly.util.JdbcUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDaoImpl implements PlaylistDao {

    @Override
    public List<Playlist> selectRandom10() {
        String sql = "SELECT id, title, cover_url FROM tb_playlist ORDER BY RAND() LIMIT 10";

        return JdbcUtil.queryList(sql, rs -> {
            try {
                Playlist playlist = new Playlist();
                playlist.setId(rs.getLong("id"));
                playlist.setTitle(rs.getString("title"));
                playlist.setCoverUrl(rs.getString("cover_url"));
                return playlist;
            } catch (SQLException e) {
                // 打印异常详情
                e.printStackTrace();
                // 转换为运行时异常抛出
                throw new RuntimeException("解析歌单数据失败：" + e.getMessage(), e);
            }
        });
    }

    @Override
    public Object selectById(Long playlistId) {
        // 只需查询是否存在，返回任意非null值即可
        String sql = "SELECT id FROM tb_playlist WHERE id = ?";
        return JdbcUtil.queryOne(sql, rs -> {
            try {
                return rs.getLong("id"); // 存在则返回ID，否则返回null
            } catch (Exception e) {
                return null;
            }
        }, playlistId);
    }

    // 新增：查询所有歌单总数
    @Override
    public Long selectAllPlaylistTotal(String title, String style) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM tb_playlist WHERE 1=1");
        ArrayList<Object> params = new ArrayList<>();

        // 标题模糊筛选
        if (title != null && !title.isEmpty()) {
            sql.append(" AND title LIKE CONCAT('%', ?, '%')");
            params.add(title);
        }
        // 风格精准筛选
        if (style != null && !style.isEmpty()) {
            sql.append(" AND style = ?");
            params.add(style);
        }

        return JdbcUtil.queryOne(sql.toString(), rs -> {
            try {
                return rs.getLong("total");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, params.toArray());
    }

    // 分页查询所有歌单
    @Override
    public List<FavoritePlaylistItemVO> selectAllPlaylistPage(String title, String style, int offset, int pageSize) {
        StringBuilder sql = new StringBuilder("""
        SELECT 
            id AS playlistId,
            title AS title,
            cover_url AS coverUrl
        FROM tb_playlist 
    """);
        ArrayList<Object> params = new ArrayList<>();
        boolean hasCondition = false; // 标记是否有筛选条件

        // 标题模糊筛选
        if (title != null && !title.trim().isEmpty()) {
            sql.append(hasCondition ? " AND " : " WHERE ");
            sql.append("title LIKE CONCAT('%', ?, '%')");
            params.add(title.trim());
            hasCondition = true;
        }
        // 风格精准筛选
        if (style != null && !style.trim().isEmpty()) {
            sql.append(hasCondition ? " AND " : " WHERE ");
            sql.append("style = ?");
            params.add(style.trim());
            hasCondition = true;
        }

        // 排序 + 分页
        sql.append(" ORDER BY id DESC LIMIT ?, ?");
        params.add(offset);
        params.add(pageSize);

        // 执行查询并映射结果
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
        }, params.toArray()); // 传入所有参数
    }

    // 映射ResultSet到Playlist实体
    private Playlist mapToPlaylist(ResultSet rs) {
        Playlist playlist = new Playlist();
        try {
            playlist.setId(rs.getLong("id"));
            playlist.setTitle(rs.getString("title"));
            playlist.setCoverUrl(rs.getString("cover_url"));
            playlist.setIntroduction(rs.getString("introduction"));
            playlist.setStyle(rs.getString("style"));
            return playlist;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int insert(Playlist playlist) {
        String sql = "INSERT INTO tb_playlist (title, cover_url, introduction, style) VALUES (?, ?, ?, ?)";
        return JdbcUtil.update(sql,
                playlist.getTitle(),
                playlist.getCoverUrl(),
                playlist.getIntroduction(),
                playlist.getStyle());
    }

    @Override
    public int update(Playlist playlist) {
        String sql = "UPDATE tb_playlist SET title = ?, introduction = ?, style = ? WHERE id = ?";
        return JdbcUtil.update(sql,
                playlist.getTitle(),
                playlist.getIntroduction(),
                playlist.getStyle(),
                playlist.getId());
    }

    @Override
    public int updateCover(Long id, String coverUrl) {
        String sql = "UPDATE tb_playlist SET cover_url = ? WHERE id = ?";
        return JdbcUtil.update(sql, coverUrl, id);
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM tb_playlist WHERE id = ?";
        return JdbcUtil.update(sql, id);
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("DELETE FROM tb_playlist WHERE id IN (");
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

    @Override
    public Playlist selectDetailById(Long id) {
        String sql = "SELECT id, title, cover_url, introduction, style FROM tb_playlist WHERE id = ?";
        return JdbcUtil.queryOne(sql, this::mapToPlaylist, id);
    }


    @Override
    public List<Playlist> selectAdminPlaylistPage(String title, String style, int offset, int pageSize) {
        StringBuilder sql = new StringBuilder("SELECT id, title, cover_url, introduction, style FROM tb_playlist WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 标题模糊搜索
        if (title != null && !title.isEmpty()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + title + "%");
        }

        // 风格精确匹配
        if (style != null && !style.isEmpty()) {
            sql.append(" AND style = ?");
            params.add(style);
        }

        // 排序与分页（按id降序，最新的在前）
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        return JdbcUtil.queryList(sql.toString(), this::mapToPlaylist, params.toArray());
    }
}