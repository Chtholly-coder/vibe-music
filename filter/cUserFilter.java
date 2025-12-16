package cn.edu.chtholly.filter;

import cn.edu.chtholly.util.JwtUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * 对于已登录的用户设置，userID
 */
@WebFilter(urlPatterns = {
        "/*"
})
public class cUserFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("application/json;charset=UTF-8");

        try {
            String token = req.getHeader("Authorization");

            // 1. 解析令牌获取userId
            Map<String, Object> claims = JwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.get("userId").toString());
            UserThreadLocal.setUserId(userId);

            // 2. 验证通过，放行请求
            chain.doFilter(request, response);
        }
        // 用户未登录
       catch (Exception e) {
           chain.doFilter(request, response);
        } finally {
            UserThreadLocal.remove(); // 清除ThreadLocal，避免内存泄漏
        }
    }
}