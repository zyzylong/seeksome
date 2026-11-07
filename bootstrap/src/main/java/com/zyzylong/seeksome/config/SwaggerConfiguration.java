package com.zyzylong.seeksome.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口测试文档相关配置
 */
@Slf4j
@Configuration
public class SwaggerConfiguration implements ApplicationRunner {

    @Value("${server.port:8080}")
    private String serverPort;
    @Value("${server.servlet.context-path:}")
    private String contextPath;


    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info() // 基本信息配置
                        .title("SeekSome") // 标题
                        .description("用户端以及管理端接口测试") // 描述 Api 接口文档的基本信息
                        .version("v1.0.0") // 版本
                        .contact(new Contact().name("zyzylong").email("zyzylong@126.com"))
                        // 设置 OpenAPI 文档的许可证信息，包括许可证名称和许可证URL
//                        .license(new License().name("").url(""))
                );
    }



    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("API测试地址: http://127.0.0.1:{}{}/doc.html", serverPort, contextPath);
    }
}
