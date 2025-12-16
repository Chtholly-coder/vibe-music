package cn.edu.chtholly.model.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class ArtistDetailVO {
    private Long artistId;          // 对应tb_artist.id
    private String artistName;      // 对应tb_artist.name
    private Integer gender;         // 对应tb_artist.gender
    private String avatar;          // 对应tb_artist.avatar
    private Date birth;             // 对应tb_artist.birth
    private String area;            // 对应tb_artist.area
    private String introduction;    // 对应tb_artist.introduction
    private List<ArtistSongItemVO> songs; // 该艺术家的歌曲列表
}