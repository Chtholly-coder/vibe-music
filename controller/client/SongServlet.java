package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.param.SongQueryParam;
import cn.edu.chtholly.model.vo.PageResultVO;
import cn.edu.chtholly.model.vo.SongDetailVO;
import cn.edu.chtholly.service.SongService;
import cn.edu.chtholly.service.impl.SongServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 统一的歌曲相关Servlet
 * 处理：
 * 1. GET /song/getSongDetail/{songId} - 查询歌曲详情
 * 2. POST /song/getAllSongs - 查询歌曲分页列表
 */
@WebServlet("/song/*")
public class SongServlet extends HttpServlet {

    private final SongService songService = new SongServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String pathInfo = req.getPathInfo(); // 获取请求路径（如"/getSongDetail/123"或"/getAllSongs"）

        // 匹配「查询歌曲详情」接口
        if (pathInfo != null && pathInfo.startsWith("/getSongDetail/")) {
            handleGetSongDetail(pathInfo, resp);
        } else {
            // 非GET类型的歌曲接口，返回接口不存在
            ServletUtil.writeJson(resp, Result.error("接口不存在"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String pathInfo = req.getPathInfo(); // 获取POST请求的路径（如"/getAllSongs"）

        // 匹配「查询歌曲列表」接口
        if ("/getAllSongs".equals(pathInfo)) {
            handleGetAllSongs(req, resp);
        } else {
            // 非POST类型的歌曲接口，返回接口不存在
            ServletUtil.writeJson(resp, Result.error("接口不存在"));
        }
    }

    // 处理「查询歌曲详情」逻辑（GET）
    private void handleGetSongDetail(String pathInfo, HttpServletResponse resp) throws IOException {
        // 提取歌曲ID（如从"/getSongDetail/123"中提取"123"）
        String idStr = pathInfo.substring("/getSongDetail/".length());
        try {
            Long songId = Long.parseLong(idStr);
            Result<SongDetailVO> result = songService.getSongDetail(songId);
            ServletUtil.writeJson(resp, result);
        } catch (NumberFormatException e) {
            // 无效ID格式的异常处理
            ServletUtil.writeJson(resp, Result.error("无效的歌曲ID"));
        }
    }

    // 处理「查询歌曲列表」逻辑（POST）
    private void handleGetAllSongs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 解析JSON请求体为SongQueryParam
            SongQueryParam queryVO = ServletUtil.parseJson(req, SongQueryParam.class);
            // 调用Service层获取分页结果
            Result<PageResultVO> result = songService.getAllSongs(queryVO);
            // 响应JSON结果
            ServletUtil.writeJson(resp, result);
        } catch (Exception e) {
            // 通用异常处理，返回错误信息
            ServletUtil.writeJson(resp, Result.error("查询歌曲列表失败：" + e.getMessage()));
        }
    }
}