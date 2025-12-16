package cn.edu.chtholly.service.impl;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.AdminStatService;
import cn.edu.chtholly.util.JdbcUtil;
import java.sql.ResultSet;
import java.util.function.Function;

public class AdminStatServiceImpl implements AdminStatService {

    // 映射ResultSet到Integer（获取count结果）
    private static final Function<ResultSet, Integer> COUNT_MAPPER = rs -> {
        try {
            return rs.getInt(1); // 取第一列（COUNT(*)的结果）
        } catch (Exception e) {
            throw new RuntimeException("结果映射失败", e);
        }
    };

    /**
     * 统计歌曲数量
     * - 无风格参数：统计所有歌曲
     * - 有风格参数：统计style字段包含该风格的歌曲（支持逗号分隔的多风格）
     */
    @Override
    public Result<Integer> getAllSongsCount(String style) {
        try {
            String sql;
            Object[] params;
            if (style != null && !style.trim().isEmpty()) {
                // 模糊匹配包含目标风格的歌曲（style是逗号分隔字符串，如"欧美流行,摇滚"）
                sql = "SELECT COUNT(*) FROM tb_song WHERE style LIKE ?";
                params = new Object[]{"%" + style.trim() + "%"};
            } else {
                // 统计所有歌曲
                sql = "SELECT COUNT(*) FROM tb_song";
                params = new Object[]{};
            }
            Integer count = JdbcUtil.queryOne(sql, COUNT_MAPPER, params);
            return Result.success(count == null ? 0 : count);
        } catch (Exception e) {
            return Result.error("统计歌曲数量失败：" + e.getMessage());
        }
    }

    /**
     * 统计艺术家数量
     * - 按地区：area不为空时
     * - 按性别：gender不为空时（0-男，1-女，2-组合）
     * - 无参数：统计所有艺术家
     */
    @Override
    public Result<Integer> getAllArtistsCount(String area, Integer gender) {
        try {
            String sql;
            Object[] params;
            if (area != null && !area.trim().isEmpty()) {
                // 按地区统计
                sql = "SELECT COUNT(*) FROM tb_artist WHERE area = ?";
                params = new Object[]{area.trim()};
            } else if (gender != null) {
                // 按性别统计
                sql = "SELECT COUNT(*) FROM tb_artist WHERE gender = ?";
                params = new Object[]{gender};
            } else {
                // 统计所有艺术家
                sql = "SELECT COUNT(*) FROM tb_artist";
                params = new Object[]{};
            }
            Integer count = JdbcUtil.queryOne(sql, COUNT_MAPPER, params);
            return Result.success(count == null ? 0 : count);
        } catch (Exception e) {
            return Result.error("统计艺术家数量失败：" + e.getMessage());
        }
    }

    /**
     * 统计所有用户数量
     */
    @Override
    public Result<Integer> getAllUsersCount() {
        try {
            String sql = "SELECT COUNT(*) FROM tb_user";
            Integer count = JdbcUtil.queryOne(sql, COUNT_MAPPER);
            return Result.success(count == null ? 0 : count);
        } catch (Exception e) {
            return Result.error("统计用户数量失败：" + e.getMessage());
        }
    }

    /**
     * 统计歌单数量
     * - 无风格参数：统计所有歌单
     * - 有风格参数：统计指定风格的歌单（Playlist的style是单个风格）
     */
    @Override
    public Result<Integer> getAllPlaylistsCount(String style) {
        try {
            String sql;
            Object[] params;
            if (style != null && !style.trim().isEmpty()) {
                // 按风格统计（精确匹配，因为Playlist的style是单个风格）
                sql = "SELECT COUNT(*) FROM tb_playlist WHERE style = ?";
                params = new Object[]{style.trim()};
            } else {
                // 统计所有歌单
                sql = "SELECT COUNT(*) FROM tb_playlist";
                params = new Object[]{};
            }
            Integer count = JdbcUtil.queryOne(sql, COUNT_MAPPER, params);
            return Result.success(count == null ? 0 : count);
        } catch (Exception e) {
            return Result.error("统计歌单数量失败：" + e.getMessage());
        }
    }
}