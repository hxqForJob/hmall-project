package com.hmall.item.web;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@ComponentScan(basePackages = "com.hmall") //扫描webutil 认证拦截器
public class HmallItemWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallItemWebApplication.class, args);
    }

}
