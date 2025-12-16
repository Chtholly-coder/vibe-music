package cn.edu.chtholly.dao;

import cn.edu.chtholly.model.entity.Feedback;
import cn.edu.chtholly.model.param.FeedbackQueryParam;

import java.util.List;

/**
 * 反馈数据访问层
 */
public interface FeedbackDao {
    // 新增反馈
    int addFeedback(Long userId, String content);

    // 分页查询反馈列表
    List<Feedback> selectByPage(FeedbackQueryParam param, int offset);

    // 查询符合条件的总条数
    Long selectTotal(FeedbackQueryParam param);

    // 根据id删除单个反馈
    int deleteById(Long id);

    // 批量删除反馈
    int deleteByIds(List<Long> ids);
}