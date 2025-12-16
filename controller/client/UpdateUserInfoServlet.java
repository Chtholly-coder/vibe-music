package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.vo.UpdateUserInfoVO;
import cn.edu.chtholly.service.UserService;
import cn.edu.chtholly.service.impl.UserServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 更新用户信息接口：PUT /user/updateUserInfo
 */
@WebServlet("/user/updateUserInfo")
public class UpdateUserInfoServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            // 解析请求体为VO
            UpdateUserInfoVO vo = ServletUtil.parseJson(req, UpdateUserInfoVO.class);
            if (vo == null) {
                ServletUtil.writeJson(resp, Result.error("请求参数无效"));
                return;
            }

            // 调用服务层（userId从ThreadLocal获取，无需传递）
            Result<String> result = userService.updateUserInfo(vo);
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("更新信息失败：" + e.getMessage()));
        } finally {
            UserThreadLocal.remove(); // 清除ThreadLocal
        }
    }
}