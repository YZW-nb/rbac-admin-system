package com.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAsync
@MapperScan("com.admin.mapper")
@SpringBootApplication
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
        log.info("========================================");
        log.info("  RBAC 权限管理系统启动成功！");
        log.info("  接口文档: http://localhost:8080/doc.html");
        log.info("========================================");
    }
}
