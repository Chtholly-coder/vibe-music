package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.vo.LoginVO;
import cn.edu.chtholly.service.UserService;
import cn.edu.chtholly.service.impl.UserServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/user/login")
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            // 1. 设置请求/响应编码（防止中文乱码）

        try {
            // 2. 解析前端JSON请求体为LoginDTO
            LoginVO loginVO = ServletUtil.parseJson(req, LoginVO.class);

            // 3. 调用Service处理登录逻辑
            Result<String> result = userService.login(loginVO);

            // 4. 返回JSON响应
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            // 处理解析异常等错误
            ServletUtil.writeJson(resp, Result.error("登录失败：" + e.getMessage()));
        }
    }
}