package com.hmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.github.wxpay.sdk.WXPayUtil;
import com.hmall.payment.service.mapper.PaymentInfoMapper;
import com.hmall.pojo.PaymentInfo;
import com.hmall.pojo.PaymentStatus;
import com.hmall.service.PaymentService;
import com.hmall.service.util.mqConfig.ActiveMQUtil;
import com.hmall.service.util.util.HttpClient;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.lang.annotation.ElementType;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付业务逻辑实现
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    /**
     * 支付数据访问成
     */
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    /**
     * 支付宝客户操作
     */
    @Autowired
    private AlipayClient alipayClient;

    /**
     * 注入消息中间件
     */
    @Autowired
    private ActiveMQUtil activeMQUtil;

    // 服务号Id
    @Value("${appid}")
    private String appid;
    // 商户号Id
    @Value("${partner}")
    private String partner;
    // 密钥
    @Value("${partnerkey}")
    private String partnerkey;


    /**
     * 添加支付信息
     * @param paymentInfo
     */
    @Override
    public void addOrUpdatePaymentInfo(PaymentInfo paymentInfo) {

        PaymentInfo paymentInfoQuery=new PaymentInfo();
        paymentInfoQuery.setOrderId(paymentInfo.getOrderId());
        paymentInfoQuery.setOutTradeNo(paymentInfo.getOutTradeNo());
        //查询支付信息
        paymentInfoQuery = paymentInfoMapper.selectOne(paymentInfoQuery);
        //判断是否存在
        if(paymentInfoQuery!=null) {
            //存在
            //判断是否已经支付,没支付修改创建时间
            if(paymentInfoQuery.getPaymentStatus().equals(PaymentStatus.UNPAID)){
                paymentInfoQuery.setCreateTime(new Date());
                paymentInfoMapper.updateByPrimaryKeySelective(paymentInfoQuery);
            }
        }else {
            //不存在，添加支付信息
            paymentInfo.setCreateTime(new Date());
            paymentInfoMapper.insertSelective(paymentInfo);
        }
    }

    /**
     *根据交易编号获取交易信息
     * @param out_trade_no
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfo(String out_trade_no) {
        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        return  paymentInfoMapper.selectOne(paymentInfo);
    }

    /**
     * 更新支付信息
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd) {
        Example example=new Example(PaymentInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("outTradeNo",out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfoUpd,example);
    }

    /**
     * 退款
     * @param orderId
     * @return
     */
    @Override
    public boolean refund(String orderId) {
        //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        PaymentInfo paymentInfo = getPaymentInfoByOrderId(orderId);

        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("refund_amount", paymentInfo.getTotalAmount());

        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }

    }

    /**
     * 根据订单Id获取支付信息
     * @param orderId
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfoByOrderId(String orderId) {
        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        PaymentInfo info = paymentInfoMapper.selectOne(paymentInfo);
        return info;
    }

    /**
     *通知订单
     * @param orderId
     */
    @Override
    public void notifyOrder(String orderId,String result) {
        Session session=null;
        Connection connection=null;
        MessageProducer producer=null;
        try {
            //获取连接
            connection = activeMQUtil.getConnection();
            connection.start();
            //创建session
            session= connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_QUEUE");
            //创建生产者
             producer = session.createProducer(paymentResultQueue);
             //创建消息
            MapMessage mapMessage = session.createMapMessage();
            mapMessage.setString("result",result);
            mapMessage.setString("orderId",orderId);
            //发送消息
            producer.send(mapMessage);
            //提交事务
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            //关闭连接
            try {
                if(producer!=null){
                    producer.close();
                }
                if(session!=null){
                    session.close();
                }
                if(connection!=null){
                    connection.close();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 微信支付获取二维码
     * @param orderId
     * @param totalFree
     * @return
     */
    @Override
    public Map createNative(String orderId, String totalFree) {
        //1.创建参数
        Map<String,String> param=new HashMap();//创建参数
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "尚硅谷");//商品描述
        param.put("out_trade_no", orderId);//商户订单号
        param.put("total_fee",totalFree);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://order.gmall.com/trade");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型
        try {
            //2.生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获得结果
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, String> map=new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", totalFree);//总金额
            map.put("out_trade_no",orderId);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * 查询支付宝下该订单Id是否支付
     * @param paymentInfoQuery
     * @return
     */
    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {
        // 查询当前的支付信息
        PaymentInfo paymentInfo = getPaymentInfo(paymentInfoQuery.getOutTradeNo());
        if (paymentInfo.getPaymentStatus()== PaymentStatus.PAID || paymentInfo.getPaymentStatus()==PaymentStatus.ClOSED){
            return true;
        }
        //根据交易订单号查询支付宝交易记录
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\""+paymentInfo.getOutTradeNo()+"\"" +
                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            if ("TRADE_SUCCESS".equals(response.getTradeStatus())||"TRADE_FINISHED".equals(response.getTradeStatus())){
                //  IPAD
                System.out.println("支付成功");
                // 改支付状态
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfo.getOutTradeNo(),paymentInfoUpd);
                //通知订单完成支付
                notifyOrder(paymentInfo.getOrderId(),"success");
                return true;
            }else {
                System.out.println("支付失败");
                return false;
            }

        } else {
            System.out.println("支付失败");
            return false;
        }

    }

    /**
     * 生成支付宝二维码时发送延迟队列，判断是否支付完成
     * @param outTradeNo 支付宝交易编号
     * @param delaySec 延迟秒数
     * @param checkCount 重发次数
     */
    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(paymentResultQueue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setInt("delaySec",delaySec);
            mapMessage.setInt("checkCount",checkCount);
            // 设置延迟多少时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);
            producer.send(mapMessage);
            session.commit();
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    /**
     * 关闭付款信息
     * @param orderId
     */
    @Override
    public void closePayment(String orderId) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);

    }

}

