package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.vo.BannerVO;
import cn.edu.chtholly.service.BannerService;
import cn.edu.chtholly.service.impl.BannerServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/banner/*")
public class BannerServlet extends HttpServlet {

    private BannerService bannerService = new BannerServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 获取请求路径
        String pathInfo = req.getPathInfo();
        if ("/getBannerList".equals(pathInfo)) {
            // 处理获取轮播图列表请求
            handleGetBannerList(req, resp);
        } else {
            // 接口不存在
            ServletUtil.writeJson(resp, Result.error("接口不存在"));
        }
    }

    private void handleGetBannerList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1. 调用Service层获取数据
        Result<List<BannerVO>> result = bannerService.getBannerList();

        // 2. 响应JSON数据
        ServletUtil.writeJson(resp, result);
    }
}