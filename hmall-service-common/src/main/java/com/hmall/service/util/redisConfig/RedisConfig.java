package com.hmall.service.util.redisConfig;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    //读取配置文件中的redis的ip地址
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.lockRedis.host:disabled}")
    private  String lockHost;

    @Value("${spring.lockRedis.port}")
    private  String lockPort;

    /**
     * 获取Redis工具类
     * @return
     */
    @Bean
    public RedisUtil getRedisUtil() {
        if (host.equals("disabled")) {
            return null;
        }
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initJedisPool(host, port, database);
        return redisUtil;

    }

    /**
     * 获取RedissonClient,用来创建分布式锁
     */
    @Bean
    public RedissonClient rLock(){
        if (lockHost.equals("disabled")) {
            return null;
        }
        Config redissonConfig=new Config();
        redissonConfig.useSingleServer().setAddress("redis://"+lockHost+":"+lockPort);
        RedissonClient redissonClient = Redisson.create(redissonConfig);
        return  redissonClient;
    }

}
