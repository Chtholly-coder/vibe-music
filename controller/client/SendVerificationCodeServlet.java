package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.UserService;
import cn.edu.chtholly.service.impl.UserServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 发送注册验证码接口：GET /user/sendVerificationCode?email=xxx
 */
@WebServlet("/user/sendVerificationCode")
public class SendVerificationCodeServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            // 1. 获取请求参数：email
            String email = req.getParameter("email");
            if (email == null || email.trim().isEmpty()) {
                ServletUtil.writeJson(resp, Result.error("邮箱不能为空"));
                return;
            }

            // 2. 调用服务层发送验证码
            Result<String> result = userService.sendVerificationCode(email.trim());
            // 3. 响应结果
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("发送验证码失败：" + e.getMessage()));
        }
    }
}