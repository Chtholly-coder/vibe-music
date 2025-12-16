package cn.edu.chtholly.controller.client;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.vo.AddPlaylistCommentVO;
import cn.edu.chtholly.model.vo.AddSongCommentVO;
import cn.edu.chtholly.service.CommentService;
import cn.edu.chtholly.service.impl.CommentServiceImpl;
import cn.edu.chtholly.util.ServletUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 评论统一处理接口：
 * - POST /comment/addPlaylistComment  添加歌单评论
 * - POST /comment/addSongComment     添加歌曲评论
 * - DELETE /comment/deleteComment/{commentId}  删除评论
 * - PATCH /comment/likeComment/{commentId}    评论点赞
 */
@WebServlet("/comment/*")
public class CommentServlet extends HttpServlet {
    private final CommentService commentService = new CommentServiceImpl();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            // 获取请求路径和请求方法，分发处理逻辑
            String pathInfo = req.getPathInfo();
            String method = req.getMethod();

            if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
                ServletUtil.writeJson(resp, Result.error("无效的评论请求路径"));
                return;
            }

            // 去除路径开头的 "/"，得到核心操作路径
            String actionPath = pathInfo.substring(1);

            // 根据请求方法+操作路径分发处理
            switch (method) {
                case "POST" -> handlePostRequest(actionPath, req, resp);
                case "DELETE" -> handleDeleteRequest(actionPath, req, resp);
                case "PATCH" -> handlePatchRequest(actionPath, req, resp);
                default -> ServletUtil.writeJson(resp, Result.error("不支持的请求方法：" + method));
            }
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("评论操作失败：" + e.getMessage()));
        } finally {
            // 统一清除ThreadLocal，避免内存泄漏（公共逻辑）
            UserThreadLocal.remove();
        }
    }

    /**
     * 处理POST类型的评论请求（添加歌单/歌曲评论）
     */
    private void handlePostRequest(String actionPath, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        switch (actionPath) {
            case "addPlaylistComment":
                // 处理添加歌单评论
                AddPlaylistCommentVO playlistVO = ServletUtil.parseJson(req, AddPlaylistCommentVO.class);
                if (playlistVO == null) {
                    ServletUtil.writeJson(resp, Result.error("请求参数无效"));
                    return;
                }
                Result<String> playlistResult = commentService.addPlaylistComment(playlistVO);
                ServletUtil.writeJson(resp, playlistResult);
                break;
            case "addSongComment":
                // 处理添加歌曲评论
                AddSongCommentVO songVO = ServletUtil.parseJson(req, AddSongCommentVO.class);
                if (songVO == null) {
                    ServletUtil.writeJson(resp, Result.error("请求参数无效"));
                    return;
                }
                Result<String> songResult = commentService.addSongComment(songVO);
                ServletUtil.writeJson(resp, songResult);
                break;
            default:
                ServletUtil.writeJson(resp, Result.error("无效的POST评论操作：" + actionPath));
        }
    }

    /**
     * 处理DELETE类型的评论请求（删除评论）
     */
    private void handleDeleteRequest(String actionPath, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (actionPath.startsWith("deleteComment/")) {
            // 提取评论ID（格式：deleteComment/123）
            String commentIdStr = actionPath.substring("deleteComment/".length());
            try {
                Long commentId = Long.parseLong(commentIdStr);
                Result<String> result = commentService.deleteComment(commentId);
                ServletUtil.writeJson(resp, result);
            } catch (NumberFormatException e) {
                ServletUtil.writeJson(resp, Result.error("评论ID必须为正整数"));
            }
        } else {
            ServletUtil.writeJson(resp, Result.error("无效的DELETE评论操作：" + actionPath));
        }
    }

    /**
     * 处理PATCH类型的评论请求（评论点赞）
     */
    private void handlePatchRequest(String actionPath, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (actionPath.startsWith("likeComment/")) {
            // 提取评论ID（格式：likeComment/123）
            String commentIdStr = actionPath.substring("likeComment/".length());
            try {
                Long commentId = Long.parseLong(commentIdStr);
                Result<String> result = commentService.likeComment(commentId);
                ServletUtil.writeJson(resp, result);
            } catch (NumberFormatException e) {
                ServletUtil.writeJson(resp, Result.error("评论ID必须为正整数"));
            }
        } else {
            ServletUtil.writeJson(resp, Result.error("无效的PATCH评论操作：" + actionPath));
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service(req, resp);
    }


    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        service(req, resp);
    }
}