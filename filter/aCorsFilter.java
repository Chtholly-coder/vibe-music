package cn.edu.chtholly.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("/*") // 覆盖所有请求路径
public class aCorsFilter implements Filter {

    // 允许的跨域源列表
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:8090",
            "http://localhost:8089"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse resp = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;

        // 1. 获取请求的 Origin 头（前端域名）
        String origin = req.getHeader("Origin");

        // 2. 动态设置 Access-Control-Allow-Origin：仅允许列表中的源
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        }

        // 3.  CORS 头
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Max-Age", "3600");

        // 4. 处理 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 5. 放行非 OPTIONS 请求
        chain.doFilter(request, response);
    }
}