package com.wsc.sharding.example.masterslave.service.impl;

import com.wsc.sharding.example.masterslave.dao.OrderItemRepository;
import com.wsc.sharding.example.masterslave.dao.OrderRepository;
import com.wsc.sharding.example.masterslave.entity.Order;
import com.wsc.sharding.example.masterslave.entity.OrderItem;
import com.wsc.sharding.example.masterslave.service.SpringPojoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 这是你的Service层  写具体的逻辑
 * 假设是创建订单
 */
@Service
public class SpringPojoServiceImpl implements SpringPojoService {

    private static final Logger logger = LoggerFactory.getLogger(SpringPojoServiceImpl.class);
    
    @Resource
    private OrderRepository orderRepository;
    
    @Resource
    private OrderItemRepository orderItemRepository;

    @Override
    public void initEnvironment() {
        //如果订单表不存在,创建表
        orderRepository.createTableIfNotExists();
        //如果订单项表不存在,创建订单项表
        orderItemRepository.createTableIfNotExists();
        //清除表中的数据
        orderRepository.truncateTable();
        //清除表中的数据
        orderItemRepository.truncateTable();
    }

    @Override
    public void cleanEnvironment() {
        //删除表
        orderRepository.dropTable();
        //删除表
        orderItemRepository.dropTable();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process() {
        logger.info("-------------- 开始执行业务操作 ---------------");
        List<Long> orderIds = insertData();
        logger.info("-------------- 业务执行完成 --------------");
    }

    private List<Long> insertData() {
        logger.info("---------------------------- 插入数据 ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            OrderItem item = new OrderItem();
            item.setOrderId(order.getOrderId());
            item.setUserId(i);
            item.setStatus("INSERT_TEST");
            orderItemRepository.insert(item);
            result.add(order.getOrderId());
        }
        return result;
    }
    
    @Override
    public void printData() {
        logger.info("---------------------------- 打印订单 -----------------------");
        for (Object each : orderRepository.selectAll()) {
            System.out.println(each);
        }
        logger.info("---------------------------- 打印订单项 -------------------");
        for (Object each : orderItemRepository.selectAll()) {
            System.out.println(each);
        }
    }
}
