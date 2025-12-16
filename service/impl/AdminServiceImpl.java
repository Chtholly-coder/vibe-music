package cn.edu.chtholly.service.impl;

import cn.edu.chtholly.dao.AdminDao;
import cn.edu.chtholly.dao.impl.AdminDaoImpl;
import cn.edu.chtholly.model.Result;
import cn.edu.chtholly.model.entity.Admin;
import cn.edu.chtholly.service.AdminService;
import cn.edu.chtholly.util.EncryptUtil;
import cn.edu.chtholly.util.RedisUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap; // 关键：使用LinkedHashMap保证顺序
import java.util.Map;

public class AdminServiceImpl implements AdminService {
    private final AdminDao adminDao = new AdminDaoImpl();

    // JWT 配置（与开源项目完全一致）
    private static final String SECRET_KEY = "VIBE_MUSIC"; // 必须与开源项目一致！
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 6; // 6小时过期

    @Override
    public Result<String> login(String username, String password) {
        // 1. 校验参数
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return Result.error("用户名或密码不能为空");
        }

        // 2. 查询管理员
        Admin admin = adminDao.selectByUsername(username.trim());
        if (admin == null) {
            return Result.error("用户名或密码错误");
        }

        // 3. 校验密码（MD5加密比对）
        String encryptedPassword = EncryptUtil.md5Encrypt(password.trim());
        if (!encryptedPassword.equals(admin.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        // 4. 生成 JWT Token（与项目完全一致）
        // 4.1 构建自定义声明（嵌套在 claims 字段中）
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("role", "ROLE_ADMIN");
        customClaims.put("adminId", admin.getId());
        customClaims.put("username", admin.getUsername());

        // 4.2 构建 JWT 头部（关键：使用LinkedHashMap保证顺序，alg在前，typ在后）
        Map<String, Object> headerClaims = new LinkedHashMap<>(); // 保证插入顺序
        headerClaims.put("alg", "HS256"); // alg 在前（必须显式设置）
        headerClaims.put("typ", "JWT");   // typ 在后

        // 4.3 生成 JWT Token（使用开源项目的密钥）
        String token = JWT.create()
                .withHeader(headerClaims) // 设置有序头部
                .withClaim("claims", customClaims) // 自定义声明嵌套在 claims 中
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
                .sign(Algorithm.HMAC256(SECRET_KEY)); // 使用正确密钥

        // 5. 返回结果
        return Result.success(token, "登录成功");
    }

    @Override
    public Result<Void> logout(String token) {
        // 1. 校验 token
        if (token == null || token.trim().isEmpty()) {
            return Result.error("令牌不能为空");
        }

        try {
            // 2. 将 token 加入 Redis 黑名单（过期时间与 JWT 一致：6小时）
            RedisUtil.addToBlacklist(token, 6 * 60 * 60);
            return Result.success(null, "登出成功");
        } catch (Exception e) {
            return Result.error("登出失败：" + e.getMessage());
        }
    }
}