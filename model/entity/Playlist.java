package cn.edu.chtholly.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Playlist {
    @JsonProperty("playlistId")  // 映射前端传入的playlistId到实体的id
    private Long id;             // id
    private String title;        // title
    private String coverUrl;     // cover_url
    private String introduction; // introduction
    private String style;        // style
}