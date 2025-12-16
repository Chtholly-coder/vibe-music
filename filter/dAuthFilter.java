package cn.edu.chtholly.filter;

import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.util.JwtUtil;
import cn.edu.chtholly.util.RedisUtil;
import cn.edu.chtholly.util.ServletUtil;
import cn.edu.chtholly.util.UserThreadLocal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 登录验证拦截器
 */
@WebFilter(urlPatterns = {
        "/feedback/addFeedback", // 意见反馈
        "/favorite/cancelCollectPlaylist", // 取消收藏歌单
        "/favorite/collectPlaylist", // 收藏歌单
        "/user/updateUserAvatar", // 更新头像
        "/comment/*", // 评论相关
        "/user/getUserInfo", // 用户信息
        "/favorite/collectSong",       // 收藏歌曲接口
        "/favorite/cancelCollectSong", // 取消收藏接口
        "/favorite/getFavoriteSongs",  // 获取收藏列表接口
        "/user/updateUserInfo", // 更新用户信息需登录
        "/user/deleteAccount"  // 删除账号需登录

})
public class dAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("application/json;charset=UTF-8");

        try {
            String token = req.getHeader("Authorization");
            // 1. 令牌不存在/为空：返回未登录错误
            if (token == null || token.trim().isEmpty()) {
                ServletUtil.writeJson(resp, Result.error("请先登录"));
                return;
            }

            // 2. 检查令牌是否在黑名单（登出后失效）
            if (RedisUtil.isInBlacklist(token)) {
                ServletUtil.writeJson(resp, Result.error("登录已失效，请重新登录"));
                return;
            }

            // 3. 解析令牌获取userId
            Map<String, Object> claims = JwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.get("userId").toString());
            UserThreadLocal.setUserId(userId);

            // 4. 验证通过，放行请求
            chain.doFilter(request, response);
        }
        // 令牌过期/格式错误/签名错误：返回登录相关错误
        catch (ExpiredJwtException e) {
            ServletUtil.writeJson(resp, Result.error("令牌已过期，请重新登录"));
        } catch (MalformedJwtException | SignatureException e) {
            ServletUtil.writeJson(resp, Result.error("令牌无效，请重新登录"));
        } catch (Exception e) {
            ServletUtil.writeJson(resp, Result.error("登录状态验证失败，请重新登录"));
        } finally {
            UserThreadLocal.remove(); // 清除ThreadLocal，避免内存泄漏
        }
    }
}