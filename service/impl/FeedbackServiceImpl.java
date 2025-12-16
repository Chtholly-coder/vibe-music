package cn.edu.chtholly.service.impl;

import cn.edu.chtholly.dao.FeedbackDao;
import cn.edu.chtholly.dao.impl.FeedbackDaoImpl;
import cn.edu.chtholly.model.PageResult;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Feedback;
import cn.edu.chtholly.model.param.FeedbackQueryParam;
import cn.edu.chtholly.model.vo.FeedbackItemVO;
import cn.edu.chtholly.service.FeedbackService;

import java.util.List;
import java.util.stream.Collectors;

public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackDao feedbackDao = new FeedbackDaoImpl();

    @Override
    public Result<Void> addFeedback(Long userId, String content) {
        // 1. 校验反馈内容非空
        if (content == null || content.trim().isEmpty()) {
            return Result.error("反馈内容不能为空");
        }

        // 2. 执行插入
        int rows = feedbackDao.addFeedback(userId, content.trim());

        // 3. 返回结果
        return rows > 0 ? Result.success(null, "添加成功") : Result.error("添加失败，请重试");
    }

    @Override
    public Result<PageResult<FeedbackItemVO>> getAllFeedbacks(FeedbackQueryParam param) {
        // 处理默认分页参数
        int pageNum = param.getPageNum() == null ? 1 : param.getPageNum();
        int pageSize = param.getPageSize() == null ? 20 : param.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 查询数据
        List<Feedback> feedbacks = feedbackDao.selectByPage(param, offset);
        Long total = feedbackDao.selectTotal(param);

        // 转换为VO列表（实体→VO）
        List<FeedbackItemVO> items = feedbacks.stream().map(feedback -> {
            FeedbackItemVO vo = new FeedbackItemVO();
            vo.setFeedbackId(feedback.getId());
            vo.setUserId(feedback.getUserId());
            vo.setFeedback(feedback.getFeedback());
            vo.setCreateTime(feedback.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        // 封装分页结果
        PageResult<FeedbackItemVO> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setItems(items);
        return Result.success(pageResult, "操作成功");
    }

    @Override
    public Result<Void> deleteFeedback(Long feedbackId) {
        // 参数校验
        if (feedbackId == null) {
            return Result.error("反馈ID不能为空");
        }

        // 执行删除
        int rows = feedbackDao.deleteById(feedbackId);
        return rows > 0 ? Result.success(null, "删除成功") : Result.error("删除失败");
    }

    @Override
    public Result<Void> deleteFeedbacks(List<Long> feedbackIds) {
        // 参数校验
        if (feedbackIds == null || feedbackIds.isEmpty()) {
            return Result.error("反馈ID列表不能为空");
        }

        // 执行批量删除
        int rows = feedbackDao.deleteByIds(feedbackIds);
        return rows > 0 ? Result.success(null, "删除成功") : Result.error("删除失败");
    }
}