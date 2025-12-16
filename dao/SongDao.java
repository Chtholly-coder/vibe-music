package cn.edu.chtholly.dao;

import cn.edu.chtholly.model.entity.Song;
import cn.edu.chtholly.model.vo.AdminSongItemVO;
import cn.edu.chtholly.model.vo.ArtistSongItemVO;
import cn.edu.chtholly.model.vo.PlaylistSongItemVO;
import java.util.List;
import java.util.Map;

public interface SongDao {
    Song selectById(Long songId);

    // 新增：根据艺术家ID查询歌曲列表
    List<ArtistSongItemVO> selectByArtistId(Long artistId);

    /**
     * 分页+多条件查询歌曲列表（含艺术家名称关联）
     * @param songName 歌曲名称（模糊）
     * @param artistName 艺术家名称（模糊）
     * @param album 专辑名称（模糊）
     * @param offset 分页偏移量（pageNum-1)*pageSize
     * @param pageSize 每页条数
     * @return 歌曲VO列表
     */
    List<PlaylistSongItemVO> selectSongPage(
            String songName, String artistName, String album,
            Integer offset, Integer pageSize
    );

    /**
     * 查询符合条件的歌曲总条数（用于分页）
     * @param songName 歌曲名称（模糊）
     * @param artistName 艺术家名称（模糊）
     * @param album 专辑名称（模糊）
     * @return 总记录数
     */
    Long selectSongTotal(
            String songName, String artistName, String album
    );

    /**
     * 批量查询歌曲的收藏状态（优化效率，避免循环查询）
     * @param userId 登录用户ID
     * @param songIds 歌曲ID列表
     * @return key: songId, value: likeStatus(1=已收藏，0=未收藏)
     */
    Map<Long, Integer> batchCheckSongLikeStatus(Long userId, List<Long> songIds);

    // 分页查询歌曲（带歌手ID筛选，管理员用）
    List<AdminSongItemVO> selectAdminSongPage(Long artistId, String songName, String album, Integer offset, Integer pageSize);

    // 查询符合条件的歌曲总数（带歌手ID筛选）
    Long selectAdminSongTotal(Long artistId, String songName, String album);

    // 插入歌曲
    int insert(Song song);

    // 更新歌曲信息
    int update(Song song);

    // 更新歌曲封面
    int updateCover(Long id, String coverUrl);

    // 更新歌曲音频和时长
    int updateAudio(Long id, String audioUrl, String duration);

    // 根据id删除歌曲
    int deleteById(Long id);

    // 批量删除歌曲
    int deleteByIds(List<Long> ids);

    // 查询随机歌曲（指定数量）
    List<PlaylistSongItemVO> selectRandomSongs(int limit);

    // 根据风格列表查询歌曲（排除用户已收藏的）
    List<PlaylistSongItemVO> selectSongsByStyles(List<String> styles, Long excludeUserId, int limit);

    // 获取用户收藏歌曲的所有风格（拆分逗号并去重）
    List<String> selectStylesByFavoriteSongs(Long userId);


}