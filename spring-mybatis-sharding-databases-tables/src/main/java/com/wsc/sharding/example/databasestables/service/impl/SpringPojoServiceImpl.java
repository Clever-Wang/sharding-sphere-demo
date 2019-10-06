package com.wsc.sharding.example.databasestables.service.impl;

import com.wsc.sharding.example.databasestables.dao.OrderItemRepository;
import com.wsc.sharding.example.databasestables.dao.OrderRepository;
import com.wsc.sharding.example.databasestables.entity.Order;
import com.wsc.sharding.example.databasestables.entity.OrderItem;
import com.wsc.sharding.example.databasestables.service.SpringPojoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

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

    @SuppressWarnings("all")
    private List<Long> insertData() {
        logger.info("---------------------------- 插入数据 ----------------------------");
        List<Long> result = new ArrayList<>(10);
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        final CountDownLatch countDownLatch = new CountDownLatch(10);

        //循环创建10个线程,使用 CyclicBarrier 来模拟并发请求
        for (int i = 1; i <= 10; i++) {
            final int userId = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //在此阻塞,如果线程数 达到10 开始执行 模拟10个并发请求
                        cyclicBarrier.await();
                        Order order = new Order();
                        order.setUserId(userId);
                        order.setStatus("INSERT_TEST");
                        orderRepository.insert(order);
                        OrderItem item = new OrderItem();
                        item.setOrderId(order.getOrderId());
                        item.setUserId(userId);
                        item.setStatus("INSERT_TEST");
                        orderItemRepository.insert(item);
                        result.add(order.getOrderId());
                    } catch (Exception e) {
                        logger.error("线程阻塞异常",e);
                    }finally {
                        //一个线程执行完 减1
                        countDownLatch.countDown();
                    }
                }
            }).start();
        }

        //如果10个线程都没有执行完,则主线程在此阻塞,防止主线程提前结束,子线程无法运行
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("线程在此阻塞,减到0就放行");
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
