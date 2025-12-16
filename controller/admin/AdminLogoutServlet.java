package cn.edu.chtholly.controller.admin;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.AdminService;
import cn.edu.chtholly.service.impl.AdminServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/logout")
public class AdminLogoutServlet extends HttpServlet {
    private final AdminService adminService = new AdminServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 1. 从请求头获取Authorization令牌
            String authHeader = req.getHeader("Authorization");

            String token = authHeader;

            // 2. 调用Service层处理登出
            Result<Void> result = adminService.logout(token);

            // 3. 返回响应
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("登出失败：" + e.getMessage()));
        }
    }
}