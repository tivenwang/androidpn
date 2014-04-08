/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.onlan.servlet;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;

/**
 * Web application lifecycle listener.
 * 监听器用于初始化定时任务
 */
public class TimeTaskServletListener implements ServletContextListener {

    private static Logger log = Logger.getLogger(TimeTaskServletListener.class);
    private ScheduledThreadPoolExecutor timeTask = null;
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
        timeTask = new ScheduledThreadPoolExecutor(10);
        /**
         * 与终端定时握手
         */
        timeTask.scheduleAtFixedRate(new UserBroadCastRunnable(), 0, 60, TimeUnit.SECONDS);
        timeTask.scheduleAtFixedRate(new UserLoginRunnable(), 0, 3, TimeUnit.SECONDS);
        log.warn("UserBroadCastRunnable:定时任务:终端定时握手");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        timeTask.shutdown();
    }
    
}
