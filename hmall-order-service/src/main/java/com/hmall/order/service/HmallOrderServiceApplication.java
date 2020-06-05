package com.hmall.order.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.hmall.order.service.mapper")
@ComponentScan(basePackages = "com.hmall")// 扫描工具类组件
@EnableTransactionManagement
public class HmallOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallOrderServiceApplication.class, args);
    }

}
