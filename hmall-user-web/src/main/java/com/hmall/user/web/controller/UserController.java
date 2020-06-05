package com.hmall.user.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hmall.pojo.UserInfo;
import com.hmall.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    public UserService userService;

    @RequestMapping("/findAll")
    public List<UserInfo> findAll()
    {
        return  userService.findAll();
    }
}
