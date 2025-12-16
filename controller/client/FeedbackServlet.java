package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.FeedbackService;
import cn.edu.chtholly.service.impl.FeedbackServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 新增反馈接口：POST /feedback/addFeedback?content=xxx
 */
@WebServlet("/feedback/addFeedback")
public class FeedbackServlet extends HttpServlet {

    private final FeedbackService feedbackService = new FeedbackServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 1. 校验登录状态（从ThreadLocal获取当前用户ID）
            Long userId = UserThreadLocal.getUserId();

            // 2. 获取Query参数中的content（注意：是Query参数，不是请求体）
            String content = request.getParameter("content");

            // 3. 调用业务层新增反馈
            Result<Void> result = feedbackService.addFeedback(userId, content);

            // 4. 返回响应
            ServletUtil.writeJson(response, result);

        } catch (Exception e) {
            ServletUtil.writeJson(response, Result.error("添加反馈失败：" + e.getMessage()));
        }
    }
}