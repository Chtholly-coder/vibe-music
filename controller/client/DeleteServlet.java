package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.UserService;
import cn.edu.chtholly.service.impl.UserServiceImpl;
import cn.edu.chtholly.util.RedisUtil;
import cn.edu.chtholly.util.ServletUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 删除账号接口：DELETE /user/deleteAccount
 */
@WebServlet("/user/deleteAccount")
public class DeleteServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            // 调用服务层删除账号
            Result<String> result = userService.deleteAccount();
            if (result.getCode() == 0) {
                // 账号删除成功，将当前Token加入黑名单（使其立即失效）
                String token = req.getHeader("Authorization");
                if (token != null && !token.trim().isEmpty()) {
                    RedisUtil.addToBlacklist(token, 86400); // 1天过期（足够Token失效）
                }
            }
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("删除账号失败：" + e.getMessage()));
        } finally {
            UserThreadLocal.remove(); // 清除ThreadLocal
        }
    }
}