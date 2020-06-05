package com.hmall.list.web;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@ComponentScan(basePackages = "com.hmall.web.utils") //扫描webutil 认证拦截器
public class HmallListWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallListWebApplication.class, args);
    }

}
