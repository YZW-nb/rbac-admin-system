package com.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j（Swagger）接口文档配置
 * 仅在 knife4j.enable=true 时生效，生产环境通过 application-prod.yml 关闭
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true", matchIfMissing = false)
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        log.warn(" Knife4j 接口文档已启用，生产环境请确保已关闭！");
        return new OpenAPI()
                .info(new Info()
                        .title("RBAC 权限管理系统 API")
                        .description("基于 Spring Boot 3.2 + MyBatis-Plus + JWT 的权限管理系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("admin")
                                .email("admin@example.com")));
    }
}
