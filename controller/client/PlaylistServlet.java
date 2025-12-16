package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.vo.PageResultVO;
import cn.edu.chtholly.model.vo.PlaylistDetailVO;
import cn.edu.chtholly.model.vo.PlaylistVO;
import cn.edu.chtholly.model.param.PlaylistQueryParam;
import cn.edu.chtholly.service.PlaylistService;
import cn.edu.chtholly.service.PlaylistDetailService;
import cn.edu.chtholly.service.impl.PlaylistServiceImpl;
import cn.edu.chtholly.service.impl.PlaylistDetailServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 歌单统一处理 Servlet
 * 处理所有歌单相关接口：
 * 1. GET /playlist/getRecommendedPlaylists - 获取推荐歌单
 * 2. POST /playlist/getAllPlaylists - 查询歌单列表（分页+条件）
 * 3. GET /playlist/getPlaylistDetail/* - 查询歌单详情（带歌单ID路径参数）
 */
@WebServlet("/playlist/*") // 匹配所有/playlist开头的请求路径，统一处理
public class PlaylistServlet extends HttpServlet {

    private final PlaylistService playlistService = new PlaylistServiceImpl();
    private final PlaylistDetailService playlistDetailService = new PlaylistDetailServiceImpl();

    /**
     * 处理所有歌单相关GET请求
     * 包括：推荐歌单、歌单详情
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo(); // 获取/playlist后的路径部分（如/getRecommendedPlaylists、/getPlaylistDetail/15）

        // 1. 处理推荐歌单请求：/playlist/getRecommendedPlaylists
        if ("/getRecommendedPlaylists".equals(pathInfo)) {
            handleGetRecommended(req, resp);
        }
        // 2. 处理歌单详情请求：/playlist/getPlaylistDetail/*
        else if (pathInfo != null && pathInfo.startsWith("/getPlaylistDetail/")) {
            handleGetPlaylistDetail(req, resp);
        }
        // 3. 未知接口
        else {
            ServletUtil.writeJson(resp, Result.error("接口不存在：GET " + pathInfo));
        }
    }

    /**
     * 处理歌单列表查询的POST请求：/playlist/getAllPlaylists
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        // 仅处理/getAllPlaylists的POST请求
        if ("/getAllPlaylists".equals(pathInfo)) {
            handlePostAllPlaylists(req, resp);
        } else {
            ServletUtil.writeJson(resp, Result.error("接口不存在：POST " + pathInfo));
        }
    }

    /**
     * 处理推荐歌单：GET /playlist/getRecommendedPlaylists
     */
    private void handleGetRecommended(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Result<List<PlaylistVO>> result = playlistService.getRecommendedPlaylists();
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("获取推荐歌单失败：" + e.getMessage()));
        }
    }

    /**
     * 处理歌单详情：GET /playlist/getPlaylistDetail/{playlistId}
     */
    private void handleGetPlaylistDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 解析URL中的歌单ID（如/getPlaylistDetail/15 → 15）
            String[] pathParts = req.getPathInfo().split("/");
            if (pathParts.length < 3) { // 格式应为/getPlaylistDetail/[ID]，至少3部分（空、getPlaylistDetail、ID）
                ServletUtil.writeJson(resp, Result.error("歌单ID不能为空"));
                return;
            }
            String playlistIdStr = pathParts[2];
            Long playlistId = Long.parseLong(playlistIdStr);

            // 获取当前登录用户ID（未登录则为null）
            Long userId = UserThreadLocal.getUserId();

            // 调用Service查询详情
            Result<PlaylistDetailVO> result = playlistDetailService.getPlaylistDetail(playlistId, userId);
            ServletUtil.writeJson(resp, result);

        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("歌单ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("获取歌单详情失败：" + e.getMessage()));
        } finally {
            UserThreadLocal.remove(); // 清除ThreadLocal，避免内存泄漏
        }
    }

    /**
     * 处理歌单列表查询：POST /playlist/getAllPlaylists
     */
    private void handlePostAllPlaylists(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 解析请求体的分页/条件参数
            PlaylistQueryParam queryVO = ServletUtil.parseJson(req, PlaylistQueryParam.class);
            // 调用Service查询分页歌单
            Result<PageResultVO> result = playlistService.getAllPlaylists(queryVO);
            // 响应结果
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("查询歌单列表失败：" + e.getMessage()));
        }
    }
}