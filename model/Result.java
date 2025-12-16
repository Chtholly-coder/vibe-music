package cn.edu.chtholly.model;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    // 成功响应
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 成功响应
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 失败响应
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMessage(message);
        return result;
    }
}