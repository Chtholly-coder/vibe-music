package cn.edu.chtholly.dao;

import cn.edu.chtholly.model.PageResult;
import cn.edu.chtholly.model.entity.Artist;
import cn.edu.chtholly.model.param.ArtistQueryParam;
import cn.edu.chtholly.model.vo.ArtistItemDetailVO;
import cn.edu.chtholly.model.vo.ArtistItemVO;
import cn.edu.chtholly.model.vo.ArtistNameVO;

import java.util.List;

public interface ArtistDao {
    // 根据id查询艺术家
    Artist selectById(Long id);

    // 根据ID查询艺术家详情
    Artist selectDetailById(Long artistId);

    // 查询符合条件的艺术家总数
    Long selectAllArtistsTotal(String name, Integer gender, String area);

    // 分页查询符合条件的艺术家列表
    List<ArtistItemVO> selectAllArtistsPage(String name, Integer gender, String area, int offset, int pageSize);

    // 分页查询歌手（带条件）
    PageResult<ArtistItemDetailVO> selectByPage(ArtistQueryParam param);

    // 新增歌手
    int insert(Artist artist);

    // 更新歌手
    int update(Artist artist);

    // 删除歌手
    int deleteById(Long id);


    // 根据歌手ID更新头像URL
    int updateAvatar(Long artistId, String newAvatarUrl);


    // 新增：查询所有艺术家名称（仅ID和名称）
    List<ArtistNameVO> selectAllArtistNames();

    // 批量删除歌手
    int deleteByIds(List<Long> ids);

}