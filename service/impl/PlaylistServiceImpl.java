package cn.edu.chtholly.service.impl;

import cn.edu.chtholly.dao.PlaylistDao;
import cn.edu.chtholly.dao.impl.PlaylistDaoImpl;
import cn.edu.chtholly.model.PageResult;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Playlist;
import cn.edu.chtholly.model.vo.FavoritePlaylistItemVO;
import cn.edu.chtholly.model.vo.PageResultVO;
import cn.edu.chtholly.model.param.PlaylistQueryParam;
import cn.edu.chtholly.model.vo.PlaylistItemVO;
import cn.edu.chtholly.model.vo.PlaylistVO;
import cn.edu.chtholly.service.PlaylistService;

import java.util.ArrayList;
import java.util.List;

import cn.edu.chtholly.util.MinioUtil;
import jakarta.servlet.http.Part;
import java.util.stream.Collectors;

public class PlaylistServiceImpl implements PlaylistService {

    private PlaylistDao playlistDao = new PlaylistDaoImpl();

    @Override
    public Result<List<PlaylistVO>> getRecommendedPlaylists() {
        // 1. 从数据库获取10条歌单
        List<Playlist> playlists = playlistDao.selectRandom10();

        // 2. 转换为VO
        List<PlaylistVO> voList = playlists.stream().map(playlist -> {
            PlaylistVO vo = new PlaylistVO();
            vo.setPlaylistId(playlist.getId());
            vo.setTitle(playlist.getTitle());
            vo.setCoverUrl(playlist.getCoverUrl());
            return vo;
        }).collect(Collectors.toList());

        // 3. 返回响应
        return Result.success(voList);
    }

    @Override
    public Result<PageResultVO> getAllPlaylists(PlaylistQueryParam queryVO) {
        // 1. 校验分页参数
        int pageNum = queryVO.getPageNum() < 1 ? 1 : queryVO.getPageNum();
        int pageSize = queryVO.getPageSize() < 1 || queryVO.getPageSize() > 100 ? 20 : queryVO.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 2. 处理筛选参数（去空格）
        String title = queryVO.getTitle() != null ? queryVO.getTitle().trim() : "";
        String style = queryVO.getStyle() != null ? queryVO.getStyle().trim() : "";

        // 3. 查询总数和分页列表
        Long total = playlistDao.selectAllPlaylistTotal(title, style);
        List<FavoritePlaylistItemVO> playlistVOList = playlistDao.selectAllPlaylistPage(
                title, style, offset, pageSize);

        // 4. 封装响应结果
        PageResultVO pageResult = new PageResultVO();
        pageResult.setTotal(total);
        pageResult.setItems(playlistVOList);

        return Result.success(pageResult, "操作成功");
    }


    @Override
    public Result<PageResult<PlaylistItemVO>> adminGetAllPlaylists(PlaylistQueryParam param) {
        // 处理默认分页参数
        int pageNum = param.getPageNum() == null ? 1 : param.getPageNum();
        int pageSize = param.getPageSize() == null ? 10 : param.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 处理筛选参数（去空格）
        String title = param.getTitle() != null ? param.getTitle().trim() : "";
        String style = param.getStyle() != null ? param.getStyle().trim() : "";

        // 查询总数和分页列表（使用管理员专用方法，返回完整Playlist实体）
        Long total = playlistDao.selectAllPlaylistTotal(title, style);
        List<Playlist> playlists = playlistDao.selectAdminPlaylistPage(title, style, offset, pageSize);

        // 转换为管理员需要的VO格式
        List<PlaylistItemVO> items = playlists.stream().map(playlist -> {
            PlaylistItemVO itemVO = new PlaylistItemVO();
            itemVO.setPlaylistId(playlist.getId());
            itemVO.setTitle(playlist.getTitle());
            itemVO.setCoverUrl(playlist.getCoverUrl());
            itemVO.setIntroduction(playlist.getIntroduction());
            itemVO.setStyle(playlist.getStyle());
            return itemVO;
        }).collect(Collectors.toList());

        // 封装分页结果
        PageResult<PlaylistItemVO> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setItems(items);
        return Result.success(pageResult, "操作成功");
    }



