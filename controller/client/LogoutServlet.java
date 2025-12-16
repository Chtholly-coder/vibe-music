package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.util.RedisUtil;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/user/logout")
public class LogoutServlet extends HttpServlet {

    // JWT令牌有效期
    private static final int JWT_EXPIRE_SECONDS = 6 * 60 * 60;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1. 设置响应编码

        try {
            // 2. 从请求头获取JWT令牌（与登录后传递格式一致：纯令牌字符串）
            String token = req.getHeader("Authorization");

            // 3. 若令牌存在，加入Redis黑名单（设置相同过期时间）
            if (token != null && !token.trim().isEmpty()) {
                RedisUtil.addToBlacklist(token, JWT_EXPIRE_SECONDS);
            }

            // 4. 返回登出成功响应
            Result<Void> result = new Result<>();
            result.setCode(0);
            result.setMessage("登出成功");
            result.setData(null);
            ServletUtil.writeJson(resp, result);

        } catch (Exception e) {
            // 异常时仍返回成功
            Result<Void> result = new Result<>();
            result.setCode(0);
            result.setMessage("登出成功");
            result.setData(null);
            ServletUtil.writeJson(resp, result);
        }
    }
}