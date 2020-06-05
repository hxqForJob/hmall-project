package com.hmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hmall.cart.service.mapper.CartInfoMapper;
import com.hmall.pojo.CartInfo;
import com.hmall.pojo.SkuInfo;
import com.hmall.service.CartService;
import com.hmall.service.SkuInfoService;
import com.hmall.service.util.redisConfig.RedisKeyUtil;
import com.hmall.service.util.redisConfig.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车业务逻辑实现
 */
@Service
public class CartServiceImpl implements CartService {

    /**
     * 注入redis工具类
     */
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 注入skuInfo业务逻辑
     */
    @Reference
    private SkuInfoService skuInfoService;

    /**
     * 购物车Mapper
     */
    @Autowired
    private CartInfoMapper cartInfoMapper;


    /**
     * 添加购物车
     * @param userId 用户Id
     * @param skuId skuId
     * @param skuNum 数量
     */
    @Override
    public void addCartInfo(String userId, String skuId,Integer skuNum) {
        Jedis jedis=null;
        String key= RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.CART_POSTFIX;
        String checkKey=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.CART_CHECKED;
        CartInfo cartInfo=new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        try {
            CartInfo cart = cartInfoMapper.selectOne(cartInfo);
            jedis = redisUtil.getJedis();
            //购物车存在，增加数量,更数据库购物车，缓存购物车
            if(cart!=null){
                cart.setSkuNum(cart.getSkuNum()+skuNum);
                cartInfoMapper.updateByPrimaryKeySelective(cart);
                //获取购物车缓存
                String cartInfoStr = jedis.hget(key, skuId);
                //如果缓存有数据,更新缓存，否则添加缓存
                if(!StringUtils.isEmpty(cartInfoStr)){
                    //更新缓存
                    cart=JSON.parseObject(cartInfoStr,CartInfo.class);
                    cart.setSkuNum(cart.getSkuNum()+skuNum);
                    jedis.hset(key,cart.getSkuId(),JSON.toJSONString(cart));
                    //判断当前添加的sku是否已经被选中，如果选中，修改选中缓存
                    if("1".equals(cart.getIsChecked())){
                        jedis.hset(checkKey,cart.getSkuId(),JSON.toJSONString(cart));
                    }
                }else {
                    jedis.hset(key, skuId, JSON.toJSONString(cart));
                }
            }else {
                //不存在，添加数据库，加入缓存
                cart = new CartInfo();
                //购物车不存在，添加购物车
                SkuInfo skuInfo = skuInfoService.getSkuInfoAndImageById(Integer.valueOf(skuId));
                cart.setSkuNum(skuNum);
                cart.setUserId(userId);
                cart.setSkuId(skuId);
                cart.setCartPrice(BigDecimal.valueOf(skuInfo.getPrice()));
                cart.setImgUrl(skuInfo.getSkuDefaultImg());
                cart.setSkuName(skuInfo.getSkuName());
                cart.setSkuPrice(BigDecimal.valueOf(skuInfo.getPrice()));
                cartInfoMapper.insert(cart);
                jedis.hset(key, skuId, JSON.toJSONString(cart));
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }


    }

    /**
     * 购物车列表
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> cartList(String userId) {
       Jedis jedis=null;
        List<CartInfo> cartInfoList=null ;
        //获取key
        String key=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.CART_POSTFIX;
        //获取缓存中该用户所有购物车
        List<String> cartListStrs =null;
       try {
           jedis=redisUtil.getJedis();
           cartListStrs=jedis.hvals(key);
           //有缓存
            if(cartListStrs!=null&&cartListStrs.size()>0){
                cartInfoList=new ArrayList<>();
                for (String cartStr: cartListStrs) {
                    if(!cartStr.equals("null")){
                        CartInfo cartInfo = JSON.parseObject(cartStr, CartInfo.class);
                        cartInfoList.add(cartInfo);
                    }
                }
            }else {
                    //走数据库
                    CartInfo cartInfo=new CartInfo();
                    cartInfo.setUserId(userId);
                    cartInfoList= cartInfoMapper.select(cartInfo);
                    if(cartInfoList==null||cartInfoList.size()<=0){
                        //为空，添加缓存为空
                        jedis.hset(key,"nocart","null");
                        jedis.expire(key,24*60*60);
                    }else {
                        //添加缓存
                        for (CartInfo cart : cartInfoList) {
                            cart.setSkuPrice(cart.getCartPrice());
                            jedis.hset(key,cart.getSkuId(),JSON.toJSONString(cart));
                        }
                    }

            }
       }catch (Exception e){
           e.printStackTrace();
           //走数据库
           CartInfo cartInfo=new CartInfo();
           cartInfo.setUserId(userId);
           cartInfoList= cartInfoMapper.select(cartInfo);
       }finally {
           if(jedis!=null){
               //关闭redis连接
               jedis.close();
           }
       }
        //更新实时价格
       if(cartInfoList!=null&&cartInfoList.size()>0) {
           for (CartInfo cartInfo : cartInfoList) {
               SkuInfo skuInfo = skuInfoService.getSkuInfoAndImageById(Integer.valueOf(cartInfo.getSkuId()));
               //System.out.println(skuInfo);
               cartInfo.setSkuPrice(BigDecimal.valueOf(skuInfo.getPrice()));
           }
       }
       return  cartInfoList;
    }

    /**
     * 合并购物车
     * @param userCartList
     * @param cookieCartList
     * @return
     */
    @Override
    public List<CartInfo> mergeCart(List<CartInfo> userCartList, List<CartInfo> cookieCartList,String userId) {
        //判断cookie中购物车是否为空
       if(cookieCartList==null||cookieCartList.size()==0){
           //判断登录用户的购物车是否为空
           if(userCartList==null||userCartList.size()==0){
               return  null;
           }
           return  userCartList;
       }
        Jedis jedis=null;
        //获取购物车key
        String key=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.CART_POSTFIX;
        //获取购物车选中key
        String checkedKey=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.CART_CHECKED;
       //用来判断cookie中的购物车是否和登录用户的购物车有相同商品 
        boolean isExits=false;
        try {
            jedis=redisUtil.getJedis();
            //开始合并
            for (CartInfo cookieCart : cookieCartList) {
                isExits=false;
                for (CartInfo userCart : userCartList) {
                    if(userCart.getSkuId().equals(cookieCart.getSkuId())){
                        //添加数量
                        userCart.setSkuNum(userCart.getSkuNum()+cookieCart.getSkuNum());
                        //更新数据库
                        cartInfoMapper.updateByPrimaryKeySelective(userCart);
                        //存在
                        isExits=true;
                        //判断当前cookie中购物车是否选中
                        if("1".equals(cookieCart.getIsChecked())){
                            //将购物车状态选中
                            userCart.setIsChecked("1");
                            //选中，添加到订单购物车
                            jedis.hset(checkedKey,userCart.getSkuId(),JSON.toJSONString(userCart));
                        }
                        //选中，将缓存中对应的购物车选中且更新数量
                        jedis.hset(key,userCart.getSkuId(),JSON.toJSONString(userCart));
                        break;
                    }
                }
                if(!isExits){
                    //数据库不存在，添加
                    cookieCart.setUserId(userId);
                    cartInfoMapper.insert(cookieCart);
                    //判断当前cookie中购物车是否选中
                    if("1".equals(cookieCart.getIsChecked())){
                        //将购物车状态选中
                        cookieCart.setIsChecked("1");
                        //选中，添加到订单购物车
                        jedis.hset(checkedKey,cookieCart.getSkuId(),JSON.toJSONString(cookieCart));
                    }
                    //选中，将缓存中对应的购物车选中且更新数量
                    jedis.hset(key,cookieCart.getSkuId(),JSON.toJSONString(cookieCart));
                }
            }
            //重新查询
            List<CartInfo> cartInfoList = cartList(userId);
            return  cartInfoList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    /**
     * 选中和取消选中购物车
     * @param userId
     * @param skuId
     * @param IsChecked
     */
    @Override
    public void checkedCart(String userId, String skuId, String IsChecked) {
        Jedis jedis=null;
        String cartKey=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.CART_POSTFIX;
        String checkKey=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.CART_CHECKED;

        try {
            jedis = redisUtil.getJedis();
            String cartInfoStr = jedis.hget(cartKey, skuId);
            //从缓存查询当前购物车
            CartInfo cartInfo=JSON.parseObject(cartInfoStr,CartInfo.class);
            if("1".equals(IsChecked)){
                //修改当前购物车状态
                cartInfo.setIsChecked("1");
                //添加当前用户选中的购物车
                jedis.hset(checkKey,skuId,JSON.toJSONString(cartInfo));
            }else {
                //修改购物车状态
                cartInfo.setIsChecked("0");
                //删除当前用户选中的购物车
                jedis.hdel(checkKey,skuId);
            }
            //更新购物车缓存
            jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfo));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    /**
     * 获取当前用户选中的购物车
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getOrderCartList(String userId) {
       Jedis jedis=null;
        try {
            jedis=redisUtil.getJedis();
            String checkCartKey=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.CART_CHECKED;
            List<CartInfo> cartInfoList=new ArrayList<>();
            List<String> cartInfoStrList = jedis.hvals(checkCartKey);
            for (String cartInfoStr : cartInfoStrList) {
                    CartInfo cartInfo=JSON.parseObject(cartInfoStr,CartInfo.class);
                    //获取最新价格
                SkuInfo skuInfo = skuInfoService.getSkuInfoAndImageById(Integer.valueOf(cartInfo.getSkuId()));
                cartInfo.setSkuPrice(BigDecimal.valueOf(skuInfo.getPrice()));
                cartInfoList.add(cartInfo);
            }
            return  cartInfoList;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        } finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    /**
     * 更新购物车缓存
     * @param userId
     */
    @Override
    public void loadCartCache(String userId) {
        Jedis jedis=null;
        //当前用户购物车redis key
        String cartKey=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.CART_POSTFIX;
        //当前用户选中购物车redis key
        String checkKey=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.CART_CHECKED;
        try {
            jedis=redisUtil.getJedis();
            //获取redis中当前用户的购物车
            List<String> cartListStr = jedis.hvals(cartKey);
            if(cartListStr!=null&&cartListStr.size()>0){
                for (String cartStr : cartListStr) {
                        CartInfo cartInfo=JSON.parseObject(cartStr,CartInfo.class);
                        //查询数据库最新信息
                        SkuInfo skuInfo=skuInfoService.getSkuInfoById(Integer.valueOf(cartInfo.getSkuId()));
                        //设置新价格
                        cartInfo.setSkuPrice(BigDecimal.valueOf(skuInfo.getPrice()));
                        //更新购物车缓存
                        jedis.hset(cartKey,skuInfo.getId(),JSON.toJSONString(cartInfo));
                        //更新数据库
                        CartInfo dbCart=new CartInfo();
                        dbCart.setId(cartInfo.getId());
                        dbCart.setCartPrice(BigDecimal.valueOf(skuInfo.getPrice()));
                        cartInfoMapper.updateByPrimaryKeySelective(dbCart);
                        //更新当前用户选中购物车价格
                        if("1".equals(cartInfo.getIsChecked())){
                            jedis.hset(checkKey,cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
                        }
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }


}
