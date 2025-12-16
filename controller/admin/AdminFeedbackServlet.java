package cn.edu.chtholly.controller.admin;

import cn.edu.chtholly.model.param.FeedbackQueryParam;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.FeedbackService;
import cn.edu.chtholly.service.impl.FeedbackServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet({
        "/admin/getAllFeedbacks",
        "/admin/deleteFeedback/*",
        "/admin/deleteFeedbacks"
})
public class AdminFeedbackServlet extends HttpServlet {

    private final FeedbackService feedbackService = new FeedbackServiceImpl();

    // 处理查询反馈列表（POST）
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            // 解析请求体JSON为查询参数
            FeedbackQueryParam param = ServletUtil.parseJson(req, FeedbackQueryParam.class);
            // 调用Service查询并返回结果
            ServletUtil.writeJson(resp, feedbackService.getAllFeedbacks(param));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("请求处理失败：" + e.getMessage()));
        }
    }

    // 处理删除反馈（DELETE）
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            // 1. 单个删除：/admin/deleteFeedback/74
            if (path.contains("/deleteFeedback/")) {
                // 解析路径中的feedbackId
                String pathInfo = req.getPathInfo(); // 例如 "/74"
                if (pathInfo == null || pathInfo.length() <= 1) {
                    ServletUtil.writeJson(resp, Result.error("反馈ID不能为空"));
                    return;
                }
                Long feedbackId = Long.parseLong(pathInfo.substring(1));
                // 调用Service删除单个反馈
                ServletUtil.writeJson(resp, feedbackService.deleteFeedback(feedbackId));
            }
            // 2. 批量删除：/admin/deleteFeedbacks，请求体为[73,71]
            else if (path.endsWith("/deleteFeedbacks")) {
                // 解析请求体JSON为ID列表
                List<Long> feedbackIds = ServletUtil.parseJson(req, List.class);
                // 调用Service批量删除
                ServletUtil.writeJson(resp, feedbackService.deleteFeedbacks(feedbackIds));
            }
        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("反馈ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("删除失败：" + e.getMessage()));
        }
    }
}