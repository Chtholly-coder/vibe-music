package cn.edu.chtholly.service;

import cn.edu.chtholly.model.PageResult;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.param.BannerQueryParam;
import cn.edu.chtholly.model.vo.BannerItemVO;
import cn.edu.chtholly.model.vo.BannerVO;
import jakarta.servlet.http.Part;

import java.util.List;

public interface BannerService {
    // 获取轮播图列表
    Result<List<BannerVO>> getBannerList();

    // 分页查询轮播图
    Result<PageResult<BannerItemVO>> getAllBanners(BannerQueryParam param);

    // 添加轮播图（含图片上传）
    Result<Void> addBanner(Part bannerFile);

    // 只更新轮播图状态（单独接口）
    Result<Void> updateBannerStatus(Long bannerId, Integer status);

    // 只更新轮播图图片（单独接口）
    Result<Void> updateBannerImage(Long bannerId, Part newBannerFile);

    // 删除轮播图（含图片删除）
    Result<Void> deleteBanner(Long bannerId);

    // 批量删除轮播图
    Result<Void> deleteBanners(List<Long> bannerIds);
}