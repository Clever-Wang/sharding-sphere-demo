# 第四节: 入门demo使用spring+mybatis读写分离

### 1.资源介绍

**共三个数据库:**

主库: `demo_ds_master` 两个从库:  `demo_ds_slave_0` 、`demo_ds_slave_1`

**共6张表:** 

每个库中有 `t_order` 、`t_order_item` 两张表。

**分库规则:** 

写操作走主库,读操作走从库

**注意：sharding sphere 读写分离不支持项**

- *主库和从库的数据同步。*
- *主库和从库的数据同步延迟导致的数据不一致。*
- *主库双写或多写。*

### 2.执行src/main/resources/db.sql

执行sql创建数据库

```sql
DROP SCHEMA IF EXISTS demo_ds_master;
DROP SCHEMA IF EXISTS demo_ds_slave_0;
DROP SCHEMA IF EXISTS demo_ds_slave_1;
CREATE SCHEMA IF NOT EXISTS demo_ds_master;
CREATE SCHEMA IF NOT EXISTS demo_ds_slave_0;
CREATE SCHEMA IF NOT EXISTS demo_ds_slave_1;
```

### 3.配置src/main/resources/spring/application-master-slave.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:master-slave="http://shardingsphere.apache.org/schema/shardingsphere/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd 
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx 
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd">
    <!-- 扫描包 -->
    <context:component-scan base-package="com.wsc.sharding.example.masterslave" />

    <!-- 主库 demo_ds_master -->
    <bean id="demo_ds_master" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_master?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>

    <!-- 从库 demo_ds_slave_0 -->
    <bean id="demo_ds_slave_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_slave_0?useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>

    <!-- 从库 demo_ds_slave_1 -->
    <bean id="demo_ds_slave_1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/demo_ds_slave_1?useSSL=false"/>
        <property name="username" value="root"/>
        <property name="password" value="123456"/>
    </bean>

    <!-- 4.0.0-RC1 版本 负载均衡策略配置方式 -->
    <!-- <bean id="randomStrategy" class="org.apache.shardingsphere.example.spring.namespace.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" /> -->

    <!-- 4.0.0-RC2 之后版本 负载均衡策略配置方式 负载均衡算法类型，'RANDOM'或'ROUND_ROBIN' ，支持自定义拓展 -->
    <master-slave:load-balance-algorithm id="randomStrategy" type="RANDOM" />
    <!--
     master-data-source-name 主库数据源Bean Id
     slave-data-source-names 从库数据源Bean Id列表，多个Bean以逗号分隔
     strategy-ref 从库负载均衡算法引用。该类需实现MasterSlaveLoadBalanceAlgorithm接口
     strategy-type 从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若strategy-ref存在则忽略该配置
     -->
    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="demo_ds_master" slave-data-source-names="demo_ds_slave_0, demo_ds_slave_1" strategy-ref="randomStrategy">
        <master-slave:props>
            <!-- 是否开启SQL显示，默认值: false -->
            <prop key="sql.show">true</prop>
        </master-slave:props>
    </master-slave:data-source>


    <!-- 事务配置 -->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="masterSlaveDataSource" />
    </bean>
    <tx:annotation-driven />
    
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="masterSlaveDataSource"/>
        <property name="mapperLocations" value="classpath*:mappers/*.xml"/>
    </bean>
    
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="com.wsc.sharding.example.masterslave.dao.impl"/>
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>
</beans>
```

### 4.运行程序测试结果

为了方便测试,直接启动一个spring容器,执行service方法,运行程序之后的结果为:

第一次执行报错,因为 sharding sphere 不支持 主库和从库的数据同步 ,执行查询操作的时候从库没有表和数据,所以报异常：
![image](https://raw.githubusercontent.com/Clever-Wang/sharding-sphere-demo/master/spring-mybatis-master-slave/a112.png)
然后执行 db.sql 中的语句 到两个从库,再次执行程序,就可以了,因为没有做主从数据同步,所以主库数据无论怎么改变,查询到从库的数据一直是那些数据。
![image](https://github.com/Clever-Wang/sharding-sphere-demo/blob/master/spring-mybatis-master-slave/a123.png?raw=true)
