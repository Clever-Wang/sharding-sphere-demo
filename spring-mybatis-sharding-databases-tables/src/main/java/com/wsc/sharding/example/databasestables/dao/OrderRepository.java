package com.wsc.sharding.example.databasestables.dao;


import com.wsc.sharding.example.databasestables.dao.common.CommonRepository;
import com.wsc.sharding.example.databasestables.entity.Order;

/**
 * 订单
 */
public interface OrderRepository extends CommonRepository<Order, Long> {
}
