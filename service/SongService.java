package cn.edu.chtholly.service;

import cn.edu.chtholly.model.PageResult;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Song;
import cn.edu.chtholly.model.vo.AdminSongItemVO;
import cn.edu.chtholly.model.vo.PageResultVO;
import cn.edu.chtholly.model.vo.PlaylistSongItemVO;
import cn.edu.chtholly.model.vo.SongDetailVO;
import cn.edu.chtholly.model.param.SongQueryParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.util.List;

public interface SongService {
    // 获取歌曲详情
    Result<SongDetailVO> getSongDetail(Long songId);

    /**
     * 分页查询歌曲列表（支持多条件筛选和收藏状态查询）
     * @param queryVO 查询参数（页码、每页条数、筛选条件）
     * @return 分页响应结果
     */
    Result<PageResultVO> getAllSongs(SongQueryParam queryVO);

    // 管理员分页查询歌曲列表
    Result<PageResult<AdminSongItemVO>> adminGetAllSongsByArtist(SongQueryParam param);
    // 添加歌曲
    Result<Void> addSong(Song song);
    // 更新歌曲信息
    Result<Void> updateSong(Song song);
    // 删除单个歌曲
    Result<Void> deleteSong(Long songId);
    // 批量删除歌曲
    Result<Void> deleteSongs(List<Long> songIds);
    // 更新歌曲封面
    Result<Void> updateSongCover(Long songId, Part coverPart);
    // 更新歌曲音频和时长
    Result<Void> updateSongAudio(Long songId, Part audioPart, String duration);

    // 新增：获取推荐歌曲
    Result<List<PlaylistSongItemVO>> getRecommendedSongs(HttpServletRequest request);
}