package com.hmall.payment.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.hmall.payment.web.config.AlipayConfig;
import com.hmall.pojo.OrderInfo;
import com.hmall.pojo.OrderStatus;
import com.hmall.pojo.PaymentInfo;
import com.hmall.pojo.PaymentStatus;
import com.hmall.service.OrderService;
import com.hmall.service.PaymentService;
import com.hmall.web.utils.annotation.LoginRequire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 */
@Controller
public class PaymentController {

    /**
     * 注入订单业务逻辑
     */
    @Reference
    private OrderService orderService;

    @Reference
    private PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;

    /**
     * 选中支付方式页面
     */
    @RequestMapping("/index")
    @LoginRequire()
    public String index(String orderId, Model model){
        OrderInfo orderInfo = orderService.getOrderAndDetail(orderId);
        orderInfo.sumTotalAmount();
        model.addAttribute("orderInfo",orderInfo);
        return "index";
    }

    /**
     * 错误视图
     * @param errorMsg
     * @param model
     * @return
     */
    @RequestMapping("/tradeFail")
    @LoginRequire
    public  String tradeFail(String errorMsg,Model model){
        model.addAttribute("errMsg",errorMsg);
        return "tradeFail";
    }


    /**
     * 提交支付宝支付
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/alipay/submit",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequire
    public String submitPayment(HttpServletRequest request, HttpServletResponse response){
        // 获取订单Id
        String orderId = request.getParameter("orderId");
        //根据订单Id获取支付信息
        PaymentInfo paymentInfo1 = paymentService.getPaymentInfoByOrderId(orderId);
        //判断当前订单支付信息是否已经支付
        if(paymentInfo1!=null&&!paymentInfo1.getPaymentStatus().equals(PaymentStatus.UNPAID)){
            try {
                response.sendRedirect("/tradeFail?errorMsg="+"当前订单已支付，无需重复下单");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        // 取得订单信息
        OrderInfo orderInfo = orderService.getOrderAndDetail(orderId);
        if(!orderInfo.getOrderStatus().equals(OrderStatus.UNPAID)){
            try {
                response.sendRedirect("/tradeFail?errorMsg="+"当前订单已支付，无需重复下单");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);

        // 保存信息
        paymentService.addOrUpdatePaymentInfo(paymentInfo);

        // 支付宝参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        // 声明一个Map
        Map<String,Object> bizContnetMap=new HashMap<>();
        bizContnetMap.put("out_trade_no",paymentInfo.getOutTradeNo());
        bizContnetMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        bizContnetMap.put("subject",paymentInfo.getSubject());
        bizContnetMap.put("total_amount",paymentInfo.getTotalAmount());
        // 将map变成json
        String Json = JSON.toJSONString(bizContnetMap);
        alipayRequest.setBizContent(Json);
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
        //开启延迟队列，验证是否支付成功
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);
        return form;
    }

    /**
     * 支付宝同步回调
     * @return
     */
    @RequestMapping(value = "/alipay/callback/return",method = RequestMethod.GET)
    @LoginRequire
    public String callbackReturn(){
        return "redirect:"+AlipayConfig.return_order_url;
    }

    /**
     * 支付宝异步回调
     * @param paramMap
     * @param request
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping(value = "/alipay/callback/notify",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequire
    public String paymentNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request) throws AlipayApiException {
        //验证签名
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8",AlipayConfig.sign_type);
        if (!flag){
            //签名验证失败
            return "fail";
        }
        // 判断结束
        String trade_status = paramMap.get("trade_status");
        //当前订单是否完成支付
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
            // 查单据是否处理
            String out_trade_no = paramMap.get("out_trade_no");

            PaymentInfo paymentInfoHas = paymentService.getPaymentInfo(out_trade_no);
            //如果订单已字符，取消支付
            if (paymentInfoHas==null||paymentInfoHas.getPaymentStatus()==PaymentStatus.PAID || paymentInfoHas.getPaymentStatus()==PaymentStatus.ClOSED){
                return "fail";
            }else {
                //获取回调后的金额
                Double total_amount = Double.valueOf(paramMap.get("total_amount"));
                //金额不同
                if(paymentInfoHas.getTotalAmount().doubleValue()!=total_amount){
                    return "fail";
                }
                // 修改
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                // 设置状态
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                // 设置回调时间
                paymentInfoUpd.setCallbackTime(new Date());
                // 设置内容
                paymentInfoUpd.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);
                //发送消息，通知订单已支付
                paymentService.notifyOrder(paymentInfoHas.getOrderId(),"success");
                return "success";
            }
        }
        return  "fail";
    }

    /**
     * 支付宝退款
     * @param orderId
     * @return
     */
    @RequestMapping("refund")
    @ResponseBody
    @LoginRequire
    public String refund(String orderId){
        boolean flag = paymentService.refund(orderId);
        System.out.println("flag:"+flag);
        return flag+"";
    }

    /**
     * 支付完成，发送消息通知订单修改状态
     * @param orderId
     * @return
     */
    @RequestMapping("/notifyOrder")
    @ResponseBody
    @LoginRequire
    public void notifyOrder(String orderId){
        //发送消息，通知订单
     paymentService.notifyOrder(orderId,"success");
    }


    /**
     * 生成微信支付二维码
     * @param
     * @return
     */
    @RequestMapping("wx/submit")
    @ResponseBody
    @LoginRequire
    public Map createNative(HttpServletRequest request, HttpServletResponse response){
        // 获取订单Id
        String orderId = request.getParameter("orderId");
        //根据订单Id获取支付信息
        PaymentInfo paymentInfo1 = paymentService.getPaymentInfoByOrderId(orderId);
        //判断当前订单支付信息是否已经支付
        if(paymentInfo1!=null&&!paymentInfo1.getPaymentStatus().equals(PaymentStatus.UNPAID)){
            try {
                response.sendRedirect("/tradeFail?errorMsg="+"当前订单已支付，无需重复下单");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        // 取得订单信息
        OrderInfo orderInfo = orderService.getOrderAndDetail(orderId);
        if(!orderInfo.getOrderStatus().equals(OrderStatus.UNPAID)){
            try {
                response.sendRedirect("/tradeFail?errorMsg="+"当前订单已支付，无需重复下单");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);

        // 保存信息
        paymentService.addOrUpdatePaymentInfo(paymentInfo);
        //调用微信支付业务逻辑
        Map map = paymentService.createNative(orderId +"", "1");
        System.out.println(map.get("code_url"));
// data = map
        return map;
    }


}