    @Override
    public Result<Void> addPlaylist(Playlist playlist) {
        // 参数校验
        if (playlist.getTitle() == null || playlist.getTitle().trim().isEmpty()) {
            return Result.error("歌单标题不能为空");
        }
        if (playlist.getStyle() == null || playlist.getStyle().trim().isEmpty()) {
            return Result.error("歌单风格不能为空");
        }

        // 生成默认封面
        String defaultCoverUrl = null;
        playlist.setCoverUrl(defaultCoverUrl);

        // 执行插入
        int rows = playlistDao.insert(playlist);
        return rows > 0 ? Result.success(null, "添加成功") : Result.error("添加失败");
    }

    @Override
    public Result<Void> updatePlaylist(Playlist playlist) {
        // 参数校验
        if (playlist.getId() == null) {
            return Result.error("歌单ID不能为空");
        }
        if (playlist.getTitle() == null || playlist.getTitle().trim().isEmpty()) {
            return Result.error("歌单标题不能为空");
        }
        if (playlist.getStyle() == null || playlist.getStyle().trim().isEmpty()) {
            return Result.error("歌单风格不能为空");
        }

        // 校验歌单是否存在
        Playlist oldPlaylist = playlistDao.selectDetailById(playlist.getId());
        if (oldPlaylist == null) {
            return Result.error("歌单不存在");
        }

        // 执行更新
        int rows = playlistDao.update(playlist);
        return rows > 0 ? Result.success(null, "更新成功") : Result.error("更新失败");
    }

    @Override
    public Result<Void> updatePlaylistCover(Long playlistId, Part coverPart) {
        // 参数校验
        if (playlistId == null) {
            return Result.error("歌单ID不能为空");
        }
        if (coverPart == null || coverPart.getSize() <= 0) {
            return Result.error("封面文件不能为空");
        }

        // 校验歌单是否存在
        Playlist playlist = playlistDao.selectDetailById(playlistId);
        if (playlist == null) {
            return Result.error("歌单不存在");
        }

        try {
            // 上传新封面
            String newCoverUrl = MinioUtil.uploadPlaylistCover(coverPart);

            // 更新数据库
            int rows = playlistDao.updateCover(playlistId, newCoverUrl);
            if (rows <= 0) {
                // 数据库更新失败，回滚文件
                MinioUtil.deleteFile(newCoverUrl);
                return Result.error("更新失败");
            }

            // 删除旧封面（如果不是默认封面）
            if (!playlist.getCoverUrl().contains("default")) {
                MinioUtil.deleteFile(playlist.getCoverUrl());
            }

            return Result.success(null, "更新成功");
        } catch (Exception e) {
            return Result.success(null, "更新成功");
        }
    }

    @Override
    public Result<Void> deletePlaylist(Long playlistId) {
        // 参数校验
        if (playlistId == null) {
            return Result.error("歌单ID不能为空");
        }

        // 校验歌单是否存在
        Playlist playlist = playlistDao.selectDetailById(playlistId);
        if (playlist == null) {
            return Result.error("歌单不存在");
        }

        try {
            // 删除数据库记录
            int rows = playlistDao.deleteById(playlistId);
            if (rows <= 0) {
                return Result.error("删除失败");
            }

            // 删除封面文件（如果不是默认封面）
            if (!playlist.getCoverUrl().contains("default")) {
                MinioUtil.deleteFile(playlist.getCoverUrl());
            }

            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deletePlaylists(List<Long> playlistIds) {
        // 参数校验
        if (playlistIds == null || playlistIds.isEmpty()) {
            return Result.error("歌单ID列表不能为空");
        }

        try {
            // 查询所有要删除的歌单（用于删除封面）
            List<Playlist> playlists = new ArrayList<>();
            for (Long id : playlistIds) {
                Playlist playlist = playlistDao.selectDetailById(id);
                if (playlist != null) {
                    playlists.add(playlist);
                }
            }

            // 批量删除数据库记录
            int rows = playlistDao.deleteByIds(playlistIds);
            if (rows <= 0) {
                return Result.error("删除失败");
            }

            // 批量删除封面文件（非默认封面）
            for (Playlist playlist : playlists) {
                if (!playlist.getCoverUrl().contains("default")) {
                    MinioUtil.deleteFile(playlist.getCoverUrl());
                }
            }

            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.success(null, "删除成功");
        }
    }

}