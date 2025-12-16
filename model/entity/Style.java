package cn.edu.chtholly.model.entity;

import lombok.Data;

/**
 * 风格表实体（对应tb_style）
 */
@Data
public class Style {
    private Long id;             // id（bigint）
    private String name;         // style（char50，与歌单style、歌曲style关联）
}