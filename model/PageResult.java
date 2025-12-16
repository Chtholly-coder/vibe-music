package cn.edu.chtholly.model;

import lombok.Data;
import java.util.List;

/**
 * 分页查询结果封装
 */
@Data
public class PageResult<T> {
    private Long total; // 总条数
    private List<T> items; // 当前页数据
}