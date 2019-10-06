# 第五节: 入门demo使用spring+mybatis读写分离+分库分表

### 1.资源介绍

**共六个数据库:**

两个主库: `demo_ds_master_0` 和 `demo_ds_master_1` 

四个从库: `demo_ds_master_0_slave_0` 、 `demo_ds_master_0_slave_1` 和 `demo_ds_master_1_slave_0` 、 `demo_ds_master_1_slave_1`

**共30张表:** 

每个库5张表,从库是主库同步过去的。

一张广播表: `t_address`

四张普通表： `t_order_0` 、`t_order_1` 和 `t_order_item_0` 、`t_order_item_1`

**分库规则:** 

根据 `user_id` 取模分库, 订单根据 `order_id` 取模分表, 订单项根据 `order_item_id` 取模分表。


### 2.执行src/main/resources/db.sql

执行sql创建数据库

```sql
DROP SCHEMA IF EXISTS demo_ds_master_0;
DROP SCHEMA IF EXISTS demo_ds_master_0_slave_0;
DROP SCHEMA IF EXISTS demo_ds_master_0_slave_1;
DROP SCHEMA IF EXISTS demo_ds_master_1;
DROP SCHEMA IF EXISTS demo_ds_master_1_slave_0;
DROP SCHEMA IF EXISTS demo_ds_master_1_slave_1;
CREATE SCHEMA IF NOT EXISTS demo_ds_master_0;
CREATE SCHEMA IF NOT EXISTS demo_ds_master_0_slave_0;
CREATE SCHEMA IF NOT EXISTS demo_ds_master_0_slave_1;
CREATE SCHEMA IF NOT EXISTS demo_ds_master_1;
CREATE SCHEMA IF NOT EXISTS demo_ds_master_1_slave_0;
CREATE SCHEMA IF NOT EXISTS demo_ds_master_1_slave_1;
```

