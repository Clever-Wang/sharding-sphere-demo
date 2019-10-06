package com.wsc.sharding.example.masterslave.dao;


import com.wsc.sharding.example.masterslave.dao.common.CommonRepository;
import com.wsc.sharding.example.masterslave.entity.Order;

/**
 * 订单
 */
public interface OrderRepository extends CommonRepository<Order, Long> {
}
