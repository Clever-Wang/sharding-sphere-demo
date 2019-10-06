package com.wsc.sharding.example.masterslave.dao.common;

import java.util.List;

/**
 * 其他dao层实现该接口
 * @param <T>
 * @param <P>
 */
public interface CommonRepository<T, P> {

    /**
     * 如果不存在创建表
     */
    void createTableIfNotExists();

    /**
     * 删除表
     */
    void dropTable();

    /**
     * 清空表
     */
    void truncateTable();

    /**
     * 插入操作
     * @param entity 实体类
     * @return 自增主键
     */
    Long insert(T entity);

    /**
     * 删除操作
     * @param key key
     */
    void delete(P key);

    /**
     * 查询所有数据
     * @return 返回一个List
     */
    List<T> selectAll();
}
