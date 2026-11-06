package com.zyzylong.seeksome.admin.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.zyzylong.seeksome.admin.auth.StpAdminUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 管理端 Sa-Token 拦截配置，登录接口放行，其余管理接口统一校验登录态。
 */
@Configuration
public class AdminSaTokenConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpAdminUtil.checkLogin()))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/login");
    }
}
