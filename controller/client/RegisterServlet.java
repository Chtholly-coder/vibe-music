package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.vo.RegisterVO;
import cn.edu.chtholly.service.UserService;
import cn.edu.chtholly.service.impl.UserServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户注册接口：POST /user/register
 */
@WebServlet("/user/register")
public class RegisterServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            // 1. 解析前端JSON请求体为RegisterVO
            RegisterVO registerVO = ServletUtil.parseJson(req, RegisterVO.class);
            if (registerVO == null) {
                ServletUtil.writeJson(resp, Result.error("请求参数无效"));
                return;
            }
            // 2. 调用服务层执行注册
            Result<String> result = userService.register(registerVO);
            // 3. 响应结果
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("注册失败：" + e.getMessage()));
        }
    }
}