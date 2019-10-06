package com.wsc.sharding.example.masterslave.service;

/**
 * 为了方便 就叫 XXXXService
 */
public interface SpringPojoService {

    /**
     * 初始化环境
     * 例如: 建表,清理数据等
     */
    void initEnvironment();

    /**
     * 清除
     * 例如: 删除表
     */
    void cleanEnvironment();

    /**
     * 执行逻辑
     * 例如: 创建订单 创建订单项入库
     */
    void process();

    /**
     * 打印数据
     */
    void printData();
}
