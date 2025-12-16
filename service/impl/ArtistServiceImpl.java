package cn.edu.chtholly.service.impl;

import cn.edu.chtholly.dao.ArtistDao;
import cn.edu.chtholly.dao.FavoriteDao;
import cn.edu.chtholly.dao.SongDao;
import cn.edu.chtholly.dao.impl.ArtistDaoImpl;
import cn.edu.chtholly.dao.impl.FavoriteDaoImpl;
import cn.edu.chtholly.dao.impl.SongDaoImpl;
import cn.edu.chtholly.model.PageResult;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Artist;
import cn.edu.chtholly.model.entity.UserFavorite;
import cn.edu.chtholly.model.vo.*;
import cn.edu.chtholly.model.param.ArtistQueryParam;
import cn.edu.chtholly.model.vo.*;
import cn.edu.chtholly.service.ArtistService;
import cn.edu.chtholly.util.MinioUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import jakarta.servlet.http.Part;

import java.util.List;

public class ArtistServiceImpl implements ArtistService {

    private final ArtistDao artistDao = new ArtistDaoImpl();
    private final SongDao songDao = new SongDaoImpl();
    private final FavoriteDao favoriteDao = new FavoriteDaoImpl();

    @Override
    public Result<ArtistDetailVO> getArtistDetail(Long artistId) {
        // 1. 查询艺术家基本信息
        Artist artist = artistDao.selectDetailById(artistId);
        if (artist == null) {
            return Result.error("艺术家不存在");
        }

        // 2. 查询该艺术家的所有歌曲
        List<ArtistSongItemVO> songVOList = songDao.selectByArtistId(artistId);

        // 3. 处理歌曲的收藏状态（likeStatus）
        Long userId = UserThreadLocal.getUserId();
        if (userId != null && !songVOList.isEmpty()) {
            // 批量校验每首歌的收藏状态
            for (ArtistSongItemVO songVO : songVOList) {
                UserFavorite favorite = favoriteDao.selectByUserAndSong(userId, songVO.getSongId());
                songVO.setLikeStatus(favorite != null ? 1 : 0);
            }
        }

        // 4. 组装艺术家详情VO
        ArtistDetailVO detailVO = new ArtistDetailVO();
        detailVO.setArtistId(artist.getId());
        detailVO.setArtistName(artist.getName());
        detailVO.setGender(artist.getGender());
        detailVO.setAvatar(artist.getAvatar());
        detailVO.setBirth(artist.getBirth());
        detailVO.setArea(artist.getArea());
        detailVO.setIntroduction(artist.getIntroduction());
        detailVO.setSongs(songVOList);

        return Result.success(detailVO, "操作成功");
    }

    @Override
    public Result<PageResultVO> getAllArtists(ArtistQueryParam queryVO) {
        // 1. 校验分页参数（避免非法值）
        int pageNum = queryVO.getPageNum() < 1 ? 1 : queryVO.getPageNum();
        int pageSize = queryVO.getPageSize() < 1 || queryVO.getPageSize() > 100 ? 20 : queryVO.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 2. 处理筛选参数（去空格，空值处理）
        String name = queryVO.getName() != null ? queryVO.getName().trim() : "";
        Integer gender = queryVO.getGender(); // 允许为null（不筛选性别）
        String area = queryVO.getArea() != null ? queryVO.getArea().trim() : "";

        // 3. 查询总数和分页列表
        Long total = artistDao.selectAllArtistsTotal(name, gender, area);
        List<ArtistItemVO> artistVOList = artistDao.selectAllArtistsPage(
                name, gender, area, offset, pageSize);

        // 4. 封装响应结果
        PageResultVO pageResult = new PageResultVO();
        pageResult.setTotal(total);
        pageResult.setItems(artistVOList);

        return Result.success(pageResult, "操作成功");
    }

    /**
     * 分页查询歌手（统一返回Result<PageResult<Artist>>）
     */
    @Override
    public Result<?> getAllArtistsDetail(ArtistQueryParam param) {
        try {
            PageResult<ArtistItemDetailVO> pageResult = artistDao.selectByPage(param);
            return Result.success(pageResult); // 用Result包装分页结果
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加歌手
     */
    @Override
    public Result<Void> addArtist(Artist artist) {
        // 业务校验
        if (artist.getName() == null || artist.getName().trim().isEmpty()) {
            return Result.error("歌手名称不能为空");
        }
        if (artist.getGender() == null) {
            return Result.error("性别不能为空");
        }

        try {
            int rows = artistDao.insert(artist);
            return rows > 0 ? Result.success(null, "添加成功") : Result.error("添加失败");
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新歌手
     */
    @Override
    public Result<Void> updateArtist(Artist artist) {
        // 业务校验
        if (artist.getId() == null) {
            return Result.error("歌手ID不能为空");
        }
        if (artist.getName() == null || artist.getName().trim().isEmpty()) {
            return Result.error("歌手名称不能为空");
        }

        try {
            int rows = artistDao.update(artist);
            return rows > 0 ? Result.success(null, "更新成功") : Result.error("更新失败（歌手不存在）");
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除歌手
     */
    @Override
    public Result<Void> deleteArtist(Long artistId) {
        if (artistId == null) {
            return Result.error("歌手ID不能为空");
        }

        try {
            int rows = artistDao.deleteById(artistId);
            return rows > 0 ? Result.success(null, "删除成功") : Result.error("删除失败（歌手不存在）");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateArtistAvatar(Long artistId, Part avatarPart) {
        // 1. 校验参数
        if (artistId == null || avatarPart == null || avatarPart.getSize() <= 0) {
            return Result.error("歌手ID或头像文件不能为空");
        }

        // 2. 查询歌手（校验存在性 + 获取旧头像）
        Artist artist = artistDao.selectById(artistId);
        if (artist == null) {
            return Result.error("歌手不存在");
        }
        String oldAvatarUrl = artist.getAvatar();

        try {
            // 3. 上传新头像到MinIO
            String newAvatarUrl = MinioUtil.uploadArtistAvatar(avatarPart);

            // 4. 更新数据库中的头像URL
            int rows = artistDao.updateAvatar(artistId, newAvatarUrl);
            if (rows <= 0) {
                // 数据库更新失败，回滚MinIO上传
                MinioUtil.deleteAvatar(newAvatarUrl);
                return Result.error("更新头像失败");
            }

            // 5. 删除旧头像（若存在）
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                MinioUtil.deleteAvatar(oldAvatarUrl);
            }

            return Result.success(null, "更新成功");
        } catch (Exception e) {
            return Result.error("更新头像失败：" + e.getMessage());
        }
    }

    // 获取所有艺术家名称列表
    @Override
    public Result<List<ArtistNameVO>> getAllArtistNames() {
        List<ArtistNameVO> artistNames = artistDao.selectAllArtistNames();
        return Result.success(artistNames, "操作成功");
    }

    @Override
    public Result<Void> deleteArtists(List<Long> artistIds) {
        // 1. 参数校验
        if (artistIds == null || artistIds.isEmpty()) {
            return Result.error("歌手ID列表不能为空");
        }

        try {
            // 2. 校验所有歌手是否存在（避免删除不存在的ID）
            for (Long artistId : artistIds) {
                Artist artist = artistDao.selectById(artistId);
                if (artist == null) {
                    return Result.error("歌手ID " + artistId + " 不存在，批量删除失败");
                }
            }

            // 3. 执行批量删除（调用DAO的批量删除方法）
            int rows = artistDao.deleteByIds(artistIds);
            if (rows != artistIds.size()) {
                return Result.error("部分歌手删除失败");
            }

            // 4. 返回符合要求的成功响应
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }
}