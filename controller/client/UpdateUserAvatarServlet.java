package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.UserService;
import cn.edu.chtholly.service.impl.UserServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;

@MultipartConfig
@WebServlet("/user/updateUserAvatar")
public class UpdateUserAvatarServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 1. 校验登录状态
            Long userId = UserThreadLocal.getUserId();
            if (userId == null) {
                ServletUtil.writeJson(response, Result.error("请先登录"));
                return;
            }

            // 2. 获取上传文件
            Part avatarPart = request.getPart("avatar");
            if (avatarPart == null || avatarPart.getSize() == 0) {
                ServletUtil.writeJson(response, Result.error("请选择头像文件"));
                return;
            }

            // 3. 调用业务层处理核心逻辑
            Result<String> result = userService.updateUserAvatar(userId, avatarPart);

            // 4. 返回响应
            ServletUtil.writeJson(response, result);

        } catch (Exception e) {
            // 异常响应也通过ServletUtil输出
            ServletUtil.writeJson(response, Result.error("服务器错误：" + e.getMessage()));
        }
    }

    // 兼容POST请求
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPatch(request, response);
    }
}