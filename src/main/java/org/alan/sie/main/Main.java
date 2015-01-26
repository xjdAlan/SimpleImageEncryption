package org.alan.sie.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Main {
    
    private static Logger logger = LogManager.getLogger(Main.class);

    private static FileSystemXmlApplicationContext appContext;
    
    public static void main(String[] args) {
        logger.info("准备启动Spring...");
        appContext = new FileSystemXmlApplicationContext("classpath:config.xml");
        logger.info("Spring启动成功...");

        EncryptionService encryptionService = appContext.getBean(EncryptionService.class);
        encryptionService.test();
        logger.info("所有任务已完成");
    }
}
