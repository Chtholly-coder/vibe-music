package cn.edu.chtholly.dao.impl;

import cn.edu.chtholly.dao.ArtistDao;
import cn.edu.chtholly.model.PageResult;
import cn.edu.chtholly.model.entity.Artist;
import cn.edu.chtholly.model.param.ArtistQueryParam;
import cn.edu.chtholly.model.vo.ArtistItemDetailVO;
import cn.edu.chtholly.model.vo.ArtistItemVO;
import cn.edu.chtholly.model.vo.ArtistNameVO;
import cn.edu.chtholly.util.JdbcUtil;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ArtistDaoImpl implements ArtistDao {

    @Override
    public Artist selectById(Long id) {
        String sql = "SELECT id, name FROM tb_artist WHERE id = ?";

        return JdbcUtil.queryOne(sql, rs -> {
            try {
                return mapToArtist(rs);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, id);
    }

    // 新增：根据ID查询艺术家详情
    @Override
    public Artist selectDetailById(Long artistId) {
        String sql = """
            SELECT id, name, gender, avatar, birth, area, introduction 
            FROM tb_artist 
            WHERE id = ?
        """;
        return JdbcUtil.queryOne(sql, rs -> {
            try {
                Artist artist = new Artist();
                artist.setId(rs.getLong("id"));
                artist.setName(rs.getString("name"));
                artist.setGender(rs.getInt("gender"));
                artist.setAvatar(rs.getString("avatar"));
                artist.setBirth(rs.getDate("birth"));
                artist.setArea(rs.getString("area"));
                artist.setIntroduction(rs.getString("introduction"));
                return artist;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, artistId);
    }

    // 结果集映射为Artist实体
    private Artist mapToArtist(ResultSet rs) throws SQLException {
        Artist artist = new Artist();
        artist.setId(rs.getLong("id"));
        artist.setName(rs.getString("name"));
        return artist;
    }

    // 查询总数（带筛选条件）
    @Override
    public Long selectAllArtistsTotal(String name, Integer gender, String area) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM tb_artist");
        List<Object> params = new ArrayList<>();
        boolean hasCondition = false;

        // 名称模糊筛选（name不为空时）
        if (name != null && !name.isEmpty()) {
            sql.append(hasCondition ? " AND " : " WHERE ");
            sql.append("name LIKE CONCAT('%', ?, '%')");
            params.add(name);
            hasCondition = true;
        }

        // 性别精准筛选（gender不为null时）
        if (gender != null) {
            sql.append(hasCondition ? " AND " : " WHERE ");
            sql.append("gender = ?");
            params.add(gender);
            hasCondition = true;
        }

        // 地区精准筛选（area不为空时）
        if (area != null && !area.isEmpty()) {
            sql.append(hasCondition ? " AND " : " WHERE ");
            sql.append("area = ?");
            params.add(area);
            hasCondition = true;
        }

        return JdbcUtil.queryOne(sql.toString(), rs -> {
            try {
                return rs.getLong("total");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, params.toArray());
    }

    // 分页查询列表（带筛选条件）
    @Override
    public List<ArtistItemVO> selectAllArtistsPage(String name, Integer gender, String area, int offset, int pageSize) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                id AS artistId,
                name AS artistName,
                avatar AS avatar
            FROM tb_artist
        """);
        List<Object> params = new ArrayList<>();
        boolean hasCondition = false;

        // 名称模糊筛选
        if (name != null && !name.isEmpty()) {
            sql.append(hasCondition ? " AND " : " WHERE ");
            sql.append("name LIKE CONCAT('%', ?, '%')");
            params.add(name);
            hasCondition = true;
        }

        // 性别精准筛选
        if (gender != null) {
            sql.append(hasCondition ? " AND " : " WHERE ");
            sql.append("gender = ?");
            params.add(gender);
            hasCondition = true;
        }

        // 地区精准筛选
        if (area != null && !area.isEmpty()) {
            sql.append(hasCondition ? " AND " : " WHERE ");
            sql.append("area = ?");
            params.add(area);
            hasCondition = true;
        }

        // 排序（按id倒序，或按名称正序，根据业务需求调整）+ 分页
        sql.append(" ORDER BY id DESC LIMIT ?, ?");
        params.add(offset);   // 分页参数1：偏移量
        params.add(pageSize); // 分页参数2：每页条数

        // 执行查询并映射结果
        return JdbcUtil.queryList(sql.toString(), rs -> {
            ArtistItemVO vo = new ArtistItemVO();
            try {
                vo.setArtistId(rs.getLong("artistId"));
                vo.setArtistName(rs.getString("artistName"));
                vo.setAvatar(rs.getString("avatar"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return vo;
        }, params.toArray());
    }

    // ResultSet映射为Artist实体（数据库字段→实体字段）
    private static final Function<ResultSet, ArtistItemDetailVO> ARTIST_MAPPER = rs -> {
        try {
            ArtistItemDetailVO artist = new ArtistItemDetailVO();
            artist.setArtistId(rs.getLong("artistId"));
            artist.setArtistName(rs.getString("artistName"));
            artist.setGender(rs.getInt("gender"));
            artist.setAvatar(rs.getString("avatar"));
            artist.setBirth(rs.getDate("birth"));
            artist.setArea(rs.getString("area"));
            artist.setIntroduction(rs.getString("introduction"));
            return artist;
        } catch (Exception e) {
            throw new RuntimeException("歌手数据映射失败", e);
        }
    };

    /**
     * 分页查询歌手
     */
    @Override
    public PageResult<ArtistItemDetailVO> selectByPage(ArtistQueryParam param) {
        int pageNum = param.getPageNum() == null ? 1 : param.getPageNum();
        int pageSize = param.getPageSize() == null ? 10 : param.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<Object> params = new ArrayList<>();
        StringBuilder whereSql = new StringBuilder(" WHERE 1=1");

        // 动态拼接条件
        if (param.getName() != null && !param.getName().trim().isEmpty()) {
            whereSql.append(" AND name LIKE ?");
            params.add("%" + param.getName().trim() + "%");
        }
        if (param.getArea() != null && !param.getArea().trim().isEmpty()) {
            whereSql.append(" AND area = ?");
            params.add(param.getArea().trim());
        }
        if (param.getGender() != null) {
            whereSql.append(" AND gender = ?");
            params.add(param.getGender());
        }

        // 查询当前页数据
        String dataSql = "SELECT id as artistId, name as artistName, gender, avatar, birth, area, introduction " +
                "FROM tb_artist " + whereSql  +  " LIMIT ? OFFSET ?";
        params.add(pageSize);
        params.add(offset);
        List<ArtistItemDetailVO> items = JdbcUtil.queryList(dataSql, ARTIST_MAPPER, params.toArray());

        // 查询总条数
        String countSql = "SELECT COUNT(*) FROM tb_artist " + whereSql;
        Long total = JdbcUtil.queryOne(countSql, rs -> {
            try { return rs.getLong(1); } catch (Exception e) { return 0L; }
        }, params.subList(0, params.size() - 2).toArray());

        PageResult<ArtistItemDetailVO> pageResult = new PageResult<>();
        pageResult.setTotal(total == null ? 0 : total);
        pageResult.setItems(items);
        return pageResult;
    }

    /**
     * 新增歌手
     */
    @Override
    public int insert(Artist artist) {
        String sql = "INSERT INTO tb_artist (name, gender, avatar, birth, area, introduction) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        return JdbcUtil.update(sql,
                artist.getName(),
                artist.getGender(),
                artist.getAvatar(),
                artist.getBirth(),
                artist.getArea(),
                artist.getIntroduction()
        );
    }

    /**
     * 更新歌手
     */
    @Override
    public int update(Artist artist) {
        String sql = "UPDATE tb_artist SET " +
                "name = ?, gender = ?, avatar = ?, birth = ?, area = ?, introduction = ? " +
                "WHERE id = ?";
        return JdbcUtil.update(sql,
                artist.getName(),
                artist.getGender(),
                artist.getAvatar(),
                artist.getBirth(),
                artist.getArea(),
                artist.getIntroduction(),
                artist.getId()
        );
    }

    /**
     * 删除歌手
     */
    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM tb_artist WHERE id = ?";
        return JdbcUtil.update(sql, id);
    }

    @Override
    public int updateAvatar(Long artistId, String newAvatarUrl) {
        String sql = "UPDATE tb_artist SET avatar = ? WHERE id = ?";
        return JdbcUtil.update(sql, newAvatarUrl, artistId);
    }

    // 新增：查询所有艺术家名称（仅ID和名称，按ID降序排列）
    @Override
    public List<ArtistNameVO> selectAllArtistNames() {
        String sql = "SELECT id AS artistId, name AS artistName FROM tb_artist ORDER BY id DESC";
        return JdbcUtil.queryList(sql, rs -> {
            ArtistNameVO vo = new ArtistNameVO();
            try {
                vo.setArtistId(rs.getLong("artistId"));
                vo.setArtistName(rs.getString("artistName"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return vo;
        });
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        // 构建IN子句：?的数量匹配ID个数
        StringBuilder sql = new StringBuilder("DELETE FROM tb_artist WHERE id IN (");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            sql.append("?,");
            params.add(ids.get(i));
        }
        // 移除最后一个逗号
        sql.setLength(sql.length() - 1);
        sql.append(")");
        return JdbcUtil.update(sql.toString(), params.toArray());
    }
}