DROP SCHEMA IF EXISTS demo_ds_master;
DROP SCHEMA IF EXISTS demo_ds_slave_0;
DROP SCHEMA IF EXISTS demo_ds_slave_1;
CREATE SCHEMA IF NOT EXISTS demo_ds_master;
CREATE SCHEMA IF NOT EXISTS demo_ds_slave_0;
CREATE SCHEMA IF NOT EXISTS demo_ds_slave_1;

-- 因为我没有对mysql做主从同步,所以 手动执行下面的sql到 两个 slave 数据库中。
-- 运行程序,可以看到：无论主库中的内容如何变化,查询操作 都是读取的 从库中的内容。

---------  下面是 demo_ds_slave_0 库的 sql 语句  ---------
-- demo_ds_slave_0 从库0 t_order 表
SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `order_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
BEGIN;
INSERT INTO `t_order` VALUES ('1', '1', 'INSERT_TEST_SLAVE0'), ('2', '2', 'INSERT_TEST_SLAVE0'), ('3', '3', 'INSERT_TEST_SLAVE0'), ('4', '4', 'INSERT_TEST_SLAVE0'), ('5', '5', 'INSERT_TEST_SLAVE0'), ('6', '6', 'INSERT_TEST_SLAVE0'), ('7', '7', 'INSERT_TEST_SLAVE0'), ('8', '8', 'INSERT_TEST_SLAVE0'), ('9', '9', 'INSERT_TEST_SLAVE0'), ('10', '10', 'INSERT_TEST_SLAVE0');
COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

-- demo_ds_slave_0 从库0 t_order_item 表
SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item` (
  `order_item_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`order_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
BEGIN;
INSERT INTO `t_order_item` VALUES ('1', '1', '1', 'INSERT_TEST_SLAVE0'), ('2', '2', '2', 'INSERT_TEST_SLAVE0'), ('3', '3', '3', 'INSERT_TEST_SLAVE0'), ('4', '4', '4', 'INSERT_TEST_SLAVE0'), ('5', '5', '5', 'INSERT_TEST_SLAVE0'), ('6', '6', '6', 'INSERT_TEST_SLAVE0'), ('7', '7', '7', 'INSERT_TEST_SLAVE0'), ('8', '8', '8', 'INSERT_TEST_SLAVE0'), ('9', '9', '9', 'INSERT_TEST_SLAVE0'), ('10', '10', '10', 'INSERT_TEST_SLAVE0');
COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

---------  下面是 demo_ds_slave_1 库的 sql 语句  ---------

-- demo_ds_slave_1 从库1 t_order 表
SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `order_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
BEGIN;
INSERT INTO `t_order` VALUES ('1', '1', 'INSERT_TEST_SLAVE1'), ('2', '2', 'INSERT_TEST_SLAVE1'), ('3', '3', 'INSERT_TEST_SLAVE1'), ('4', '4', 'INSERT_TEST_SLAVE1'), ('5', '5', 'INSERT_TEST_SLAVE1'), ('6', '6', 'INSERT_TEST_SLAVE1'), ('7', '7', 'INSERT_TEST_SLAVE1'), ('8', '8', 'INSERT_TEST_SLAVE1'), ('9', '9', 'INSERT_TEST_SLAVE1'), ('10', '10', 'INSERT_TEST_SLAVE1');
COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

-- demo_ds_slave_1 从库1 t_order_item 表
SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item` (
  `order_item_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`order_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
BEGIN;
INSERT INTO `t_order_item` VALUES ('1', '1', '1', 'INSERT_TEST_SLAVE1'), ('2', '2', '2', 'INSERT_TEST_SLAVE1'), ('3', '3', '3', 'INSERT_TEST_SLAVE1'), ('4', '4', '4', 'INSERT_TEST_SLAVE1'), ('5', '5', '5', 'INSERT_TEST_SLAVE1'), ('6', '6', '6', 'INSERT_TEST_SLAVE1'), ('7', '7', '7', 'INSERT_TEST_SLAVE1'), ('8', '8', '8', 'INSERT_TEST_SLAVE1'), ('9', '9', '9', 'INSERT_TEST_SLAVE1'), ('10', '10', '10', 'INSERT_TEST_SLAVE1');
COMMIT;
SET FOREIGN_KEY_CHECKS = 1;