package com.hmall.order.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.hmall")
public class HmallOrderWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmallOrderWebApplication.class, args);
    }

}
