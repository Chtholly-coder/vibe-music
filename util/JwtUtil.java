package cn.edu.chtholly.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.io.InputStream;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class JwtUtil {
    private static final Key SECRET_KEY; // HMAC256密钥
    private static final long EXPIRATION_HOURS = 6; // 令牌有效期6小时

    static {
        try {
            // 加载配置文件（resources/jwt.properties）
            InputStream is = JwtUtil.class.getClassLoader().getResourceAsStream("jwt.properties");
            Properties props = new Properties();
            props.load(is);
            String secret = props.getProperty("jwt.secret");
            SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("初始化JWT密钥失败", e);
        }
    }

    // 生成JWT令牌（HMAC256签名）
    public static String generateToken(Map<String, Object> claims) {
        long expireTime = System.currentTimeMillis() + EXPIRATION_HOURS * 60 * 60 * 1000;
        // 建造者模式
        return Jwts.builder()
                .setClaims(claims) // 自定义载荷（包含userId、username等）
                .setExpiration(new Date(expireTime)) // 过期时间
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // HMAC256签名
                .compact();
    }

    // 解析JWT令牌（验证签名并获取载荷）
    public static Map<String, Object> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // 用相同密钥验证签名
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}