package com.zyzylong.seeksome.framework.security;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 用户端 Sa-Token 拦截配置，注册与登录接口放行，其余认证接口统一校验用户登录态。
 */
@Configuration
public class AppSaTokenConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/app/auth/**")
                .excludePathPatterns("/app/auth/login", "/app/auth/register");
    }
}
