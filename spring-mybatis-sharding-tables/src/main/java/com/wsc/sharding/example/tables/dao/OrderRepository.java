package com.wsc.sharding.example.tables.dao;


import com.wsc.sharding.example.tables.dao.common.CommonRepository;
import com.wsc.sharding.example.tables.entity.Order;

/**
 * 订单
 */
public interface OrderRepository extends CommonRepository<Order, Long> {
}
