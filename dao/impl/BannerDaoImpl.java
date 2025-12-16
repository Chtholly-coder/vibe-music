package cn.edu.chtholly.dao.impl;

import cn.edu.chtholly.dao.BannerDao;
import cn.edu.chtholly.model.entity.Banner;
import cn.edu.chtholly.model.param.BannerQueryParam;
import cn.edu.chtholly.util.JdbcUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BannerDaoImpl implements BannerDao {

    // 映射ResultSet到Banner实体
    private Banner mapToBanner(ResultSet rs) {
        Banner banner = new Banner();
        try {
            banner.setId(rs.getLong("id"));
            banner.setBannerUrl(rs.getString("banner_url"));
            banner.setStatus(rs.getInt("status"));
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return banner;
    }

    // 查询所有轮播图
    @Override
    public List<Banner> selectAllOrderByDesc() {
        String sql = "SELECT id, banner_url, status FROM tb_banner ORDER BY id DESC";
        return JdbcUtil.queryList(sql, this::mapToBanner);
    }

    @Override
    public List<Banner> selectByPage(BannerQueryParam param, int offset) {
        StringBuilder sql = new StringBuilder("SELECT id, banner_url, status FROM tb_banner WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 状态筛选（前端bannerStatus→数据库status）
        if (param.getBannerStatus() != null) {
            int status = "1".equals(param.getBannerStatus()) ? 1 : 0;
            sql.append(" AND status = ?");
            params.add(status);
        }

        // 排序与分页（按id降序，最新的在前）
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(param.getPageSize());
        params.add(offset);

        return JdbcUtil.queryList(sql.toString(), this::mapToBanner, params.toArray());
    }

    @Override
    public Long selectTotal(BannerQueryParam param) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM tb_banner WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 复用状态筛选条件
        if (param.getBannerStatus() != null) {
            int status = "1".equals(param.getBannerStatus()) ? 1 : 0;
            sql.append(" AND status = ?");
            params.add(status);
        }

        return JdbcUtil.queryOne(sql.toString(), rs -> {
            try {
                return rs.getLong("total");
            } catch (SQLException e) {
                return 0L;
            }
        }, params.toArray());
    }

    @Override
    public int insert(Banner banner) {
        String sql = "INSERT INTO tb_banner (banner_url, status) VALUES (?, ?)";
        return JdbcUtil.update(sql, banner.getBannerUrl(), banner.getStatus());
    }

    @Override
    public int update(Banner banner) {
        // 动态更新：只更新非null字段
        StringBuilder sql = new StringBuilder("UPDATE tb_banner SET ");
        List<Object> params = new ArrayList<>();

        if (banner.getBannerUrl() != null) {
            sql.append("banner_url = ?, ");
            params.add(banner.getBannerUrl());
        }
        if (banner.getStatus() != null) {
            sql.append("status = ?, ");
            params.add(banner.getStatus());
        }

        // 移除末尾逗号
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ?");
        params.add(banner.getId());

        return JdbcUtil.update(sql.toString(), params.toArray());
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM tb_banner WHERE id = ?";
        return JdbcUtil.update(sql, id);
    }

    @Override
    public Banner selectById(Long id) {
        String sql = "SELECT id, banner_url, status FROM tb_banner WHERE id = ?";
        return JdbcUtil.queryOne(sql, this::mapToBanner, id);
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        // 构建IN子句：?的数量匹配ID个数
        StringBuilder sql = new StringBuilder("DELETE FROM tb_banner WHERE id IN (");
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