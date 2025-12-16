package cn.edu.chtholly.model.vo;

import lombok.Data;

/**
 * 艺术家名称VO（仅包含ID和名称，用于下拉选择等场景）
 */
@Data
public class ArtistNameVO {
    private Long artistId;   // 艺术家ID
    private String artistName; // 艺术家名称
}