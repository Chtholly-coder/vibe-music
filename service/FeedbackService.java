package cn.edu.chtholly.service;

import cn.edu.chtholly.model.PageResult;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.param.FeedbackQueryParam;
import cn.edu.chtholly.model.vo.FeedbackItemVO;

import java.util.List;

/**
 * 反馈业务层
 */
public interface FeedbackService {
    Result<Void> addFeedback(Long userId, String content);

    // 分页查询反馈列表
    Result<PageResult<FeedbackItemVO>> getAllFeedbacks(FeedbackQueryParam param);

    // 删除单个反馈
    Result<Void> deleteFeedback(Long feedbackId);

    // 批量删除反馈
    Result<Void> deleteFeedbacks(List<Long> feedbackIds);
}