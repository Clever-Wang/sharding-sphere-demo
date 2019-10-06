package com.wsc.sharding.example.masterslave.dao.impl;

import com.wsc.sharding.example.masterslave.dao.OrderRepository;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MybatisOrderRepository extends OrderRepository {
}
