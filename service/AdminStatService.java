package cn.edu.chtholly.service;

import cn.edu.chtholly.model.Result;

public interface AdminStatService {
    // 统计歌曲数量（支持按风格筛选）
    Result<Integer> getAllSongsCount(String style);

    // 统计艺术家数量（支持按地区、性别筛选）
    Result<Integer> getAllArtistsCount(String area, Integer gender);

    // 统计用户总数
    Result<Integer> getAllUsersCount();

    // 统计歌单数量（支持按风格筛选）
    Result<Integer> getAllPlaylistsCount(String style);
}