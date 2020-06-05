package com.hmall.service.util.redisConfig;

public class RedisKeyUtil {
    public static final  String SKUINFO_PREFIX="skuInfo:"; //skuinfo key前缀
    public static final  String SKUINFO_POSTFIX=":info";//skuinfo key后缀
    public  static  final  int SKUINFO_EXPIRE=24*60*60;//skuinfo key过期时间
    public static final  String SKUINFOHOT_POSTFIX=":hot"; //skuHot key前缀
    public static final String USERINFO_PREFIX="userInfo:"; //userInfo key前缀
    public  static  final String USERINFO_POSTFIX=":info"; //userInfo key后缀
    public  static  final  int USERINFO_EXPIRE=7*60*60*24;//skuinfo key过期时间
    public static final String CART_POSTFIX=":cart";//购物车后缀
    public  static final String CART_CHECKED=":cartChecked";//选中购物车后缀
    public  static final String ORDER_TRADE=":tradeNo";//订单交易编号
}
