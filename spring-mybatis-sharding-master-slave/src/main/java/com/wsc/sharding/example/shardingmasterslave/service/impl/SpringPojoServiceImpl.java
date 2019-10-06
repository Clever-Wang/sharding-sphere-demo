package com.wsc.sharding.example.shardingmasterslave.service.impl;

import com.wsc.sharding.example.shardingmasterslave.dao.AddressRepository;
import com.wsc.sharding.example.shardingmasterslave.dao.OrderItemRepository;
import com.wsc.sharding.example.shardingmasterslave.dao.OrderRepository;
import com.wsc.sharding.example.shardingmasterslave.entity.Address;
import com.wsc.sharding.example.shardingmasterslave.entity.Order;
import com.wsc.sharding.example.shardingmasterslave.entity.OrderItem;
import com.wsc.sharding.example.shardingmasterslave.service.SpringPojoService;
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

    @Resource
    private AddressRepository addressRepository;

    @Override
    public void initEnvironment() {
        logger.info("-------------- 开始初始化 ---------------");
        //如果订单表不存在,创建表
        orderRepository.createTableIfNotExists();
        //如果订单项表不存在,创建订单项表
        orderItemRepository.createTableIfNotExists();
        //清除表中的数据
        orderRepository.truncateTable();
        //清除表中的数据
        orderItemRepository.truncateTable();
        //初始化 广播表
        initAddressTable();
        logger.info("-------------- 初始化结束 ---------------");
    }

    /**
     * 广播表 会在两个主库内 同时创建。
     */
    private void initAddressTable() {
        addressRepository.createTableIfNotExists();
        addressRepository.truncateTable();
        for (int i = 1; i <= 10; i++) {
            Address entity = new Address();
            entity.setAddressId((long) i);
            entity.setAddressName("address_" + String.valueOf(i));
            addressRepository.insert(entity);
        }
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

        // 在一个事务方法中，查询sql默认会走slave库,但是一旦遇到insert/update/delete语句，
        // 就会设置一个查询主库的标志，isMasterRoute()==true，这时候，Connection为主库的连接，
        // 并且引擎会强制设置DML_FLAG的值为true， 而这个对象是存在ThreadLocal中的，
        // 那么这样一个请求后续的所有读操作都会走主库。
        // 事务方法执行完成之后,AOP结束后会清除 ThreadLocal 中的属性 后续的查询操作会走 从库。
        logger.info("-------------- 开始执行插入操作 ---------------");
        List<Long> orderIds = insertData();
        logger.info("-------------- 插入操作执行结束 --------------");

        logger.info("-------------- 开始打印数据 --------------");
        printData();
        logger.info("-------------- 打印数据结束 --------------");

        logger.info("-------------- 开始删除数据 --------------");
        deleteData(orderIds);
        logger.info("-------------- 删除数据结束 --------------");

        //上面的sql执行都没问题,因为最开始执行完 insertData 操作后，
        //后面的打印sql 和 删除sql 都是走的主库,但是该方法结束后,main 方法内打印操作会报异常
        //因为 我没有做 mysql 数据同步,从库内没有表 所以报错。
    }

    private List<Long> insertData() {
        logger.info("---------------------------- 插入数据 ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setAddressId(i);
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

    private void deleteData(final List<Long> orderIds) {
        logger.info("---------------------------- 删除数据 ----------------------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
            orderItemRepository.delete(each);
        }
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
