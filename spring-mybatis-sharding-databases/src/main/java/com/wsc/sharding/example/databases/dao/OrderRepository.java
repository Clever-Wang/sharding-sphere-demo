package com.wsc.sharding.example.databases.dao;


import com.wsc.sharding.example.databases.dao.common.CommonRepository;
import com.wsc.sharding.example.databases.entity.Order;

/**
 * 订单
 */
public interface OrderRepository extends CommonRepository<Order, Long> {
}
