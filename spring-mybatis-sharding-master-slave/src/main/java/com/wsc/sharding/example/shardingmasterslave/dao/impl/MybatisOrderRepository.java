package com.wsc.sharding.example.shardingmasterslave.dao.impl;

import com.wsc.sharding.example.shardingmasterslave.dao.OrderRepository;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MybatisOrderRepository extends OrderRepository {
}
