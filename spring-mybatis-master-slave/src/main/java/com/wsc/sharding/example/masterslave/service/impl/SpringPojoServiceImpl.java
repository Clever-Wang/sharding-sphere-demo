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

        // 事务方法中，查询sql默认会走slave库，但是一旦遇到insert/update/delete语句，就会设置一个查询主库的标志，而这个对象是存在ThreadLocal中的，其后的sql都会在主库执行

        //默认使用从库查询
        printData();

        logger.info("-------------- 开始执行插入操作 ---------------");
        //使用主库
        List<Long> orderIds = insertData();
        logger.info("-------------- 插入操作执行完成 --------------");

        // 上面执行过 insert 操作,所以下面的查询走主库
        logger.info("-------------- 开始打印数据 --------------");
        printData();
        logger.info("-------------- 数据打印结束 --------------");

        // 上面执行过 insert 操作,所以下面的查询走主库
        logger.info("-------------- 开始打印数据2 --------------");
        printData();
        logger.info("-------------- 数据打印结束2 --------------");
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
