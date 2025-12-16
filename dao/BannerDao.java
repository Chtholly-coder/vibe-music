package cn.edu.chtholly.dao;

import cn.edu.chtholly.model.entity.Banner;
import cn.edu.chtholly.model.param.BannerQueryParam;

import java.util.List;

public interface BannerDao {
    List<Banner> selectAllOrderByDesc();

    // 分页查询轮播图（带条件）
    List<Banner> selectByPage(BannerQueryParam param, int offset);

    // 查询符合条件的总条数
    Long selectTotal(BannerQueryParam param);

    // 插入轮播图
    int insert(Banner banner);

    // 更新轮播图（状态或URL）
    int update(Banner banner);

    // 根据id删除轮播图
    int deleteById(Long id);

    // 根据id查询轮播图
    Banner selectById(Long id);

    // 批量删除
    int deleteByIds(List<Long> ids);

}