package com.hmall.cart.web;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo //开启Dubbo
@ComponentScan(basePackages = "com.hmall") //可以添加拦截器
public class HmallCartWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallCartWebApplication.class, args);
    }

}
