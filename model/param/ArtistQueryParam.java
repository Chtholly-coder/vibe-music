package cn.edu.chtholly.model.param;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistQueryParam {
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    // 同时接受 "name" 和 "artistName" 作为参数名，最终都映射到这个字段
    @JsonAlias({"name", "artistName"})
    private String Name;

    private Integer gender;
    private String area;



    public String getName(){
        return Name;
    }
}
