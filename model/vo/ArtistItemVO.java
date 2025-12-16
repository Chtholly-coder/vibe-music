package cn.edu.chtholly.model.vo;

import lombok.Data;

@Data
public class ArtistItemVO {
    private Long artistId;    // 对应tb_artist.id
    private String artistName; // 对应tb_artist.name
    private String avatar;    // 对应tb_artist.avatar
}