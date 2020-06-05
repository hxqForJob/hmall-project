package com.hmall.user.service;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.hmall.user.service.mapper")
@ComponentScan(basePackages = "com.hmall.service.util")
@EnableDubbo
public class HmallUserManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallUserManageApplication.class, args);
    }

}
