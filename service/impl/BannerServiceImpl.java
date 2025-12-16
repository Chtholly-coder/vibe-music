package cn.edu.chtholly.service.impl;

import cn.edu.chtholly.dao.BannerDao;
import cn.edu.chtholly.dao.impl.BannerDaoImpl;
import cn.edu.chtholly.model.PageResult;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Banner;
import cn.edu.chtholly.model.param.BannerQueryParam;
import cn.edu.chtholly.model.vo.BannerItemVO;
import cn.edu.chtholly.model.vo.BannerVO;
import cn.edu.chtholly.service.BannerService;
import cn.edu.chtholly.util.MinioUtil;
import jakarta.servlet.http.Part;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BannerServiceImpl implements BannerService {

    // 手动实例化DAO
    private BannerDao bannerDao = new BannerDaoImpl();

    @Override
    public Result<List<BannerVO>> getBannerList() {
        // 1. 查询数据库获取所有轮播图
        List<Banner> bannerList = bannerDao.selectAllOrderByDesc();

        // 2. 转换为VO
        List<BannerVO> bannerVOList = bannerList.stream().map(banner -> {
            BannerVO vo = new BannerVO();
            vo.setBannerId(banner.getId());       // 映射id为bannerId
            vo.setBannerUrl(banner.getBannerUrl()); // 映射banner_url为bannerUrl
            return vo;
        }).collect(Collectors.toList());

        // 3. 返回成功响应
        return Result.success(bannerVOList);
    }

    // 状态转换：数据库status→前端bannerStatus
    private String statusToBannerStatus(Integer status) {
        return status == 0 ? "ENABLE" : "DISABLE";
    }

    // 状态转换：前端bannerStatus→数据库status
    private Integer bannerStatusToStatus(String bannerStatus) {
        return "ENABLE".equals(bannerStatus) ? 0 : 1;
    }

    @Override
    public Result<PageResult<BannerItemVO>> getAllBanners(BannerQueryParam param) {
        // 处理默认分页参数
        int pageNum = param.getPageNum() == null ? 1 : param.getPageNum();
        int pageSize = param.getPageSize() == null ? 5 : param.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 查询数据
        List<Banner> banners = bannerDao.selectByPage(param, offset);
        Long total = bannerDao.selectTotal(param);

        // 转换为VO列表（实体→VO）
        List<BannerItemVO> items = banners.stream().map(banner -> {
            BannerItemVO vo = new BannerItemVO();
            vo.setBannerId(banner.getId());
            vo.setBannerUrl(banner.getBannerUrl());
            vo.setBannerStatus(statusToBannerStatus(banner.getStatus()));
            return vo;
        }).collect(Collectors.toList());

        // 封装分页结果
        PageResult<BannerItemVO> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setItems(items);
        return Result.success(pageResult, "操作成功");
    }

    @Override
    public Result<Void> addBanner(Part bannerFile) {
        // 校验文件
        if (bannerFile == null || bannerFile.getSize() <= 0) {
            return Result.error("轮播图文件不能为空");
        }

        try {
            // 调用MinioUtil专门的轮播图上传方法
            String bannerUrl = MinioUtil.uploadBanner(bannerFile);

            // 保存到数据库（默认启用状态：0）
            Banner banner = new Banner();
            banner.setBannerUrl(bannerUrl);
            banner.setStatus(0); // 默认为启用

            int rows = bannerDao.insert(banner);
            if (rows <= 0) {
                // 数据库插入失败，回滚MinIO文件
                MinioUtil.deleteFile(bannerUrl);
                return Result.error("添加轮播图失败");
            }
            return Result.success(null, "添加成功");
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    // 更新轮播图状态
    @Override
    public Result<Void> updateBannerStatus(Long bannerId, Integer status) {
        // 1. 参数校验
        if (bannerId == null) {
            return Result.error("轮播图ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("状态值必须为0（启用）或1（禁用）");
        }

        // 2. 校验轮播图是否存在
        Banner banner = bannerDao.selectById(bannerId);
        if (banner == null) {
            return Result.error("轮播图不存在");
        }

        // 3. 更新状态
        Banner updateBanner = new Banner();
        updateBanner.setId(bannerId);
        updateBanner.setStatus(status);
        int rows = bannerDao.update(updateBanner);

        return rows > 0 ? Result.success(null, "更新成功") : Result.error("更新失败");
    }

    // 只更新轮播图图片
    @Override
    public Result<Void> updateBannerImage(Long bannerId, Part newBannerFile) {
        // 1. 参数校验
        if (bannerId == null) {
            return Result.error("轮播图ID不能为空");
        }
        if (newBannerFile == null || newBannerFile.getSize() <= 0) {
            return Result.error("轮播图文件不能为空");
        }

        // 2. 校验轮播图是否存在
        Banner banner = bannerDao.selectById(bannerId);
        if (banner == null) {
            return Result.error("轮播图不存在");
        }

        try {
            // 3. 上传新图片到MinIO
            String newBannerUrl = MinioUtil.uploadBanner(newBannerFile);

            // 4. 更新数据库中的图片URL（只更新banner_url字段）
            Banner updateBanner = new Banner();
            updateBanner.setId(bannerId);
            updateBanner.setBannerUrl(newBannerUrl);
            int rows = bannerDao.update(updateBanner);

            if (rows <= 0) {
                // 数据库更新失败，回滚MinIO文件
                MinioUtil.deleteFile(newBannerUrl);
                return Result.error("更新失败");
            }

            // 5. 删除旧图片
            MinioUtil.deleteFile(banner.getBannerUrl());

            return Result.success(null, "更新成功");
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteBanner(Long bannerId) {
        if (bannerId == null) {
            return Result.error("轮播图ID不能为空");
        }
        Banner banner = bannerDao.selectById(bannerId);
        if (banner == null) {
            return Result.error("轮播图不存在");
        }

        try {
            // 删除数据库记录
            int rows = bannerDao.deleteById(bannerId);
            if (rows <= 0) {
                return Result.error("删除失败");
            }

            // 删除MinIO中的图片（使用通用删除方法）
            MinioUtil.deleteFile(banner.getBannerUrl());
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteBanners(List<Long> bannerIds) {
        if (bannerIds == null || bannerIds.isEmpty()) {
            return Result.error("轮播图ID列表不能为空");
        }

        try {
            // 1. 先批量查询所有轮播图（校验是否存在）
            List<Banner> banners = new ArrayList<>();
            for (Long id : bannerIds) {
                Banner banner = bannerDao.selectById(id);
                if (banner == null) {
                    return Result.error("轮播图ID " + id + " 不存在");
                }
                banners.add(banner);
            }

            // 2. 批量删除数据库记录（性能更优）
            int rows = bannerDao.deleteByIds(bannerIds);
            if (rows != bannerIds.size()) {
                return Result.error("部分轮播图删除失败");
            }

            // 3. 批量删除MinIO文件
            for (Banner banner : banners) {
                MinioUtil.deleteFile(banner.getBannerUrl());
            }

            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }
}