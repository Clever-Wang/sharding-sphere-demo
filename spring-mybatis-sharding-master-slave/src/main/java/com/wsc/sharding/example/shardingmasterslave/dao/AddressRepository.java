package com.wsc.sharding.example.shardingmasterslave.dao;


import com.wsc.sharding.example.shardingmasterslave.dao.common.CommonRepository;
import com.wsc.sharding.example.shardingmasterslave.entity.Address;

/**
 * 广播表
 */
public interface AddressRepository extends CommonRepository<Address, Long> {
}
