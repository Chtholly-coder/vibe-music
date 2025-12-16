package cn.edu.chtholly.controller.admin;

import cn.edu.chtholly.model.entity.Playlist;
import cn.edu.chtholly.model.param.PlaylistQueryParam;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.PlaylistService;
import cn.edu.chtholly.service.impl.PlaylistServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.List;

// 支持文件上传
@MultipartConfig
@WebServlet({
        "/admin/getAllPlaylists",
        "/admin/addPlaylist",
        "/admin/updatePlaylist",
        "/admin/updatePlaylistCover/*",
        "/admin/deletePlaylist/*",
        "/admin/deletePlaylists"
})
public class AdminPlaylistServlet extends HttpServlet {

    private final PlaylistService playlistService = new PlaylistServiceImpl();

    // 处理POST请求（查询、添加）
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            // 1. 查询所有歌单：POST /admin/getAllPlaylists
            if (path.endsWith("/getAllPlaylists")) {
                PlaylistQueryParam param = ServletUtil.parseJson(req, PlaylistQueryParam.class);
                ServletUtil.writeJson(resp, playlistService.adminGetAllPlaylists(param));
            }
            // 2. 添加歌单：POST /admin/addPlaylist
            else if (path.endsWith("/addPlaylist")) {
                // 解析请求体（注意：前端传入的是playlistId，映射到实体的id）
                Playlist playlist = ServletUtil.parseJson(req, Playlist.class);
                ServletUtil.writeJson(resp, playlistService.addPlaylist(playlist));
            }
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("请求处理失败：" + e.getMessage()));
        }
    }

    // 处理PUT请求（更新歌单信息）
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            // 更新歌单信息：PUT /admin/updatePlaylist
            if (path.endsWith("/updatePlaylist")) {
                Playlist playlist = ServletUtil.parseJson(req, Playlist.class);
                ServletUtil.writeJson(resp, playlistService.updatePlaylist(playlist));
            }
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("更新失败：" + e.getMessage()));
        }
    }

    // 处理PATCH请求（更新封面）
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            // 更新歌单封面：PATCH /admin/updatePlaylistCover/24
            if (path.contains("/updatePlaylistCover/")) {
                // 解析路径中的歌单ID
                String pathInfo = req.getPathInfo(); // 例如 "/24"
                if (pathInfo == null || pathInfo.length() <= 1) {
                    ServletUtil.writeJson(resp, Result.error("歌单ID不能为空"));
                    return;
                }
                Long playlistId = Long.parseLong(pathInfo.substring(1));

                // 获取form-data中的封面文件（字段名：cover）
                Part coverPart = req.getPart("cover");

                ServletUtil.writeJson(resp, playlistService.updatePlaylistCover(playlistId, coverPart));
            }
        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("歌单ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("更新失败：" + e.getMessage()));
        }
    }

    // 处理DELETE请求（删除单个、批量删除）
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            // 1. 删除单个歌单：DELETE /admin/deletePlaylist/24
            if (path.contains("/deletePlaylist/")) {
                String pathInfo = req.getPathInfo();
                if (pathInfo == null || pathInfo.length() <= 1) {
                    ServletUtil.writeJson(resp, Result.error("歌单ID不能为空"));
                    return;
                }
                Long playlistId = Long.parseLong(pathInfo.substring(1));
                ServletUtil.writeJson(resp, playlistService.deletePlaylist(playlistId));
            }
            // 2. 批量删除歌单：DELETE /admin/deletePlaylists，请求体为[26,25]
            else if (path.endsWith("/deletePlaylists")) {
                // 使用 TypeReference 确保解析为 List<Long>
                List<Long> playlistIds = ServletUtil.parseJson(req, new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
                ServletUtil.writeJson(resp, playlistService.deletePlaylists(playlistIds));
            }
        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("歌单ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.success("删除失败：" + e.getMessage()));
        }
    }
}