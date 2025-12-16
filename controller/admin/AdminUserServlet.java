package cn.edu.chtholly.controller.admin;

import cn.edu.chtholly.model.entity.User;
import cn.edu.chtholly.model.param.UserQueryParam;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.UserService;
import cn.edu.chtholly.service.impl.UserServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 忽略前端传入的"title"等未知字段
@JsonIgnoreProperties(ignoreUnknown = true)
@WebServlet({
        "/admin/getAllUsers",
        "/admin/addUser",
        "/admin/updateUser",
        "/admin/updateUserStatus/*",
        "/admin/deleteUser/*",
        "/admin/deleteUsers"
})
public class AdminUserServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    // 处理查询/添加用户（POST）
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            if (path.endsWith("/getAllUsers")) {
                // 分页查询用户
                UserQueryParam param = ServletUtil.parseJson(req, UserQueryParam.class);
                ServletUtil.writeJson(resp, userService.getAllUsers(param));
            } else if (path.endsWith("/addUser")) {
                // 添加用户
                User user = ServletUtil.parseJson(req, User.class);
                ServletUtil.writeJson(resp, userService.addUser(user));
            }
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("请求处理失败：" + e.getMessage()));
        }
    }

    // 处理更新用户（PUT）
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            User user = ServletUtil.parseJson(req, User.class);
            // 前端传入userId→映射为实体id
            user.setId(user.getId() != null ? user.getId() : null);
            ServletUtil.writeJson(resp, userService.updateUser(user));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("更新失败：" + e.getMessage()));
        }
    }

    // 处理更新用户状态（PATCH）
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            // 路径格式：/admin/updateUserStatus/{userId}/{status}
            String pathInfo = req.getPathInfo(); // 例如 "/138/0"
            if (pathInfo == null || pathInfo.split("/").length < 3) {
                ServletUtil.writeJson(resp, Result.error("路径格式错误，应为/updateUserStatus/{userId}/{status}"));
                return;
            }
            String[] parts = pathInfo.split("/");
            Long userId = Long.parseLong(parts[1]);
            Integer status = Integer.parseInt(parts[2]); // 0-启用，1-禁用

            ServletUtil.writeJson(resp, userService.updateUserStatus(userId, status));
        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("用户ID或状态格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("状态更新失败：" + e.getMessage()));
        }
    }

    // 处理删除用户（DELETE）
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();
        try {
            // 1. 批量删除：/admin/deleteUsers（请求体是ID数组）
            if (path.endsWith("/deleteUsers")) {
                // 解析请求体的JSON数组（默认是List<Integer>）
                List<Integer> intUserIds = ServletUtil.parseJson(req, List.class);
                if (intUserIds == null || intUserIds.isEmpty()) {
                    ServletUtil.writeJson(resp, Result.error("用户ID列表不能为空"));
                    return;
                }
                // 转换为Long类型（解决Integer→Long类型转换异常）
                List<Long> userIds = new ArrayList<>();
                for (Integer id : intUserIds) {
                    userIds.add(id.longValue());
                }
                // 调用批量删除Service
                ServletUtil.writeJson(resp, userService.deleteUsers(userIds));
            }
            // 2. 单条删除：（/admin/deleteUser/{userId}）
            else if (path.contains("/deleteUser/")) {
                String pathInfo = req.getPathInfo();
                if (pathInfo == null || pathInfo.length() <= 1) {
                    ServletUtil.writeJson(resp, Result.error("用户ID不能为空"));
                    return;
                }
                Long userId = Long.parseLong(pathInfo.substring(1));
                ServletUtil.writeJson(resp, userService.deleteUser(userId));
            } else {
                ServletUtil.writeJson(resp, Result.error("接口路径错误"));
            }
        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("用户ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("删除失败：" + e.getMessage()));
        }
    }
}