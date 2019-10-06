package com.wsc.sharding.example.masterslave.dao;


import com.wsc.sharding.example.masterslave.dao.common.CommonRepository;
import com.wsc.sharding.example.masterslave.entity.OrderItem;

public interface OrderItemRepository extends CommonRepository<OrderItem, Long> {
}
