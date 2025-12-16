package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.vo.PlaylistSongItemVO;
import cn.edu.chtholly.service.SongService;
import cn.edu.chtholly.service.impl.SongServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/song/getRecommendedSongs")
public class GetRecommendedSongsServlet extends HttpServlet {
    private final SongService songService = new SongServiceImpl();

    // 处理GET请求
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = req.getRequestURI();

        try {
            // 处理推荐歌曲请求
            if (path.endsWith("/getRecommendedSongs")) {
                Result<List<PlaylistSongItemVO>> result = songService.getRecommendedSongs(req);
                ServletUtil.writeJson(resp, result);
                return;
            }
            
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("获取推荐歌曲失败：" + e.getMessage()));
        }
    }

}