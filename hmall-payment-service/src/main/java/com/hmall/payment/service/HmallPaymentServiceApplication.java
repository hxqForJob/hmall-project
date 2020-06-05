package com.hmall.payment.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.hmall.payment.service.mapper")
@ComponentScan(basePackages = "com.hmall")
public class HmallPaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallPaymentServiceApplication.class, args);
    }

}
