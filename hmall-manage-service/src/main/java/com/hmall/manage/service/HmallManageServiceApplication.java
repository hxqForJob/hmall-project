package com.hmall.manage.service;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.hmall.manage.service.mapper")
@EnableTransactionManagement
@ComponentScan(basePackages = "com.hmall.service.util")//扫码工具类配置
@EnableDubbo
public class HmallManageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallManageServiceApplication.class, args);
    }

}
