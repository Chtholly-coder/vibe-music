package cn.edu.chtholly.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

public class MailUtil {
    // 配置参数
    private static String SMTP_HOST;
    private static int SMTP_PORT;
    private static String FROM_EMAIL;
    private static String AUTH_CODE;
    private static int CONNECTION_TIMEOUT;
    private static int SMTP_TIMEOUT;
    private static String SENDER_NICKNAME;

    // 静态初始化：加载配置文件
    static {
        Properties props = new Properties();
        try (InputStream inputStream = MailUtil.class.getClassLoader().getResourceAsStream("mail.properties")) {
            // 校验配置文件是否存在
            if (inputStream == null) {
                throw new RuntimeException("未找到mail.properties配置文件，请检查src/main/resources目录下是否存在");
            }
            // 加载配置（指定UTF-8编码，避免中文昵称乱码）
            props.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("加载mail.properties配置失败：" + e.getMessage(), e);
        }

        // 读取核心配置项
        FROM_EMAIL = props.getProperty("mail.from.email");
        AUTH_CODE = props.getProperty("mail.auth.code");
        if (FROM_EMAIL == null || FROM_EMAIL.isBlank()) {
            throw new RuntimeException("mail.from.email（发件人邮箱）未配置");
        }
        if (AUTH_CODE == null || AUTH_CODE.isBlank()) {
            throw new RuntimeException("mail.auth.code（邮箱授权码）未配置");
        }

        // 读取SMTP服务器配置（
        SMTP_HOST = props.getProperty("mail.smtp.host", "smtp.qq.com"); // 默认QQ邮箱SMTP

        // 端口校验：必须是1-65535的整数
        try {
            SMTP_PORT = Integer.parseInt(props.getProperty("mail.smtp.port", "465"));
            if (SMTP_PORT < 1 || SMTP_PORT > 65535) {
                throw new IllegalArgumentException("SMTP端口必须在1-65535之间");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("mail.smtp.port配置格式错误，必须是整数", e);
        }

        // 读取超时配置
        try {
            CONNECTION_TIMEOUT = Integer.parseInt(props.getProperty("mail.smtp.connection.timeout", "5000"));
            SMTP_TIMEOUT = Integer.parseInt(props.getProperty("mail.smtp.timeout", "3000"));
            if (CONNECTION_TIMEOUT <= 0 || SMTP_TIMEOUT <= 0) {
                throw new IllegalArgumentException("超时时间必须大于0");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("超时配置格式错误，必须是整数", e);
        }

        // 读取发件人昵称
        SENDER_NICKNAME = props.getProperty("mail.sender.nickname", "VibeMusic官方");
    }

    // 1. 注册验证码邮件
    public static boolean sendRegisterCode(String toEmail, String code) {
        return sendEmail(toEmail, code, "注册");
    }

    // 2. 重置密码验证码邮件
    public static boolean sendResetPwdCode(String toEmail, String code) {
        return sendEmail(toEmail, code, "重置密码");
    }

    // 统一发送逻辑
    private static boolean sendEmail(String toEmail, String code, String scene) {
        Properties props = getSmtpProps();
        Session session = getSession(props);
        try {
            MimeMessage message = new MimeMessage(session);
            // 发件人：使用配置的邮箱和昵称
            message.setFrom(new InternetAddress(FROM_EMAIL, SENDER_NICKNAME, "UTF-8"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            String subject = "【VibeMusic】" + scene + "验证码";
            message.setSubject(subject, "UTF-8");

            // 修复时区
            SimpleDateFormat rfc822Format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
            rfc822Format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            message.setHeader("Date", rfc822Format.format(new Date()));

            // 场景化正文
            String content = getSceneContent(code, scene);
            message.setContent(content, "text/html;charset=UTF-8");
            message.saveChanges();
            Transport.send(message);
            return true;
        } catch (Exception e) {
            System.err.println(scene + "验证码发送失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 场景化正文
    private static String getSceneContent(String code, String scene) {
        if ("注册".equals(scene)) {
            return String.format("""
                <div style="font-family: 'Microsoft YaHei', Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #1E90FF; font-size: 18px; margin-bottom: 20px;">VibeMusic账号注册</h2>
                    <p style="font-size: 14px; color: #333; line-height: 1.6;">
                        您好！您正在注册VibeMusic账号，您的验证码是：
                    </p>
                    <div style="font-size: 24px; font-weight: bold; color: #1E90FF; margin: 15px 0;">
                        %s
                    </div>
                    <p style="font-size: 12px; color: #999;">
                        验证码5分钟内有效，请尽快完成注册，请勿泄露给他人。
                    </p>
                </div>
                """, code);
        } else {
            return String.format("""
                <div style="font-family: 'Microsoft YaHei', Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #FF6347; font-size: 18px; margin-bottom: 20px;">VibeMusic密码重置</h2>
                    <p style="font-size: 14px; color: #333; line-height: 1.6;">
                        您好！您正在申请重置VibeMusic账号密码，您的验证码是：
                    </p>
                    <div style="font-size: 24px; font-weight: bold; color: #FF6347; margin: 15px 0;">
                        %s
                    </div>
                    <p style="font-size: 12px; color: #999;">
                        验证码5分钟内有效，请勿泄露给他人。如非本人操作，请忽略此邮件。
                    </p>
                </div>
                """, code);
        }
    }

    // 公共SMTP配置
    private static Properties getSmtpProps() {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.connectiontimeout", CONNECTION_TIMEOUT); // 从配置读取
        props.put("mail.smtp.timeout", SMTP_TIMEOUT); // 从配置读取
        return props;
    }

    // 公共Session创建（
    private static Session getSession(Properties props) {
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, AUTH_CODE);
            }
        });
    }
}