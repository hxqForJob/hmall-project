package com.hmall.order.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hmall.pojo.*;
import com.hmall.service.CartService;
import com.hmall.service.OrderService;
import com.hmall.service.SkuInfoService;
import com.hmall.service.UserService;
import com.hmall.web.utils.annotation.LoginRequire;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@Controller
public class OrderController {

    /**
     * 用户业务逻辑
     */
    @Reference
    private UserService userService;


    /**
     * 购物车业务逻辑
     */
    @Reference
    private CartService cartService;

    /**
     * 订单业务逻辑
     */
    @Reference
    private OrderService orderService;

    /**
     * 获取商品业务逻辑
     */
    @Reference
    private SkuInfoService skuInfoService;

    /**
     * 结算
     * @return
     */
    @RequestMapping("/toTrade")
    @LoginRequire(autoRedirect = true)
    public  String initTrade(HttpServletRequest request){
        String userId=request.getAttribute("userId").toString();
        //获取当前用户地址
        List<UserAddress> addressList = userService.getUserAddress(userId);
        //获取当前用户选中的购物车
        List<CartInfo> cartInfoList = cartService.getOrderCartList(userId);
        if(cartInfoList==null||cartInfoList.size()<=0){
            request.setAttribute("errMsg","当前订单没有商品，请重新选择商品下单");
            return "tradeFail";
        }
        Double totalPrice=0d;
        //获取购物车的总价格
        for (CartInfo cartInfo : cartInfoList) {
            totalPrice+=cartInfo.getSkuPrice().doubleValue()*cartInfo.getSkuNum();
        }
        //初始化交易编号
        String tradeNo= orderService.initTradeNo(userId);
        request.setAttribute("addressList",addressList);
        request.setAttribute("cartInfoList",cartInfoList);
        request.setAttribute("totalPrice",totalPrice);
        request.setAttribute("tradeNo",tradeNo);
        return "trade";
    }

    /**
     * 提交订单
     * @param orderInfo
     * @return
     */
    @RequestMapping("/submitOrder")
    @LoginRequire(autoRedirect = true)
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String userId=request.getAttribute("userId").toString();
        //获取页面交易编号
        String tradeNo=request.getParameter("tradeNo");
        //获取缓存交易编号
        String tradeNoCache=orderService.getTradeNo(userId);
        //判断当前订单是否重复提交
        if(!tradeNoCache.equals(tradeNo)){
            request.setAttribute("errMsg","不能重复提交订单");
            return  "tradeFail";
        }
        //检验价格，库存
        for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
            String skuId = orderDetail.getSkuId();
            SkuInfo skuInfo = skuInfoService.getSkuInfoById(Integer.valueOf(skuId));
            boolean priceCheck=orderDetail.getOrderPrice().doubleValue()==skuInfo.getPrice();
            //价格不一致
            if(!priceCheck){
                request.setAttribute("errMsg",orderDetail.getSkuName()+"价格已改动，请重新下单");
                //更新购物车缓存
                cartService.loadCartCache(userId);
                return  "tradeFail";
            }
            //检验库存
            boolean flag = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!flag){
                request.setAttribute("errMsg",orderDetail.getSkuName()+"商品库存不足！");
                return "tradeFail";
            }
        }
        //删除缓存交易编号
        orderService.delTradeNo(userId);
        orderInfo.setUserId(userId);
        orderInfo.setPaymentWay(PaymentWay.ONLINE);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.sumTotalAmount();
        String orderId = orderService.addOrderInfo(orderInfo);
        return "redirect://payment.hmall.com/index?orderId="+orderId;
    }

    /**
     * 拆单
     * @param orderId 主订单Id
     * @param wareSkuMap 拆单参数[{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
     * @return
     */
    @RequestMapping("/orderSplit")
    @ResponseBody
    public String orderSplit(@RequestParam("orderId") String orderId, @RequestParam("wareSkuMap")List<Map> wareSkuMap){
        List<Map> splitResult=orderService.splitOrder(orderId,wareSkuMap);
        return JSON.toJSONString(splitResult);
    }
}
