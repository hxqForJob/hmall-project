package com.hmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hmall.order.service.mapper.OrderDetailMapper;
import com.hmall.order.service.mapper.OrderInfoMapper;
import com.hmall.pojo.*;
import com.hmall.service.OrderService;
import com.hmall.service.PaymentService;
import com.hmall.service.util.mqConfig.ActiveMQUtil;
import com.hmall.service.util.redisConfig.RedisKeyUtil;
import com.hmall.service.util.redisConfig.RedisUtil;
import com.hmall.utils.HttpClientUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

/**
 * 订单业务逻辑实现
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Reference
    private PaymentService paymentService;

    @Reference
    private  OrderServiceImpl orderService;


    /**
     * 初始化交易编号
     * @param userId
     * @return
     */
    @Override
    public String initTradeNo(String userId) {
        Jedis jedis=null;
        String uId = UUID.randomUUID().toString();
        try {
            jedis=redisUtil.getJedis();
            String key=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.ORDER_TRADE;
            jedis.set(key,uId);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        return  uId;
    }

    /**
     * 删除交易编号
     * @param userId
     */
    @Override
    public void delTradeNo(String userId) {
        Jedis jedis=null;
        try {
            jedis=redisUtil.getJedis();
            String key=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.ORDER_TRADE;
            jedis.del(key);
        }catch (Exception e){
            e.printStackTrace();;
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    /**
     * 获取交易编号
     * @param userId
     * @return
     */
    @Override
    public String getTradeNo(String userId) {
        Jedis jedis=null;
        try {
            jedis=redisUtil.getJedis();
            String key=RedisKeyUtil.USERINFO_PREFIX+userId+RedisKeyUtil.ORDER_TRADE;
            String tradeNo = jedis.get(key);
            if(tradeNo==null){
                tradeNo="";
            }
            return tradeNo;
        }catch (Exception e){
            e.printStackTrace();;
            return "";
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    /**
     * 添加订单, 返回订单Id
     * @param orderInfo
     */
    @Transactional
    @Override
    public String addOrderInfo(OrderInfo orderInfo) {

        // 设置创建时间
        orderInfo.setCreateTime(new Date());
        // 设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        // 生成第三方支付订单编号
        String outTradeNo="HMALL"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);

        // 插入订单详细信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        // 为了跳转到支付页面使用。支付会根据订单id进行支付。
        String orderId = orderInfo.getId();
        return orderId;

    }

    /**
     * 获取订单信息和订单明细
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderAndDetail(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.getOrderAndDetail(Integer.valueOf(orderId));
        return  orderInfo;
    }

    /**
     * 获取订单
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderInfo(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        return  orderInfo;
    }

    /**
     * 检验库存
     * @param skuId
     * @param skuNum
     * @return
     */
    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        // 调用gware-manage 库存系统 http://www.gware.com/hasStock?skuId=10221&num=2
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }

    /**
     * 更新订单状态
     * @param orderId
     * @param status
     */
    @Override
    public void updateOrderStatus(String orderId, ProcessStatus status) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        orderInfo.setProcessStatus(status);
        orderInfo.setOrderStatus(status.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    /**
     * 发送消息通知仓库
     * @param orderId
     */
    @Override
    public void notifyWare(String orderId) {
        OrderInfo orderAndDetail = getOrderAndDetail(orderId);
        //初始化订单参数
        Map<String,Object> wareParam=initWareParam(orderAndDetail);
        String jsonParam= JSON.toJSONString(wareParam);
        //发送消息
        sendOrderMsg(jsonParam);
    }

    /**
     * 获取过期订单
     * @return
     */
    @Override
    public List<OrderInfo> getExpiredOrderList() {
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andLessThan("expireTime",new Date()).andEqualTo("processStatus",ProcessStatus.UNPAID);
        return orderInfoMapper.selectByExample(example);

    }

    /**
     * 关闭过期订单和支付记录
     * @param orderInfo
     */
    @Override
    @Async //多线程关闭
    public void execExpiredOrder(OrderInfo orderInfo) {
        // 订单信息
        updateOrderStatus(orderInfo.getId(),ProcessStatus.CLOSED);
        // 关闭付款信息
        paymentService.closePayment(orderInfo.getId());

    }

    /**
     * 拆单
     * @param orderId
     * @param wareSkuMap
     * @return
     */
    @Override
    @Transactional
    public List<Map> splitOrder(String orderId, List<Map> wareSkuMap) {
        //用来保存结果
        List<Map> result=new ArrayList<>();
        //组装当前订单信息
        Map<String,Object> orderMap=new HashMap<>();
        //根据订单Id获取主订单
        OrderInfo orderInfo = getOrderAndDetail(orderId);
        //遍历需要拆单的明细
        for (Map map : wareSkuMap) {
            //获取订单的仓库Id
            String wareId=map.get("wareId").toString();
            //获取订单的商品Id
            List<String> skuIds= (List<String>) map.get("skuIds");
            //声明子订单
             OrderInfo subOrder=new OrderInfo();
           //拷贝属性
            BeanUtils.copyProperties(orderInfo,subOrder);
            subOrder.setOrderDetailList(new ArrayList<>());
            //设置父订单Id
            subOrder.setParentOrderId(orderId);
            //设置仓库Id
            subOrder.setWareId(wareId);
            //用来保存自定义明细
            List<Map> details=new ArrayList<>();
            for (String skuId : skuIds) {
                for (OrderDetail orderDetail : orderInfo.getOrderDetailList()) {
                    //如果主订单明细中商品Id和拆单商品的Id相同，则将主订单的明细添加到子订单中明细中
                    if(skuId.equals(orderDetail.getSkuId())){
                        orderDetail.setId(null);
                        subOrder.getOrderDetailList().add(orderDetail);
                        Map skuDetail=new HashMap();
                        skuDetail.put("skuId",skuId);
                        skuDetail.put("skuNum",orderDetail.getSkuNum());
                        skuDetail.put("skuName",orderDetail.getSkuName());
                        details.add(skuDetail);
                    }
                }
            }
            subOrder.sumTotalAmount();
            //保存子订单
            orderService.addOrderInfo(subOrder);
            orderMap.put("orderId",subOrder.getId());
            orderMap.put("consignee",subOrder.getConsignee());
            orderMap.put("consigneeTel",subOrder.getConsigneeTel());
            orderMap.put("orderComment",subOrder.getOrderComment());
            orderMap.put("orderBody",subOrder.getTradeBody());
            orderMap.put("deliveryAddress",subOrder.getDeliveryAddress());
            orderMap.put("paymentWay",subOrder.getPaymentWay());
            orderMap.put("wareId",wareId);
            orderMap.put("details",details);
            result.add(orderMap);
        }
        //更新主订单状态
        orderService.updateOrderStatus(orderId,ProcessStatus.SPLIT);
        return  result;
    }

    /**
     * 发送消息
     * @param jsonParam
     */
    private void sendOrderMsg(String jsonParam) {
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
            Queue orderResultQueue = session.createQueue("ORDER_RESULT_QUEUE");
            //创建生产者
            producer = session.createProducer(orderResultQueue);
            //创建消息
            TextMessage mapMessage = session.createTextMessage(jsonParam);
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
     * 初始化订单参数
     * @param orderAndDetail
     * @return
     */
    private Map<String, Object> initWareParam(OrderInfo orderAndDetail) {
            Map<String,Object> mapParam=new HashMap<>();
            List<Map<String,Object>> detailList=new ArrayList<>();
            mapParam.put("orderId",orderAndDetail.getId());
            mapParam.put("consignee", orderAndDetail.getConsignee());
            mapParam.put("consigneeTel",orderAndDetail.getConsigneeTel());
            mapParam.put("orderComment",orderAndDetail.getOrderComment());
            mapParam.put("orderBody",orderAndDetail.getTradeBody());
            mapParam.put("deliveryAddress",orderAndDetail.getDeliveryAddress());
            mapParam.put("paymentWay","2");
            mapParam.put("wareId",orderAndDetail.getWareId());
        List<OrderDetail> orderDetailList = orderAndDetail.getOrderDetailList();
        if(orderDetailList!=null&&orderDetailList.size()>0){
            for (OrderDetail orderDetail : orderDetailList) {
                Map<String,Object> detailMap=new HashMap<>();
                detailMap.put("skuId",orderDetail.getSkuId());
                detailMap.put("skuNum",orderDetail.getSkuNum());
                detailMap.put("skuName",orderDetail.getSkuName());
                detailList.add(detailMap);
            }
        }
        mapParam.put("details",detailList);
        return  mapParam;
    }

}
