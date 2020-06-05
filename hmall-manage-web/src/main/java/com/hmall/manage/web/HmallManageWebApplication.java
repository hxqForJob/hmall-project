package com.hmall.manage.web;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class HmallManageWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallManageWebApplication.class, args);
    }

}
