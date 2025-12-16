package cn.edu.chtholly.controller.admin;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.param.AdminLoginParam;
import cn.edu.chtholly.service.AdminService;
import cn.edu.chtholly.service.impl.AdminServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {
    private final AdminService adminService = new AdminServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 1. 解析请求体JSON为登录参数
            AdminLoginParam loginParam = ServletUtil.parseJson(req, AdminLoginParam.class);

            // 2. 调用Service层处理登录
            Result<String> result = adminService.login(loginParam.getUsername(), loginParam.getPassword());

            // 3. 返回响应
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("登录失败：" + e.getMessage()));
        }
    }
}