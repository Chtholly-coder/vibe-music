package cn.edu.chtholly.service;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.vo.PlaylistDetailVO;

public interface PlaylistDetailService {
    Result<PlaylistDetailVO> getPlaylistDetail(Long playlistId, Long userId);
}