package cn.edu.chtholly.controller.admin;

import cn.edu.chtholly.model.entity.Song;
import cn.edu.chtholly.model.param.SongQueryParam;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.SongService;
import cn.edu.chtholly.service.impl.SongServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

// 支持文件上传
@MultipartConfig
@WebServlet({
        "/admin/getAllSongsByArtist",
        "/admin/addSong",
        "/admin/updateSong",
        "/admin/deleteSong/*",
        "/admin/deleteSongs",
        "/admin/updateSongCover/*",
        "/admin/updateSongAudio/*"
})
public class AdminSongServlet extends HttpServlet {

    private final SongService songService = new SongServiceImpl();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    // 处理POST请求（查询、添加）
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            // 1. 分页查询歌曲：POST /admin/getAllSongsByArtist
            if (path.endsWith("/getAllSongsByArtist")) {
                SongQueryParam param = ServletUtil.parseJson(req, SongQueryParam.class);
                ServletUtil.writeJson(resp, songService.adminGetAllSongsByArtist(param));
            }
            // 2. 添加歌曲：POST /admin/addSong
            else if (path.endsWith("/addSong")) {
                Song song = ServletUtil.parseJson(req, Song.class);
                // 处理日期格式（如果前端传入的是字符串）
                if (song.getReleaseTime() == null && req.getParameter("releaseTime") != null) {
                    song.setReleaseTime(sdf.parse(req.getParameter("releaseTime")));
                }
                ServletUtil.writeJson(resp, songService.addSong(song));
            }
        } catch (ParseException e) {
            ServletUtil.writeJson(resp, Result.error("日期格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("请求处理失败：" + e.getMessage()));
        }
    }

    // 处理PUT请求（更新歌曲信息）
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            // 更新歌曲信息：PUT /admin/updateSong
            if (path.endsWith("/updateSong")) {
                Song song = ServletUtil.parseJson(req, Song.class);
                // 处理日期格式
                if (song.getReleaseTime() == null && req.getParameter("releaseTime") != null) {
                    song.setReleaseTime(sdf.parse(req.getParameter("releaseTime")));
                }
                ServletUtil.writeJson(resp, songService.updateSong(song));
            }
        } catch (ParseException e) {
            ServletUtil.writeJson(resp, Result.error("日期格式错误"));
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
            // 1. 删除单个歌曲：DELETE /admin/deleteSong/539
            if (path.contains("/deleteSong/")) {
                String pathInfo = req.getPathInfo();
                if (pathInfo == null || pathInfo.length() <= 1) {
                    ServletUtil.writeJson(resp, Result.error("歌曲ID不能为空"));
                    return;
                }
                Long songId = Long.parseLong(pathInfo.substring(1));
                ServletUtil.writeJson(resp, songService.deleteSong(songId));
            }
            // 2. 批量删除歌曲：DELETE /admin/deleteSongs，请求体为[541,540]
            else if (path.endsWith("/deleteSongs")) {
                // 使用TypeReference确保解析为List<Long>
                List<Long> songIds = ServletUtil.parseJson(req, new TypeReference<List<Long>>() {});
                ServletUtil.writeJson(resp, songService.deleteSongs(songIds));
            }
        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("歌曲ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("删除失败：" + e.getMessage()));
        }
    }

    // 处理PATCH请求（更新封面、音频）
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            // 1. 更新歌曲封面：PATCH /admin/updateSongCover/540
            if (path.contains("/updateSongCover/")) {
                // 解析路径中的歌曲ID
                String pathInfo = req.getPathInfo();
                if (pathInfo == null || pathInfo.length() <= 1) {
                    ServletUtil.writeJson(resp, Result.error("歌曲ID不能为空"));
                    return;
                }
                Long songId = Long.parseLong(pathInfo.substring(1));

                // 获取form-data中的封面文件
                Part coverPart = req.getPart("cover");

                ServletUtil.writeJson(resp, songService.updateSongCover(songId, coverPart));
            }
            // 2. 更新歌曲音频：PATCH /admin/updateSongAudio/540
            else if (path.contains("/updateSongAudio/")) {
                // 解析路径中的歌曲ID
                String pathInfo = req.getPathInfo();
                if (pathInfo == null || pathInfo.length() <= 1) {
                    ServletUtil.writeJson(resp, Result.error("歌曲ID不能为空"));
                    return;
                }
                Long songId = Long.parseLong(pathInfo.substring(1));

                // 获取form-data中的音频文件和时长
                Part audioPart = req.getPart("audio");
                String duration = req.getParameter("duration");

                ServletUtil.writeJson(resp, songService.updateSongAudio(songId, audioPart, duration));
            }
        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("歌曲ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("更新失败：" + e.getMessage()));
        }
    }
}