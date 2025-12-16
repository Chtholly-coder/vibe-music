package cn.edu.chtholly.controller.admin;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Artist;
import cn.edu.chtholly.model.param.ArtistQueryParam;
import cn.edu.chtholly.service.ArtistService;
import cn.edu.chtholly.service.impl.ArtistServiceImpl;
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

// 新增@MultipartConfig：支持接收multipart/form-data类型的文件上传
@MultipartConfig
@WebServlet({
        "/admin/getAllArtists",
        "/admin/addArtist",
        "/admin/updateArtist",
        "/admin/deleteArtist/*",
        "/admin/updateArtistAvatar/*",
        "/admin/getAllArtistNames", // 新增路径
        "/admin/deleteArtists"
})
public class AdminArtistServlet extends HttpServlet {

    private final ArtistService artistService = new ArtistServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            if (path.endsWith("/getAllArtists")) {
                // 分页查询
                ArtistQueryParam param = ServletUtil.parseJson(req, ArtistQueryParam.class);
                ServletUtil.writeJson(resp, artistService.getAllArtistsDetail(param));
            } else if (path.endsWith("/addArtist")) {
                // 添加歌手
                Artist artist = ServletUtil.parseJson(req, Artist.class);
                ServletUtil.writeJson(resp, artistService.addArtist(artist));
            }
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("请求处理失败：" + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            Artist artist = ServletUtil.parseJson(req, Artist.class);
            ServletUtil.writeJson(resp, artistService.updateArtist(artist));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("更新失败：" + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();
        try {
            // 1. 批量删除：/admin/deleteArtists（请求体是ID数组）
            if (path.endsWith("/deleteArtists")) {
                // 解析请求体的JSON数组（默认是List<Integer>）
                List<Integer> intArtistIds = ServletUtil.parseJson(req, List.class);
                if (intArtistIds == null || intArtistIds.isEmpty()) {
                    ServletUtil.writeJson(resp, Result.error("歌手ID列表不能为空"));
                    return;
                }
                // 转换为Long类型（解决Integer→Long类型转换异常）
                List<Long> artistIds = new ArrayList<>();
                for (Integer id : intArtistIds) {
                    artistIds.add(id.longValue());
                }
                // 调用批量删除Service
                ServletUtil.writeJson(resp, artistService.deleteArtists(artistIds));
            }
            // 2. 单条删除：原有逻辑保留（/admin/deleteArtist/{artistId}）
            else if (path.contains("/deleteArtist/")) {
                String pathInfo = req.getPathInfo();
                if (pathInfo == null || pathInfo.length() <= 1) {
                    ServletUtil.writeJson(resp, Result.error("歌手ID不能为空"));
                    return;
                }
                Long artistId = Long.parseLong(pathInfo.substring(1));
                ServletUtil.writeJson(resp, artistService.deleteArtist(artistId));
            } else {
                ServletUtil.writeJson(resp, Result.error("接口路径错误"));
            }
        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("歌手ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("删除失败：" + e.getMessage()));
        }
    }

    // 处理PATCH请求（头像更新）
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            // 1. 从路径中提取歌手ID（如：/admin/updateArtistAvatar/2 → 2）
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                ServletUtil.writeJson(resp, Result.error("歌手ID不能为空"));
                return;
            }
            Long artistId = Long.parseLong(pathInfo.substring(1));

            // 2. 获取Form Data中的头像文件
            Part avatarPart = req.getPart("avatar");
            if (avatarPart == null || avatarPart.getSize() <= 0) {
                ServletUtil.writeJson(resp, Result.error("头像文件不能为空"));
                return;
            }

            // 3. 调用Service更新头像
            ServletUtil.writeJson(resp, artistService.updateArtistAvatar(artistId, avatarPart));
        } catch (NumberFormatException e) {
            ServletUtil.writeJson(resp, Result.error("歌手ID格式错误"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("更新头像失败：" + e.getMessage()));
        }
    }

    // 处理GET请求（获取所有艺术家名称）
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            // GET /admin/getAllArtistNames
            if (path.endsWith("/getAllArtistNames")) {
                ServletUtil.writeJson(resp, artistService.getAllArtistNames());
            }
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("请求处理失败：" + e.getMessage()));
        }
    }
}