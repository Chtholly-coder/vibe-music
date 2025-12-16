package cn.edu.chtholly.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Song {
    @JsonProperty("songId")  // 映射前端传入的songId到实体的id
    private Long id;
    private Long artistId;       // artist_id（bigint，关联tb_artist.id）
    @JsonProperty("songName")  // 映射前端传入的songName到实体的name
    private String name;         // name（char255）
    private String album;        // album（char255）
    private String lyric;        // lyric（text）
    private String duration;     // duration（char10）
    private String style;        // style（char255）
    private String coverUrl;     // cover_URL（char255，对应cover_url）
    private String audioUrl;     // audio_URL（char255，对应audio_url）
    private Date releaseTime;    // release（date，对应release_time）
}