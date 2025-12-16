package cn.edu.chtholly.dao.impl;

import cn.edu.chtholly.dao.FeedbackDao;
import cn.edu.chtholly.model.entity.Feedback;
import cn.edu.chtholly.model.param.FeedbackQueryParam;
import cn.edu.chtholly.util.JdbcUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDaoImpl implements FeedbackDao {

    @Override
    public int addFeedback(Long userId, String content) {
        // 插入SQL
        String sql = "INSERT INTO tb_feedback (user_id, feedback, create_time) VALUES (?, ?, NOW())";
        return JdbcUtil.update(sql, userId, content);
    }

    // 映射ResultSet到Feedback实体
    private Feedback mapToFeedback(ResultSet rs){
        Feedback feedback = new Feedback();
        try {
            feedback.setId(rs.getLong("id"));
            feedback.setUserId(rs.getLong("user_id"));
            feedback.setFeedback(rs.getString("feedback"));
            feedback.setCreateTime(rs.getTimestamp("create_time"));
            return feedback;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Feedback> selectByPage(FeedbackQueryParam param, int offset) {
        StringBuilder sql = new StringBuilder("SELECT id, user_id, feedback, create_time FROM tb_feedback WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 关键词模糊搜索（匹配feedback字段）
        if (param.getKeyword() != null && !param.getKeyword().trim().isEmpty()) {
            sql.append(" AND feedback LIKE ?");
            params.add("%" + param.getKeyword().trim() + "%");
        }

        // 排序与分页（按创建时间降序）
        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");
        params.add(param.getPageSize());
        params.add(offset);

        return JdbcUtil.queryList(sql.toString(), this::mapToFeedback, params.toArray());
    }

    @Override
    public Long selectTotal(FeedbackQueryParam param) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM tb_feedback WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 复用关键词搜索条件
        if (param.getKeyword() != null && !param.getKeyword().trim().isEmpty()) {
            sql.append(" AND feedback LIKE ?");
            params.add("%" + param.getKeyword().trim() + "%");
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
    public int deleteById(Long id) {
        String sql = "DELETE FROM tb_feedback WHERE id = ?";
        return JdbcUtil.update(sql, id);
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        // 构建IN子句（动态拼接）
        StringBuilder sql = new StringBuilder("DELETE FROM tb_feedback WHERE id IN (");
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
}