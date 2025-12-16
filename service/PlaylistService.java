package cn.edu.chtholly.service;

import cn.edu.chtholly.model.PageResult;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Playlist;
import cn.edu.chtholly.model.vo.PageResultVO;
import cn.edu.chtholly.model.param.PlaylistQueryParam;
import cn.edu.chtholly.model.vo.PlaylistItemVO;
import cn.edu.chtholly.model.vo.PlaylistVO;
import jakarta.servlet.http.Part;

import java.util.List;

public interface PlaylistService {
    // 获取推荐歌单
    Result<List<PlaylistVO>> getRecommendedPlaylists();

    // 查询所有歌单（支持分页和筛选）
    Result<PageResultVO> getAllPlaylists(PlaylistQueryParam queryVO);

    // 分页查询所有歌单（管理员用，返回完整信息）
    Result<PageResult<PlaylistItemVO>> adminGetAllPlaylists(PlaylistQueryParam param);
    // 添加歌单
    Result<Void> addPlaylist(Playlist playlist);
    // 更新歌单信息（不含封面）
    Result<Void> updatePlaylist(Playlist playlist);
    // 更新歌单封面
    Result<Void> updatePlaylistCover(Long playlistId, Part coverPart);
    // 删除单个歌单
    Result<Void> deletePlaylist(Long playlistId);
    // 批量删除歌单
    Result<Void> deletePlaylists(List<Long> playlistIds);
}