package cn.edu.chtholly.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;
import java.util.*;


public class RedisUtil {
    // 配置参数
    private static String REDIS_HOST;
    private static int REDIS_PORT;
    private static String REDIS_PASSWORD;
    private static int REDIS_DATABASE;
    private static int CONNECTION_TIMEOUT;

    // Lettuce 核心组件
    private static final RedisClient redisClient;
    private static final ClientResources clientResources;

    // 静态初始化：加载配置文件 + 初始化Redis客户端
    static {
        // 1. 加载redis.properties配置文件
        Properties props = new Properties();
        try (InputStream inputStream = RedisUtil.class.getClassLoader().getResourceAsStream("redis.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("未找到redis.properties配置文件，请检查resources目录下是否存在该文件");
            }
            props.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("加载redis.properties配置文件失败：" + e.getMessage(), e);
        }

        // 2. 读取配置项
        REDIS_HOST = props.getProperty("redis.host", "localhost");
        REDIS_PASSWORD = props.getProperty("redis.password", "");

        // 端口：必须是合法整数
        try {
            REDIS_PORT = Integer.parseInt(props.getProperty("redis.port", "6379"));
            if (REDIS_PORT < 1 || REDIS_PORT > 65535) {
                throw new IllegalArgumentException("Redis端口必须在1-65535之间");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("redis.port配置项格式错误，必须是整数", e);
        }

        // 数据库索引
        try {
            REDIS_DATABASE = Integer.parseInt(props.getProperty("redis.database", "0"));
            if (REDIS_DATABASE < 0 || REDIS_DATABASE > 15) {
                throw new IllegalArgumentException("Redis数据库索引必须在0-15之间");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("redis.database配置项格式错误，必须是整数", e);
        }

        // 连接超时
        try {
            CONNECTION_TIMEOUT = Integer.parseInt(props.getProperty("redis.connection.timeout", "3000"));
            if (CONNECTION_TIMEOUT <= 0) {
                throw new IllegalArgumentException("连接超时时间必须大于0");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("redis.connection.timeout配置项格式错误，必须是整数", e);
        }

        // 3. 初始化Lettuce连接资源
        clientResources = DefaultClientResources.create();

        // 4. 构建Redis连接URI
        RedisURI.Builder builder = RedisURI.builder()
                .withHost(REDIS_HOST)
                .withPort(REDIS_PORT)
                .withDatabase(REDIS_DATABASE)
                .withTimeout(Duration.ofMillis(CONNECTION_TIMEOUT));

        // 密码不为空时才设置认证
        if (REDIS_PASSWORD != null && !REDIS_PASSWORD.isBlank()) {
            builder.withPassword(REDIS_PASSWORD.toCharArray());
        }

        RedisURI redisURI = builder.build();

        // 5. 创建Redis客户端
        redisClient = RedisClient.create(clientResources, redisURI);
    }

    /**
     * 将令牌加入黑名单
     */
    public static void addToBlacklist(String token, int expireSeconds) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("令牌不能为空");
        }
        if (expireSeconds <= 0) {
            throw new IllegalArgumentException("过期时间必须大于0");
        }

        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = redisClient.connect();
            RedisCommands<String, String> commands = connection.sync();
            commands.setex("blacklist:" + token, expireSeconds, "1");
        } catch (Exception e) {
            throw new RuntimeException("Redis添加黑名单失败：" + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * 检查令牌是否在黑名单
     */
    public static boolean isInBlacklist(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = redisClient.connect();
            RedisCommands<String, String> commands = connection.sync();
            return commands.exists("blacklist:" + token) > 0;
        } catch (Exception e) {
            throw new RuntimeException("Redis查询黑名单失败：" + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * 存储键值对（带过期时间，用于验证码存储）
     * @param key 键（如 "verify_code:邮箱"）
     * @param value 验证码
     * @param expireSeconds 过期时间（秒）
     */
    public static void setex(String key, String value, int expireSeconds) {
        if (key == null || key.isBlank() || value == null || value.isBlank()) {
            throw new IllegalArgumentException("键或值不能为空");
        }
        if (expireSeconds <= 0) {
            throw new IllegalArgumentException("过期时间必须大于0");
        }

        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = redisClient.connect();
            RedisCommands<String, String> commands = connection.sync();
            commands.setex(key, expireSeconds, value);
        } catch (Exception e) {
            throw new RuntimeException("Redis存储失败：" + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * 获取键对应的值（用于获取验证码）
     * @param key 键（如 "verify_code:邮箱"）
     * @return 存储的值（验证码），不存在返回null
     */
    public static String get(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = redisClient.connect();
            RedisCommands<String, String> commands = connection.sync();
            return commands.get(key);
        } catch (Exception e) {
            throw new RuntimeException("Redis查询失败：" + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * 删除键（验证码使用后删除）
     * @param key 键（如 "verify_code:邮箱"）
     */
    public static void del(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        StatefulRedisConnection<String, String> connection = null;
        try {
            connection = redisClient.connect();
            RedisCommands<String, String> commands = connection.sync();
            commands.del(key);
        } catch (Exception e) {
            throw new RuntimeException("Redis删除失败：" + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * 关闭资源（程序终止时调用）
     */
    public static void close() {
        try {
            redisClient.shutdown();
            clientResources.shutdown();
        } catch (Exception e) {
            throw new RuntimeException("Redis资源关闭失败：" + e.getMessage(), e);
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    //  Set 类型（用户收藏songId列表）
    public static void sAdd(String key, String... values) {
        if (key == null || key.isBlank() || values == null || values.length == 0) return;
        StatefulRedisConnection<String, String> conn = null;
        try {
            conn = redisClient.connect();
            conn.sync().sadd(key, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.close();
        }
    }

    public static void sRem(String key, String... values) {
        if (key == null || key.isBlank() || values == null || values.length == 0) return;
        StatefulRedisConnection<String, String> conn = null;
        try {
            conn = redisClient.connect();
            conn.sync().srem(key, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.close();
        }
    }

    public static Set<String> sMembers(String key) {
        if (key == null || key.isBlank()) return Collections.emptySet();
        StatefulRedisConnection<String, String> conn = null;
        try {
            conn = redisClient.connect();
            return conn.sync().smembers(key);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        } finally {
            if (conn != null) conn.close();
        }
    }

    //  ZSet 类型（热门歌曲）
    public static void zIncrBy(String key, double increment, String member) {
        if (key == null || key.isBlank() || member == null || member.isBlank()) return;
        StatefulRedisConnection<String, String> conn = null;
        try {
            conn = redisClient.connect();
            conn.sync().zincrby(key, increment, member);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.close();
        }
    }

    /**
     * 缓存完整的歌曲VO对象（FavoriteSongItemVO），过期时间30分钟
     */
    public static void setSongVO(Long songId, Object songVO) {
        if (songId == null || songVO == null) return;
        String key = "song:vo:" + songId;
        StatefulRedisConnection<String, String> conn = null;
        try {
            String json = objectMapper.writeValueAsString(songVO);
            conn = redisClient.connect();
            conn.sync().setex(key, 1800, json); // 30分钟过期，避免数据不一致
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.close();
        }
    }

    /**
     * 从Redis获取完整的歌曲VO对象
     */
    public static <T> T getSongVO(Long songId, Class<T> clazz) {
        if (songId == null || clazz == null) return null;
        String key = "song:vo:" + songId;
        StatefulRedisConnection<String, String> conn = null;
        try {
            conn = redisClient.connect();
            String json = conn.sync().get(key);
            if (json == null || json.isBlank()) return null;
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) conn.close();
        }
    }

    /**
     * 删除指定songId的VO缓存（取消收藏时调用）
     */
    public static void delSongVO(Long songId) {
        if (songId == null) return;
        String key = "song:vo:" + songId;
        StatefulRedisConnection<String, String> conn = null;
        try {
            conn = redisClient.connect();
            conn.sync().del(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.close();
        }
    }

    // 从ZSet中删除指定成员
    public static void zRem(String key, String... members) {
        if (key == null || key.isBlank() || members == null || members.length == 0) {
            return;
        }
        StatefulRedisConnection<String, String> conn = null;
        try {
            conn = redisClient.connect();
            conn.sync().zrem(key, members); // 执行ZSet删除操作
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    // 获取ZSet中指定成员的分数
    public static Double zScore(String key, String member) {
        if (key == null || key.isBlank() || member == null || member.isBlank()) {
            return 0.0; // 入参非法时返回默认值
        }
        StatefulRedisConnection<String, String> conn = null;
        try {
            conn = redisClient.connect();
            // 调用Redis的zscore命令，获取成员分数
            return conn.sync().zscore(key, member);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0; // 异常时返回默认值
        } finally {
            if (conn != null) {
                conn.close(); // 释放连接
            }
        }
    }
}