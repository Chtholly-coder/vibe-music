package cn.edu.chtholly.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * 项目启动初始化监听器：强制设置Java程序默认时区为东八区
 */
@WebListener
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 强制设置Java虚拟机的默认时区为东八区（Asia/Shanghai）
        System.setProperty("user.timezone", "Asia/Shanghai");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 程序关闭时无需处理
    }
}