### 3.配置src/main/resources/spring/application-sharding-master-slave.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding"
       xmlns:master-slave="http://shardingsphere.apache.org/schema/shardingsphere/masterslave"
       xmlns:bean="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd 
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx 
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd">
    <!-- 扫描包 -->
    <context:component-scan base-package="com.wsc.sharding.example.shardingmasterslave" />

    <!-- 主库 master_0 -->
    <bean id="demo_ds_master_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_master_0?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>

    <!-- 主库 master_0 的从库 slave_0 -->
    <bean id="demo_ds_master_0_slave_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_master_0_slave_0?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>

    <!-- 主库 master_0 的从库 slave_1 -->
    <bean id="demo_ds_master_0_slave_1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_master_0_slave_1?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>

    <!-- 从库 master_1 -->
    <bean id="demo_ds_master_1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_master_1?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>

    <!-- 主库 master_1 的从库 slave_0 -->
    <bean id="demo_ds_master_1_slave_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_master_1_slave_0?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>

    <!-- 主库 master_1 的从库 slave_1 -->
    <bean id="demo_ds_master_1_slave_1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_master_1_slave_1?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>

    <!--
    行表达式分片策略
    对应InlineShardingStrategy。使用Groovy的表达式，提供对SQL语句中的=和IN的分片操作支持，只支持单分片键。
    对于简单的分片算法，可以通过简单的配置使用，从而避免繁琐的Java代码开发，
    如: t_user_$->{u_id % 8} 表示t_user表根据u_id模8，而分成8张表，表名称为t_user_0到t_user_7。

    sharding-column 分片列名称
    algorithm-expression 分片算法行表达式，需符合groovy语法 根据 user_id 取模2  路由到 demo_ds_ms_0 数据库 或者 demo_ds_ms_1 数据库
    -->
    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="demo_ds_ms_${user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order_${order_id % 2}" />
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_item_id" algorithm-expression="t_order_item_${order_item_id % 2}" />
    
    <bean:properties id="properties">
        <prop key="worker.id">123</prop>
        <prop key="max.tolerate.time.difference.milliseconds">0</prop>
    </bean:properties>

    <!--
    分布式主键生成策略
    column 自增列名称
    type   自增列值生成器类型，可自定义或选择内置类型：SNOWFLAKE/UUID
    -->
    <sharding:key-generator id="orderKeyGenerator" type="SNOWFLAKE" column="order_id" props-ref="properties" />
    <sharding:key-generator id="itemKeyGenerator" type="SNOWFLAKE" column="order_item_id" props-ref="properties" />

    <!-- 4.0.0-RC1 版本 负载均衡策略配置方式 -->
    <!-- <bean id="randomStrategy" class="org.apache.shardingsphere.example.spring.namespace.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" /> -->

    <!-- 4.0.0-RC2 之后版本 负载均衡策略配置方式 负载均衡算法类型，'RANDOM'或'ROUND_ROBIN' ，支持自定义拓展 -->
    <master-slave:load-balance-algorithm id="randomStrategy" type="RANDOM" />

    <sharding:data-source id="shardingDataSource">
        <!-- data-source-names 数据源Bean列表，多个Bean以逗号分隔 -->
        <sharding:sharding-rule data-source-names="demo_ds_master_0,demo_ds_master_0_slave_0,demo_ds_master_0_slave_1,demo_ds_master_1,demo_ds_master_1_slave_0,demo_ds_master_1_slave_1">
            <sharding:master-slave-rules>
                <!--
                master-data-source-name 主库数据源Bean Id
                slave-data-source-names 从库数据源Bean Id列表，多个Bean以逗号分隔
                strategy-ref 从库负载均衡算法引用。该类需实现 MasterSlaveLoadBalanceAlgorithm 接口
                -->
                <sharding:master-slave-rule id="demo_ds_ms_0" master-data-source-name="demo_ds_master_0" slave-data-source-names="demo_ds_master_0_slave_0, demo_ds_master_0_slave_1" strategy-ref="randomStrategy" />
                <sharding:master-slave-rule id="demo_ds_ms_1" master-data-source-name="demo_ds_master_1" slave-data-source-names="demo_ds_master_1_slave_0, demo_ds_master_1_slave_1" strategy-ref="randomStrategy" />
            </sharding:master-slave-rules>
            <sharding:table-rules>
                <!--
                logic-table 逻辑表名称
                actual-data-nodes 真实数据节点，由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式 如： demo_ds_ms_0.t_order_0
                database-strategy-ref 数据库分片策略，对应<sharding:xxx-strategy>中的策略Id，缺省表示使用<sharding:sharding-rule />配置的默认数据库分片策略
                table-strategy-ref 分表策略，对应<sharding:xxx-strategy>中的略id，不填则使用<sharding:sharding-rule/>配置的default-table-strategy-ref
                key-generator-ref 自增列值生成器引用，缺省表示使用默认自增列值生成器
                -->
                <sharding:table-rule logic-table="t_order" actual-data-nodes="demo_ds_ms_${0..1}.t_order_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" key-generator-ref="orderKeyGenerator"/>
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="demo_ds_ms_${0..1}.t_order_item_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" key-generator-ref="itemKeyGenerator"/>
            </sharding:table-rules>
            <sharding:binding-table-rules>
                <!-- 绑定表规则 -->
                <sharding:binding-table-rule logic-tables="t_order,t_order_item"/>
            </sharding:binding-table-rules>
            <sharding:broadcast-table-rules>
                <!-- 广播表规则 table 广播规则的表名 -->
                <sharding:broadcast-table-rule table="t_address"/>
            </sharding:broadcast-table-rules>
        </sharding:sharding-rule>
        <sharding:props>
            <!-- 是否开启SQL显示，默认值: false -->
            <prop key="sql.show">true</prop>
        </sharding:props>
    </sharding:data-source>
    
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="shardingDataSource" />
    </bean>
    <tx:annotation-driven />
    
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="shardingDataSource"/>
        <property name="mapperLocations" value="classpath*:mappers/*.xml"/>
    </bean>
    
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="com.wsc.sharding.example.shardingmasterslave.dao.impl"/>
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>
</beans>

```

### 4.运行程序测试结果

关于读写分离相关的事项,可以查看上一篇 [第四节: 入门demo使用spring+mybatis读写分离](https://github.com/Clever-Wang/sharding-sphere-demo/tree/master/spring-mybatis-master-slave#4%E8%BF%90%E8%A1%8C%E7%A8%8B%E5%BA%8F%E6%B5%8B%E8%AF%95%E7%BB%93%E6%9E%9C)

service方法中 执行插入  打印  删除 操作都是基于主库, service方法执行完成之后,到main 方法中,再次查询数据报错,因为从库没有表.原因同上一节。