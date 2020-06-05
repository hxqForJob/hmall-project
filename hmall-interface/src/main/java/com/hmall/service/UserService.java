package com.hmall.service;

import com.hmall.pojo.UserAddress;
import com.hmall.pojo.UserInfo;

import java.util.List;

/**
 * 后台管理用户业务逻辑接口
 */
public interface UserService {

    /**
     * 查询所有
     * @return
     */
    List<UserInfo> findAll();


    /**
     * 登录
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 根据Id查询redis中的用户
     * @param userId
     * @return
     */
    UserInfo getUserCacheById(String userId);

    /**
     * 退出业务逻辑
     * @param userId
     */
    void logout(String userId);


    /**
     * 获取用户地址
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddress(String userId);


}
