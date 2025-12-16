package cn.edu.chtholly.service.impl;

import cn.edu.chtholly.dao.CommentDao;
import cn.edu.chtholly.dao.impl.CommentDaoImpl;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Comment;
import cn.edu.chtholly.model.vo.AddPlaylistCommentVO;
import cn.edu.chtholly.model.vo.AddSongCommentVO;
import cn.edu.chtholly.service.CommentService;
import cn.edu.chtholly.util.UserThreadLocal;

public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao = new CommentDaoImpl();

    // 1. 添加歌曲评论（type=0）
    @Override
    public Result<String> addSongComment(AddSongCommentVO vo) {
        // 1 登录校验
        Long userId = UserThreadLocal.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }

        // 2 手动参数校验
        if (vo.getSongId() == null || vo.getSongId() <= 0) {
            return Result.error("歌曲ID不能为空且必须为正整数");
        }
        if (vo.getContent() == null || vo.getContent().trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        if (vo.getContent().length() > 255) {
            return Result.error("评论内容不能超过255字");
        }

        // 3 校验歌曲是否存在
        if (!commentDao.existsSongById(vo.getSongId())) {
            return Result.error("该歌曲不存在，无法评论");
        }

        // 4 构建Comment实体（type=0=歌曲评论）
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setSongId(vo.getSongId());
        comment.setPlaylistId(null); // 歌单ID设为null
        comment.setContent(vo.getContent().trim());
        comment.setType(0); // 歌曲评论标识

        // 5 插入数据库
        int insertRows = commentDao.insertComment(comment);
        if (insertRows <= 0) {
            return Result.error("添加评论失败，请稍后重试");
        }

        return Result.success("添加成功");
    }

    // 2. 新增：添加歌单评论（type=1）
    @Override
    public Result<String> addPlaylistComment(AddPlaylistCommentVO vo) {
        // ① 登录校验
        Long userId = UserThreadLocal.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }

        // ② 手动参数校验
        if (vo.getPlaylistId() == null || vo.getPlaylistId() <= 0) {
            return Result.error("歌单ID不能为空且必须为正整数");
        }
        if (vo.getContent() == null || vo.getContent().trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        if (vo.getContent().length() > 255) {
            return Result.error("评论内容不能超过255字");
        }

        // ③ 校验歌单是否存在
        if (!commentDao.existsPlaylistById(vo.getPlaylistId())) {
            return Result.error("该歌单不存在，无法评论");
        }

        // ④ 构建Comment实体（type=1=歌单评论）
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setSongId(null); // 歌曲ID设为null
        comment.setPlaylistId(vo.getPlaylistId()); // 对应数据表play_list字段
        comment.setContent(vo.getContent().trim());
        comment.setType(1); // 歌单评论标识

        // ⑤ 插入数据库
        int insertRows = commentDao.insertComment(comment);
        if (insertRows <= 0) {
            return Result.error("添加评论失败，请稍后重试");
        }

        return Result.success("添加成功");
    }

    // 3. 删除评论（支持两种评论类型，逻辑不变）
    @Override
    public Result<String> deleteComment(Long commentId) {
        Long userId = UserThreadLocal.getUserId();
        if (userId == null) {
            return Result.error("请先登录");
        }

        if (commentId == null || commentId <= 0) {
            return Result.error("评论ID不能为空且必须为正整数");
        }

        // 校验评论是否存在（不管是歌曲还是歌单评论）
        if (!commentDao.existsCommentById(commentId)) {
            return Result.error("该评论不存在或已被删除");
        }

        // 校验是否为本人评论
        if (!commentDao.isCommentOwner(commentId, userId)) {
            return Result.error("无权限删除他人评论");
        }

        int deleteRows = commentDao.deleteCommentByIdAndUserId(commentId, userId);
        if (deleteRows <= 0) {
            return Result.error("删除评论失败，请稍后重试");
        }

        return Result.success("删除成功");
    }

    // 4. 点赞评论
    @Override
    public Result<String> likeComment(Long commentId) {
        if (commentId == null || commentId <= 0) {
            return Result.error("评论ID不能为空且必须为正整数");
        }

        if (!commentDao.existsCommentById(commentId)) {
            return Result.error("该评论不存在或已被删除");
        }

        int updateRows = commentDao.incrementLikeCount(commentId);
        if (updateRows <= 0) {
            return Result.error("点赞失败，请稍后重试");
        }

        return Result.success("成功");
    }
}