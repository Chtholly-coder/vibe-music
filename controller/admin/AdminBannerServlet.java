package cn.edu.chtholly.controller.admin;

import cn.edu.chtholly.model.param.BannerQueryParam;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.BannerService;
import cn.edu.chtholly.service.impl.BannerServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 支持文件上传
@MultipartConfig
@WebServlet({
        "/admin/getAllBanners",
        "/admin/addBanner",
        "/admin/updateBanner/*",         // 只更新图片
        "/admin/updateBannerStatus/*",   // 只更新状态
        "/admin/deleteBanner/*",
        "/admin/deleteBanners"           // 批量删除
})
public class AdminBannerServlet extends HttpServlet {

    private final BannerService bannerService = new BannerServiceImpl();

    // 处理查询轮播图（POST）
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            if (path.endsWith("/getAllBanners")) {
                // 解析查询参数
                BannerQueryParam param = ServletUtil.parseJson(req, BannerQueryParam.class);
                ServletUtil.writeJson(resp, bannerService.getAllBanners(param));
            } else if (path.endsWith("/addBanner")) {
                // 处理添加轮播图（form-data文件）
                Part bannerFile = req.getPart("banner"); // 前端表单字段名
                ServletUtil.writeJson(resp, bannerService.addBanner(bannerFile));
            }
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("请求处理失败：" + e.getMessage()));
        }
    }

    // 处理PATCH请求（状态更新/图片更新）
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            // 1. 状态更新接口：/admin/updateBannerStatus/{id}?status=1
            if (path.contains("/updateBannerStatus/")) {
                // 解析路径中的bannerId
                String pathInfo = req.getPathInfo(); // 例如 "/9"
                if (pathInfo == null || pathInfo.length() <= 1) {
                    ServletUtil.writeJson(resp, Result.error("轮播图ID不能为空"));
                    return;
                }
                Long bannerId = Long.parseLong(pathInfo.substring(1));

                // 解析查询参数中的status（前端直接传数字：0=启用，1=禁用）
                String statusStr = req.getParameter("status");
                if (statusStr == null) {
                    ServletUtil.writeJson(resp, Result.error("状态值不能为空"));
                    return;
                }
                Integer status = Integer.parseInt(statusStr);

                // 调用Service更新状态
                ServletUtil.writeJson(resp, bannerService.updateBannerStatus(bannerId, status));
            }
            // 2. 图片更新接口：/admin/updateBanner/{id}（form-data文件）
            else if (path.contains("/updateBanner/")) {
                // 解析路径中的bannerId
                String pathInfo = req.getPathInfo(); // 例如 "/9"
                if (pathInfo == null || pathInfo.length() <= 1) {
                    ServletUtil.writeJson(resp, Result.error("轮播图ID不能为空"));
                    return;
                }
                Long bannerId = Long.parseLong(pathInfo.substring(1));

                // 获取form-data中的图片文件（前端表单字段名：bannerFile）
                Part newBannerFile = req.getPart("banner");

                // 调用Service更新图片
                ServletUtil.writeJson(resp, bannerService.updateBannerImage(bannerId, newBannerFile));
            }
        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("ID或状态格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("更新失败：" + e.getMessage()));
        }
    }

    // 处理删除轮播图（DELETE）
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();
        try {
            // 1. 批量删除：/admin/deleteBanners
            if (path.endsWith("/deleteBanners")) {
                // 第一步：先解析为通用List
                List<Integer> intBannerIds = ServletUtil.parseJson(req, List.class);
                if (intBannerIds == null || intBannerIds.isEmpty()) {
                    ServletUtil.writeJson(resp, Result.error("轮播图ID列表不能为空"));
                    return;
                }
                // 第二步：将Integer列表转为Long列表
                List<Long> bannerIds = new ArrayList<>();
                for (Integer id : intBannerIds) {
                    bannerIds.add(id.longValue()); // 安全转换：Integer → Long
                }
                // 调用批量删除Service
                ServletUtil.writeJson(resp, bannerService.deleteBanners(bannerIds));
            }
            // 2. 单条删除：/admin/deleteBanner/{id}（原有逻辑保留）
            else if (path.contains("/deleteBanner/")) {
                String pathInfo = req.getPathInfo();
                if (pathInfo == null || pathInfo.length() <= 1) {
                    ServletUtil.writeJson(resp, Result.error("轮播图ID不能为空"));
                    return;
                }
                Long bannerId = Long.parseLong(pathInfo.substring(1));
                ServletUtil.writeJson(resp, bannerService.deleteBanner(bannerId));
            } else {
                ServletUtil.writeJson(resp, Result.error("接口路径错误"));
            }
        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("轮播图ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("删除失败：" + e.getMessage()));
        }
    }
}