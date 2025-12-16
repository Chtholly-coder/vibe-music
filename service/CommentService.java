package cn.edu.chtholly.service;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.vo.AddPlaylistCommentVO;
import cn.edu.chtholly.model.vo.AddSongCommentVO;

public interface CommentService {
    // 1. 歌曲评论添加（type=0）
    Result<String> addSongComment(AddSongCommentVO vo);

    // 2. 歌单评论添加（type=1，新增）
    Result<String> addPlaylistComment(AddPlaylistCommentVO vo);

    // 3. 评论删除（支持两种评论类型，不变）
    Result<String> deleteComment(Long commentId);

    // 4. 评论点赞（支持两种评论类型，不变）
    Result<String> likeComment(Long commentId);
}