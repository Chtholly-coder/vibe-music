package cn.edu.chtholly.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 解析请求体JSON为Java对象
    public static <T> T parseJson(HttpServletRequest req, Class<T> clazz) throws IOException {
        return objectMapper.readValue(req.getInputStream(), clazz);
    }

    // 解析泛型列表
    public static <T> T parseJson(HttpServletRequest req, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(req.getInputStream(), typeReference);
    }

    // 将对象转换为JSON写入响应
    public static void writeJson(HttpServletResponse resp, Object obj) throws IOException {
        resp.getWriter().write(objectMapper.writeValueAsString(obj));
    }
}