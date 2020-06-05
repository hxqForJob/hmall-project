package com.hmall.cart.service;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.hmall.cart.service.mapper") //Mapper扫描
@ComponentScan(basePackages = "com.hmall.service.util")// 扫描工具类组件
@EnableDubbo// 开启Dubbo
public class HmallCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallCartServiceApplication.class, args);
    }

}
