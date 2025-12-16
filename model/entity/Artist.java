package cn.edu.chtholly.model.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Artist {
    // 同时接受 "id" 和 "artistId" 作为参数名，最终都映射到这个字段
    @JsonAlias({"id", "artistId"})
    private Long id;             // id（bigint，主键）
    // 同时接受 "name" 和 "artistName" 作为参数名，最终都映射到这个字段
    @JsonAlias({"name", "artistName"})
    private String name;         // name（char100）
    private Integer gender;      // gender（int：0-男，1-女，2-组合/乐队）
    private String avatar;       // avatar（char255）
    private Date birth;          // birth（date）
    private String area;         // area（char30）
    private String introduction; // introduction（text）

}