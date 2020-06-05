package com.hmall.payment.web;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.hmall")
@EnableDubbo
public class HmallPaymentWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallPaymentWebApplication.class, args);
    }

}
