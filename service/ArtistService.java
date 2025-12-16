package cn.edu.chtholly.service;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Artist;
import cn.edu.chtholly.model.param.ArtistQueryParam;
import cn.edu.chtholly.model.vo.ArtistDetailVO;
import cn.edu.chtholly.model.vo.ArtistNameVO;
import cn.edu.chtholly.model.vo.PageResultVO;
import jakarta.servlet.http.Part;

import java.util.List;

public interface ArtistService {
    // 查询所有艺术家（支持分页和筛选）
    Result<PageResultVO> getAllArtists(ArtistQueryParam queryVO);

    // 查询艺术家详情（含歌曲列表）
    Result<ArtistDetailVO> getArtistDetail(Long artistId);

    // 分页查询歌手
    Result<?> getAllArtistsDetail(ArtistQueryParam param);

    // 添加歌手
    Result<Void> addArtist(Artist artist);

    // 更新歌手
    Result<Void> updateArtist(Artist artist);

    // 删除歌手
    Result<Void> deleteArtist(Long artistId);



    // 更新歌手头像
    Result<Void> updateArtistAvatar(Long artistId, Part avatarPart);

    // 获取所有艺术家名称列表
    Result<List<ArtistNameVO>> getAllArtistNames();

    // 批量删除歌手
    Result<Void> deleteArtists(List<Long> artistIds);

}