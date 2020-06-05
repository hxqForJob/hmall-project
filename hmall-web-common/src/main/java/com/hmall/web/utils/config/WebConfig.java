package com.hmall.web.utils.config;

import com.hmall.web.utils.intercept.AuthIntercepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 配置WebConfig,web.xml
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    /**
     * 注入认证拦截器
     */
    @Autowired
    private AuthIntercepter authIntercepter;

    /**
     * 添加拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authIntercepter).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
