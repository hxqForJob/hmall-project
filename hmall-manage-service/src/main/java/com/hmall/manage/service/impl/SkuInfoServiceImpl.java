package com.hmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hmall.manage.service.mapper.SkuAttrValueMapper;
import com.hmall.manage.service.mapper.SkuImageMapper;
import com.hmall.manage.service.mapper.SkuInfoMapper;
import com.hmall.manage.service.mapper.SkuSaleAttrValueMapper;
import com.hmall.pojo.*;
import com.hmall.service.SkuInfoService;
import com.hmall.service.util.redisConfig.RedisKeyUtil;
import com.hmall.service.util.redisConfig.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * sku业务逻辑实现
 */
@Service
public class SkuInfoServiceImpl implements SkuInfoService {

    /**
     * 定义ES库
     */
    public static final String ES_INDEX="hmall_index";

    /**
     * 定义Es表
     */
    public static final String ES_TYPE="SkuInfo";


    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    //注入redis工具类
    @Autowired
    private RedisUtil redisUtil;

    //注入redissonClient
    @Autowired
    private RedissonClient redissonClient;

    //注入JestClient用来操作es
    @Autowired
    private JestClient jestClient;

    /**
     * 添加Sku
     * @param skuInfo
     * @return
     */
    @Override
    @Transactional
    public boolean saveSkuInfo(SkuInfo skuInfo) {
        try {
            //获取sku下的图片
            List<SkuImage> skuImageList = skuInfo.getSkuImageList();
            //获取sku下的平台属性值
            List<SkuAttrValue> skuAtrrValueList = skuInfo.getSkuAttrValueList();
            //获取sku下的销售属性值
            List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            //添加skuinfo
            skuInfoMapper.insertSelective(skuInfo);
            //添加skuimage
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
            //添加skuattrvalue
            for (SkuAttrValue skuAttrValue : skuAtrrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
            //添加skusaleattrvalue
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
           return  true;
        } catch (Exception e) {
            e.printStackTrace();
            return  false;
        }
    }

    /**
     * 根据Id查询SkuInfo
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoById(Integer skuId) {
      return  skuInfoMapper.selectByPrimaryKey(skuId.toString());
    }

    /**
     * 根据Id查询SkuInfo和图片
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoAndImageById(Integer skuId) {
        //获取redis中skuinfo的key
        String key= RedisKeyUtil.SKUINFO_PREFIX+skuId+RedisKeyUtil.SKUINFO_POSTFIX;
        Jedis jedis = null;
        SkuInfo skuInfo=null;
        //skuinfoJson字符串
        String skuInfoStr;
        //redis分布式锁
        RLock redisLock=null;
        try {
            jedis=redisUtil.getJedis();

                if(jedis.exists(key)){
                    //redis存在,从redis取
                    skuInfoStr = jedis.get(key);
                    skuInfo=JSON.parseObject(skuInfoStr,SkuInfo.class);
                    //System.out.println("其他线程从redis取");
                }else {
                    //不存在，从数据库取，并放入缓存
                    //获取锁
                    redisLock = redissonClient.getLock("skuInfoLock");
                    //上锁，避免缓存击穿
                    redisLock.lock(10, TimeUnit.SECONDS);
                    //后面阻塞的线程判断当前redis是否有数据
                    if(jedis.exists(key)){
                        //redis存在,从redis取
                        skuInfoStr = jedis.get(key);
                        skuInfo=JSON.parseObject(skuInfoStr,SkuInfo.class);
                    }else {
                        skuInfo=skuInfoMapper.getSkuInfoAndImageById(skuId);
                        //如果数据也为空，将过期时间设置为12个小时
                        if(skuInfo==null){
                            jedis.setex(key,1*60*60,"null");
                            // System.out.println("从数据库取,数据库为null");
                        }else {
                            //将从数据库得到数据存放到redis中
                            skuInfoStr= JSON.toJSONString(skuInfo);
                            jedis.setex(key,RedisKeyUtil.SKUINFO_EXPIRE,skuInfoStr);
                            //System.out.println("从数据库取");
                        }
                    }
                }
        }catch (Exception e){
            e.printStackTrace();
            skuInfo=skuInfoMapper.getSkuInfoAndImageById(skuId);
           // System.out.println("redis发生异常");
        }finally {
            if(jedis!=null){
                jedis.close();
            }
            if(redisLock!=null){
                //释放锁
                redisLock.unlock();
            }

        }
        return  skuInfo;
    }

    /**
     * 获取当前Spu下所有的sku销售属性值
     * @param spuId
     * @return
     */
    @Override
    public List<SkuSaleAttrValue> getAllSkuSaleAttrValueCom(String spuId) {
        return skuSaleAttrValueMapper.getBySpuId(Integer.valueOf(spuId));
    }

    /**
     * 上架Sku
     * @param skuId
     */
    @Override
    public void onSale(String skuId) {
        //根据SkuId查询SkuInfo包括图片
        SkuInfo skuInfo = skuInfoMapper.getSkuInfoAndImageById(Integer.valueOf(skuId));
        //根据SkuId查询Sku对应的平台属性值
        SkuAttrValue skuAttrValue=new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        //将Sku平台属性值放入SkuInfo中
        skuInfo.setSkuAttrValueList(skuAttrValueList);
        //实例化一个保存至es中的SkuInfo
        SkuLsInfo  skuLsInfo=new SkuLsInfo();
        //拷贝对象
        BeanUtils.copyProperties(skuInfo,skuLsInfo);
        //System.out.println(skuLsInfo);
        if(skuLsInfo!=null){
            //存入es业务逻辑
            saveSkuLsInfo(skuLsInfo);
        }
    }

    /**
     * 向es添加skuinfo
     * @param skuLsInfo
     */
    private void saveSkuLsInfo(SkuLsInfo skuLsInfo) {
        try {
            Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
            DocumentResult result = jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
