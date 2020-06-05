package com.hmall.service;

import com.hmall.pojo.CartInfo;

import java.util.List;

/**
 * 购物车业务逻辑接口
 */
public interface CartService {

    /**
     * 添加购物车
     * @param userId 用户Id
     * @param skuId skuId
     * @param num 数量
     */
    void addCartInfo(String userId,String skuId,Integer num);

    /**
     * 查询购物车
     * @param userId
     * @return
     */
    List<CartInfo> cartList(String userId);

    /**
     * 合并购物车
     * @param userCartList
     * @param cookieCartList
     * @return
     */
    List<CartInfo> mergeCart(List<CartInfo> userCartList, List<CartInfo> cookieCartList,String userId);

    /**
     * 选中购物车，取消选中购物车
     */
    void checkedCart(String userId,String skuId,String Ischecked);


    /**
     * 获取当前用户下单的购物车
     * @param userId
     * @return
     */
    List<CartInfo> getOrderCartList(String userId);

    /**
     * 更新购物车缓存
     * @param userId
     */
    void loadCartCache(String userId);
}
