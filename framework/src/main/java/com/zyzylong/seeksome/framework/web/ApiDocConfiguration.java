package com.zyzylong.seeksome.framework.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置，声明文档元信息并输出访问地址。
 */
@Slf4j
@Configuration
public class ApiDocConfiguration implements ApplicationRunner {

    private static final String TITLE = "SeekSome";

    private static final String DESCRIPTION = "用户端以及管理端接口测试";

    private static final String VERSION = "v1.0.0";

    private static final String CONTACT_NAME = "zyzylong";

    private static final String CONTACT_EMAIL = "zyzylong@126.com";

    private final String serverPort;

    private final String contextPath;

    public ApiDocConfiguration(@Value("${server.port:8080}") String serverPort,
                               @Value("${server.servlet.context-path:}") String contextPath) {
        this.serverPort = serverPort;
        this.contextPath = contextPath;
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(TITLE)
                        .description(DESCRIPTION)
                        .version(VERSION)
                        .contact(new Contact()
                                .name(CONTACT_NAME)
                                .email(CONTACT_EMAIL)));
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("接口文档地址: http://127.0.0.1:{}{}/doc.html", serverPort, contextPath);
    }
}
