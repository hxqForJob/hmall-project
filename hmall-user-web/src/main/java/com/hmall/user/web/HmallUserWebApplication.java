package com.hmall.user.web;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class HmallUserWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallUserWebApplication.class, args);
    }

}
