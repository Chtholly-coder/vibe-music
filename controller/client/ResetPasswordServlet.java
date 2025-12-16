package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.vo.ResetPasswordVO;
import cn.edu.chtholly.service.UserService;
import cn.edu.chtholly.service.impl.UserServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 重置密码接口：PATCH /user/resetUserPassword
 */
@WebServlet("/user/resetUserPassword")
public class ResetPasswordServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            // 1.解析请求体为VO
            ResetPasswordVO vo = ServletUtil.parseJson(req, ResetPasswordVO.class);
            if (vo == null) {
                ServletUtil.writeJson(resp, Result.error("请求参数无效"));
                return;
            }

            // 2.调用服务层重置密码
            Result<String> result = userService.resetPassword(vo);
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("密码重置失败：" + e.getMessage()));
        }
    }
}