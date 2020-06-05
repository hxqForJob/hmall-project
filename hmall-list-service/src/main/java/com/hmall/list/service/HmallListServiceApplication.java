package com.hmall.list.service;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@ComponentScan(basePackages = "com.hmall")//扫码工具类配置
public class HmallListServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallListServiceApplication.class, args);
    }

}
