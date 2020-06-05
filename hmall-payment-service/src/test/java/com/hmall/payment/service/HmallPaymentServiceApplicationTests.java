package com.hmall.payment.service;

import lombok.SneakyThrows;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.AsyncCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class HmallPaymentServiceApplicationTests {

    @Test
   public void sendTest() throws JMSException {
        //1.创建连接工厂
        //2.获取连接
        //3.打开连接
        //4.创建session
        //5.创建队列
        //6.创建生产者
        //7.创建消息
        //8.发送消息
        //9提交事务
        //10.关闭连接
        ActiveMQConnectionFactory connectionFactory=new ActiveMQConnectionFactory("tcp://192.168.25.132:61616");
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = session.createQueue("TEST-QUEUE");
        MessageProducer producer = session.createProducer(queue);
        TextMessage textMessage = session.createTextMessage("测试activemq点对点模式");
        producer.send(textMessage);
        session.commit();
        producer.close();
        session.close();
        connection.close();


    }

    @Test
    public void reciveTest() throws JMSException {
        //1.创建连接工厂
        //2.获取连接
        //3.打开连接
        //4.创建session
        //5.创建队列
        //6.创建消费者
        //7.创建监听器
        //8.接收消息
        ActiveMQConnectionFactory connectionFactory=new ActiveMQConnectionFactory("tcp://192.168.25.132:61616");
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = session.createQueue("TEST-QUEUE");
        MessageConsumer consumer = session.createConsumer(queue);
        MessageListener messageListener=new MessageListener() {
            @SneakyThrows
            @Override
            public void onMessage(Message message) {
                    if (message instanceof TextMessage){
                        TextMessage textMessage= (TextMessage) message;
                        System.out.println(textMessage.getText());
                    }
            }
        };
        consumer.setMessageListener(messageListener);


    }

}
