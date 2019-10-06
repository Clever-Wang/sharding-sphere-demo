package com.wsc.sharding.example.shardingmasterslave.dao;


import com.wsc.sharding.example.shardingmasterslave.dao.common.CommonRepository;
import com.wsc.sharding.example.shardingmasterslave.entity.Order;

/**
 * 订单
 */
public interface OrderRepository extends CommonRepository<Order, Long> {
}
