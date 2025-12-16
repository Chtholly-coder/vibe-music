package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.param.FavoriteQueryParam;
import cn.edu.chtholly.model.param.FavoritePlaylistQueryParam;
import cn.edu.chtholly.model.vo.FavoriteSongsVO;
import cn.edu.chtholly.model.vo.PageResultVO;
import cn.edu.chtholly.service.FavoriteService;
import cn.edu.chtholly.service.impl.FavoriteServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 收藏功能统一Servlet
 */
@WebServlet({
        "/favorite/cancelCollectPlaylist",
        "/favorite/cancelCollectSong",
        "/favorite/collectPlaylist",
        "/favorite/collectSong",
        "/favorite/getFavoriteSongs",
        "/favorite/getFavoritePlaylists"
})
public class FavoriteServlet extends HttpServlet {

    private final FavoriteService favoriteService = new FavoriteServiceImpl();

    /**
     * 统一分发请求，完全保留原每个Servlet的逻辑
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestURI = req.getRequestURI();
        String method = req.getMethod();

        // 按原Servlet的路径和方法分发，逻辑完全复制原代码
        if ("/favorite/cancelCollectPlaylist".equals(requestURI) && "DELETE".equalsIgnoreCase(method)) {
            doCancelCollectPlaylist(req, resp);
        } else if ("/favorite/cancelCollectSong".equals(requestURI) && "DELETE".equalsIgnoreCase(method)) {
            doCancelCollectSong(req, resp);
        } else if ("/favorite/collectPlaylist".equals(requestURI) && "POST".equalsIgnoreCase(method)) {
            doCollectPlaylist(req, resp);
        } else if ("/favorite/collectSong".equals(requestURI) && "POST".equalsIgnoreCase(method)) {
            doCollectSong(req, resp);
        } else if ("/favorite/getFavoriteSongs".equals(requestURI)) {
            if ("OPTIONS".equalsIgnoreCase(method)) {
                doGetFavoriteSongsOptions(req, resp);
            } else if ("POST".equalsIgnoreCase(method)) {
                doGetFavoriteSongs(req, resp);
            }
        } else if ("/favorite/getFavoritePlaylists".equals(requestURI) && "POST".equalsIgnoreCase(method)) {
            doGetFavoritePlaylists(req, resp);
        }
    }

    private void doCancelCollectPlaylist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 1. 校验登录状态
            Long userId = UserThreadLocal.getUserId();

            // 2. 获取歌单ID参数
            String playlistIdStr = request.getParameter("playlistId");
            if (playlistIdStr == null || playlistIdStr.trim().isEmpty()) {
                ServletUtil.writeJson(response, Result.error("歌单ID不能为空"));
                return;
            }
            Long playlistId;
            try {
                playlistId = Long.parseLong(playlistIdStr.trim());
            } catch (NumberFormatException e) {
                ServletUtil.writeJson(response, Result.error("歌单ID格式错误"));
                return;
            }

            // 3. 调用Service层取消收藏
            Result<Void> result = favoriteService.cancelCollectPlaylist(userId, playlistId);
            ServletUtil.writeJson(response, result);

        } catch (Exception e) {
            ServletUtil.writeJson(response, Result.error("服务器错误：" + e.getMessage()));
        }
    }

    private void doCancelCollectSong(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        try {
            // 1. 获取当前登录用户ID
            Long userId = UserThreadLocal.getUserId();

            // 2. 获取请求参数songId
            String songIdStr = req.getParameter("songId");
            if (songIdStr == null || songIdStr.trim().isEmpty()) {
                ServletUtil.writeJson(resp, Result.error("歌曲ID不能为空"));
                return;
            }
            Long songId = Long.parseLong(songIdStr);

            // 3. 调用Service取消收藏
            Result<Void> result = favoriteService.cancelCollectSong(userId, songId);
            ServletUtil.writeJson(resp, result);

        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("歌曲ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("取消收藏失败：" + e.getMessage()));
        }
    }

    private void doCollectPlaylist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 1. 校验登录状态
            Long userId = UserThreadLocal.getUserId();

            // 2. 获取歌单ID参数
            String playlistIdStr = request.getParameter("playlistId");
            if (playlistIdStr == null || playlistIdStr.trim().isEmpty()) {
                ServletUtil.writeJson(response, Result.error("歌单ID不能为空"));
                return;
            }
            Long playlistId;
            try {
                playlistId = Long.parseLong(playlistIdStr.trim());
            } catch (NumberFormatException e) {
                ServletUtil.writeJson(response, Result.error("歌单ID格式错误"));
                return;
            }

            // 3. 调用Service层收藏歌单
            Result<Void> result = favoriteService.collectPlaylist(userId, playlistId);
            ServletUtil.writeJson(response, result);

        } catch (Exception e) {
            ServletUtil.writeJson(response, Result.error("服务器错误：" + e.getMessage()));
        }
    }

    private void doCollectSong(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        try {
            // 1. 获取当前登录用户ID（拦截器确保已登录）
            Long userId = UserThreadLocal.getUserId();

            // 2. 获取请求参数songId
            String songIdStr = req.getParameter("songId");
            if (songIdStr == null || songIdStr.trim().isEmpty()) {
                ServletUtil.writeJson(resp, Result.error("歌曲ID不能为空"));
                return;
            }
            Long songId = Long.parseLong(songIdStr);

            // 3. 调用Service收藏歌曲
            Result<Void> result = favoriteService.collectSong(userId, songId);
            ServletUtil.writeJson(resp, result);

        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("歌曲ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("收藏失败：" + e.getMessage()));
        }
    }


    private void doGetFavoriteSongsOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 设置跨域允许的头信息和方法
        resp.setHeader("Access-Control-Allow-Origin", "*"); // 允许所有源（生产环境需指定具体域名）
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS"); // 允许的方法
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization"); // 允许的请求头
        resp.setStatus(HttpServletResponse.SC_OK); // 预检成功状态码
    }

    private void doGetFavoriteSongs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 1. 获取当前登录用户ID
            Long userId = UserThreadLocal.getUserId();
            if (userId == null) {
                ServletUtil.writeJson(resp, Result.error("请先登录"));
                return;
            }

            // 2. 读取请求体的JSON参数，转换为查询对象
            FavoriteQueryParam param = ServletUtil.parseJson(req, FavoriteQueryParam.class);
            if (param == null) {
                param = new FavoriteQueryParam(); // 默认为空参数
            }

            // 3. 调用Service查询收藏列表
            Result<FavoriteSongsVO> result = favoriteService.getFavoriteSongs(userId, param);
            ServletUtil.writeJson(resp, result);

        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("获取收藏列表失败：" + e.getMessage()));
        }
    }

    private void doGetFavoritePlaylists(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 1. 解析前端JSON请求体为VO
            FavoritePlaylistQueryParam queryVO = ServletUtil.parseJson(req, FavoritePlaylistQueryParam.class);

            // 2. 调用Service层查询收藏歌单
            Result<PageResultVO> result = favoriteService.getFavoritePlaylists(queryVO);

            // 3. 响应结果
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            // 异常处理
            ServletUtil.writeJson(resp, Result.error("查询收藏歌单失败：" + e.getMessage()));
        }
    }
}