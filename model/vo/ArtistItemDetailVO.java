package cn.edu.chtholly.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ArtistItemDetailVO {
    private Long artistId;             // id（bigint，主键）
    private String artistName;         // name（char100）
    private Integer gender;      // gender（int：0-男，1-女，2-组合/乐队）
    private String avatar;       // avatar（char255）
    private Date birth;          // birth（date）
    private String area;         // area（char30）
    private String introduction; // introduction（text）
}
