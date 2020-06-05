package com.hmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hmall.pojo.UserAddress;
import com.hmall.pojo.UserInfo;
import com.hmall.service.UserService;
import com.hmall.service.util.redisConfig.RedisKeyUtil;
import com.hmall.service.util.redisConfig.RedisUtil;
import com.hmall.user.service.mapper.UserAddressMapper;
import com.hmall.user.service.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserInfoImpl implements UserService {

    /**
     * 用户数据访问层
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户地址数据访问
     */
    @Autowired
    private UserAddressMapper userAddressMapper;

    /**
     * 注入redis 工具类
     */
    @Autowired
    private RedisUtil redisUtil;


    /**
     * 查询所有用户
     * @return
     */
    @Override
    public List<UserInfo> findAll() {
        List<UserInfo> userInfos = userMapper.selectAll();
        return  userInfos;
    }

    /**
     * 登录业务逻辑
     * @param userInfo
     * @return
     */
    @Override
    public UserInfo login(UserInfo userInfo) {
        String md5Pwd= DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(md5Pwd);
        UserInfo info = userMapper.selectOne(userInfo);
        if(info!=null){
            //添加到redis中
            String key= RedisKeyUtil.USERINFO_PREFIX+info.getId()+RedisKeyUtil.USERINFO_POSTFIX;
            String value= JSON.toJSONString(info);
            Jedis jedis=null;
            try {
             jedis=redisUtil.getJedis();
             jedis.setex(key,RedisKeyUtil.USERINFO_EXPIRE,value);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(jedis!=null){
                    jedis.close();
                }
            }
        }
        return  info;
    }

    /**
     * 根据id查询用户缓存
     * @param userId
     * @return
     */
    @Override
    public UserInfo getUserCacheById(String userId) {
        Jedis jedis=null;
        try {
            jedis=redisUtil.getJedis();
            String key=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.USERINFO_POSTFIX;
            String userStr = jedis.get(key);
            //如果没查到数据，说明过期或者未登录
            if(StringUtils.isEmpty(userStr)){
                return  null;
            }else {
                UserInfo info = JSON.parseObject(userStr, UserInfo.class);
                return  info;
            }
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    /**
     * 退出
     * @param userId
     */
    @Override
    public void logout(String userId) {
        Jedis jedis=null;
        String key=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.USERINFO_POSTFIX;
        try {
            jedis=redisUtil.getJedis();
            jedis.del(key);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    public List<UserAddress> getUserAddress(String userId){
        UserAddress userAddress=new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);
        return  userAddressList;
    }
}
