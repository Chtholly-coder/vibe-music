package cn.edu.chtholly.controller.admin;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.service.AdminStatService;
import cn.edu.chtholly.service.impl.AdminStatServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/*")
public class AdminStatServlet extends HttpServlet {

    private final AdminStatService statService = new AdminStatServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo(); // 获取请求路径（如/getAllSongsCount）

        try {
            switch (pathInfo) {
                case "/getAllSongsCount":
                    // 处理歌曲统计（支持?style=欧美流行）
                    String songStyle = req.getParameter("style");
                    Result<Integer> songResult = statService.getAllSongsCount(songStyle);
                    ServletUtil.writeJson(resp, songResult);
                    break;

                case "/getAllArtistsCount":
                    // 处理艺术家统计（支持?area=美国 或 ?gender=0）
                    String area = req.getParameter("area");
                    String genderStr = req.getParameter("gender");
                    Integer gender = genderStr != null ? Integer.parseInt(genderStr) : null;
                    Result<Integer> artistResult = statService.getAllArtistsCount(area, gender);
                    ServletUtil.writeJson(resp, artistResult);
                    break;

                case "/getAllUsersCount":
                    // 处理用户统计（无参数）
                    Result<Integer> userResult = statService.getAllUsersCount();
                    ServletUtil.writeJson(resp, userResult);
                    break;

                case "/getAllPlaylistsCount":
                    // 处理歌单统计（支持?style=欧美流行）
                    String playlistStyle = req.getParameter("style");
                    Result<Integer> playlistResult = statService.getAllPlaylistsCount(playlistStyle);
                    ServletUtil.writeJson(resp, playlistResult);
                    break;

                default:
                    ServletUtil.writeJson(resp, Result.error("接口不存在"));
            }
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("请求处理失败：" + e.getMessage()));
        }
    }
}