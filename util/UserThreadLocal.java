package cn.edu.chtholly.util;

/**
 * 线程本地存储
 */
public class UserThreadLocal {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    // 设置用户ID
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    // 获取用户ID
    public static Long getUserId() {
        return USER_ID.get();
    }

    // 清除存储
    public static void remove() {
        USER_ID.remove();
    }
}