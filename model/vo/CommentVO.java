package cn.edu.chtholly.model.vo;

import lombok.Data;
import java.text.SimpleDateFormat;
import cn.edu.chtholly.model.entity.Comment;

@Data
public class CommentVO {
    private Long commentId;      // 评论id（对应tb_comment.id）
    private String username;     // 用户名（新增，对应tb_user.name）
    private String userAvatar;   // 用户头像（新增，对应tb_user.avatar）
    private String content;      // 评论内容（对应tb_comment.content）
    private String createTime;   // 评论时间（格式化后，对应tb_comment.create_time）
    private Long likeCount;      // 点赞数（对应tb_comment.like_count）

    // 从实体类转换为VO（新增username和userAvatar参数）
    public static CommentVO fromEntity(Comment comment, String username, String userAvatar) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(comment.getId());
        vo.setUsername(username != null ? username : "匿名用户"); // 默认值
        vo.setUserAvatar(userAvatar != null ? userAvatar : ""); // 默认空字符串
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0); // 避免null
        // 格式化时间为 "yyyy-MM-dd"（与响应一致）
        vo.setCreateTime(new SimpleDateFormat("yyyy-MM-dd").format(comment.getCreateTime()));
        return vo;
    }
}