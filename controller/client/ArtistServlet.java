package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.param.ArtistQueryParam;
import cn.edu.chtholly.model.vo.ArtistDetailVO;
import cn.edu.chtholly.model.vo.PageResultVO;
import cn.edu.chtholly.service.ArtistService;
import cn.edu.chtholly.service.impl.ArtistServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 统一的艺术家相关Servlet
 * 处理：
 * 1. GET /artist/getArtistDetail/{artistId} - 查询艺术家详情
 * 2. POST /artist/getAllArtists - 查询艺术家分页列表
 */
@WebServlet("/artist/*")
public class ArtistServlet extends HttpServlet {

    private final ArtistService artistService = new ArtistServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo(); // 获取请求路径后缀（如"/getArtistDetail/123"）

        // 匹配「查询艺术家详情」接口，逻辑直接写在doGet中
        if (pathInfo != null && pathInfo.startsWith("/getArtistDetail/")) {
            try {
                // 提取路径中的艺术家ID（如从"/getArtistDetail/32"截取"32"）
                String idStr = pathInfo.substring("/getArtistDetail/".length());
                if (idStr.isEmpty()) {
                    ServletUtil.writeJson(response, Result.error("艺术家ID不能为空"));
                    return;
                }
                Long artistId = Long.parseLong(idStr);

                // 调用Service查询详情并响应结果
                Result<ArtistDetailVO> result = artistService.getArtistDetail(artistId);
                ServletUtil.writeJson(response, result);

            } catch (NumberFormatException e) {
                // 处理ID格式错误
                ServletUtil.writeJson(response, Result.error("艺术家ID格式错误"));
            } catch (Exception e) {
                // 处理通用异常
                ServletUtil.writeJson(response, Result.error("查询艺术家详情失败：" + e.getMessage()));
            }
        } else {
            // 非GET类型的艺术家接口，返回接口不存在
            ServletUtil.writeJson(response, Result.error("接口不存在"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String pathInfo = request.getPathInfo(); // 获取POST请求路径后缀（如"/getAllArtists"）

        // 匹配「查询艺术家列表」接口，逻辑直接写在doPost中
        if ("/getAllArtists".equals(pathInfo)) {
            try {
                // 解析前端JSON参数为ArtistQueryParam
                ArtistQueryParam queryVO = ServletUtil.parseJson(request, ArtistQueryParam.class);

                // 调用Service查询分页列表并响应结果
                Result<PageResultVO> result = artistService.getAllArtists(queryVO);
                ServletUtil.writeJson(response, result);

            } catch (Exception e) {
                // 处理通用异常
                ServletUtil.writeJson(response, Result.error("查询艺术家失败：" + e.getMessage()));
            }
        } else {
            // 非POST类型的艺术家接口，返回接口不存在
            ServletUtil.writeJson(response, Result.error("接口不存在"));
        }
    }
}