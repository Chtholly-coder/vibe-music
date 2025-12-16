package cn.edu.chtholly.util;

import java.util.Random;

/**
 * 随机验证码生成工具（6位混合字符：数字+大小写字母）
 */
public class VerificationCodeUtil {
    private static final Random RANDOM = new Random();
    private static final int CODE_LENGTH = 6;
    // 字符池：数字0-9 + 大写字母A-Z + 小写字母a-z（排除易混淆字符：0/O、1/I、l）
    private static final String CHAR_POOL = "23456789ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";

    /**
     * 生成6位混合字符验证码
     */
    public static String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            // 从字符池随机取一个字符
            int randomIndex = RANDOM.nextInt(CHAR_POOL.length());
            code.append(CHAR_POOL.charAt(randomIndex));
        }
        return code.toString();
    }
}