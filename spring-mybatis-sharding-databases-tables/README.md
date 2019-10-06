# 第二节: 入门demo使用spring+mybatis分库分表

### 1.资源介绍

**共两个数据库:**
 
 `demo_ds_0` 和 `demo_ds_1` 

**共8张表:**
 
每个库对应 `t_order_0` 、 `t_order_1` 、`t_order_item_0` 、 `t_order_item_1`

**分库分表规则:** 

根据`user_id`取模分库, 且根据`order_id`取模分表的两库两表的配置。

### 2.执行src/main/resources/db.sql

执行sql创建数据库

```sql
DROP SCHEMA IF EXISTS demo_ds_0;
DROP SCHEMA IF EXISTS demo_ds_1;
CREATE SCHEMA IF NOT EXISTS demo_ds_0;
CREATE SCHEMA IF NOT EXISTS demo_ds_1;
```

### 3.配置src/main/resources/spring/application-sharding-databases-tables.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding"
       xmlns:bean="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd">
    <!-- 扫描包 -->
    <context:component-scan base-package="com.wsc.sharding.example.databasestables" />

    <!-- 数据库ds_0 使用的是日本开源号称最快的数据源 "光" -->
    <bean id="demo_ds_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_0?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>

    <!-- 数据库ds_1 使用的是日本开源号称最快的数据源 "光" -->
    <bean id="demo_ds_1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_1?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>

    <!--
    行表达式分片策略
    对应InlineShardingStrategy。使用Groovy的表达式，提供对SQL语句中的=和IN的分片操作支持，只支持单分片键。
    对于简单的分片算法，可以通过简单的配置使用，从而避免繁琐的Java代码开发，
    如: t_user_$->{u_id % 8} 表示t_user表根据u_id模8，而分成8张表，表名称为t_user_0到t_user_7。

    sharding-column 分片列名称
    algorithm-expression 分片算法行表达式，需符合groovy语法 根据 user_id 取模2  路由到 demo_ds_0 数据库 或者 demo_ds_1 数据库
    -->
    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="demo_ds_${user_id % 2}" />
    <!--
    sharding-column 分片列名称
    algorithm-expression 分片算法行表达式，需符合groovy语法 根据 order_id 取模2  路由到 t_order_0 数据库 或者 t_order_1 数据库
    -->
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order_${order_id % 2}" />
    <!--
    sharding-column 分片列名称
    algorithm-expression 分片算法行表达式，需符合groovy语法 根据 order_id 取模2  路由到 t_order_item_0 数据库 或者 t_order_item_1 数据库
    -->
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_id" algorithm-expression="t_order_item_${order_id % 2}" />

    <bean:properties id="properties">
        <prop key="worker.id">123</prop>
        <prop key="max.tolerate.time.difference.milliseconds">0</prop>
    </bean:properties>

    <!--
    分布式主键生成策略
    column 自增列名称
    type   自增列值生成器类型，可自定义或选择内置类型：SNOWFLAKE/UUID
    -->
    <!--
    Sharding-JDBC采用snowflake算法作为默认的分布式分布式自增主键策略，用于保证分布式的情况下可以无中心化的生成不重复的自增序列。
    因此自增主键可以保证递增，但无法保证连续。而snowflake算法的最后4位是在同一毫秒内的访问递增值。
    因此，如果毫秒内并发度不高，最后4位为零的几率则很大。因此并发度不高的应用生成偶数主键的几率会更高。
    所以 直接运行 官网的demo 是看不出效果的 ,跟 上一节分库没什么区别,看不出分库分表的效果。
    为了模拟并发,使用 CyclicBarrier 具体查看代码
    -->
    <sharding:key-generator id="orderKeyGenerator" type="SNOWFLAKE" column="order_id" props-ref="properties" />
    <sharding:key-generator id="itemKeyGenerator" type="SNOWFLAKE" column="order_item_id" props-ref="properties" />
    
    <sharding:data-source id="shardingDataSource">
        <!-- 数据源Bean列表，多个Bean以逗号分隔 -->
        <sharding:sharding-rule data-source-names="demo_ds_0, demo_ds_1">
            <sharding:table-rules>
                <!--
                logic-table 逻辑表名称
                actual-data-nodes 真实数据节点，由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式 如： demo_ds_0.t_order_0
                database-strategy-ref 数据库分片策略，对应<sharding:xxx-strategy>中的策略Id，缺省表示使用<sharding:sharding-rule />配置的默认数据库分片策略
                table-strategy-ref 分表策略，对应<sharding:xxx-strategy>中的略id，不填则使用<sharding:sharding-rule/>配置的default-table-strategy-ref
                key-generator-ref 自增列值生成器引用，缺省表示使用默认自增列值生成器
                -->
                <sharding:table-rule logic-table="t_order" actual-data-nodes="demo_ds_${0..1}.t_order_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" key-generator-ref="orderKeyGenerator" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="demo_ds_${0..1}.t_order_item_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" key-generator-ref="itemKeyGenerator" />
            </sharding:table-rules>
            <!-- 绑定表规则 -->
            <sharding:binding-table-rules>
                <sharding:binding-table-rule logic-tables="t_order,t_order_item"/>
            </sharding:binding-table-rules>
        </sharding:sharding-rule>
        <sharding:props>
            <!-- 是否开启SQL显示，默认为false不开启 改为true 方便查看日志 -->
            <prop key="sql.show">true</prop>
        </sharding:props>
    </sharding:data-source>

    <!-- 事务相关配置 -->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="shardingDataSource" />
    </bean>
    <tx:annotation-driven />
    
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="shardingDataSource"/>
        <property name="mapperLocations" value="classpath*:mappers/*.xml"/>
    </bean>
    
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="com.wsc.sharding.example.databasestables.dao.impl"/>
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>
</beans>
```

### 4.运行程序测试结果

`注意`: 

```
Sharding-JDBC采用snowflake算法作为默认的分布式分布式自增主键策略，用于保证分布式的情况下可以无中心化的生成不重复的自增序列。
因此自增主键可以保证递增，但无法保证连续。而snowflake算法的最后4位是在同一毫秒内的访问递增值。所以，如果毫秒内并发度不高，最后4位为零的几率则很大。因此并发度不高的应用生成偶数主键的几率会更高。
    
所以 直接运行 官网的demo 是看不出效果的 ,跟 上一节分库没什么区别,看不出分库分表的效果。
    
为了模拟并发,使用 CyclicBarrier 具体查看代码。
```

为了方便测试,直接启动一个spring容器,执行service方法,运行程序之后的结果为:

`user_id` 为奇数,则插入 `demo_ds_1` 且 `order_id` 为奇数则插入 `t_order_1` 为偶数则插入 `t_order_0` 。

`user_id` 为偶数,则插入 `demo_ds_0` 且 `order_id` 为奇数则插入 `t_order_1` 为偶数则插入 `t_order_0` 。

每个数据库中有5个订单 对应 5个订单项。