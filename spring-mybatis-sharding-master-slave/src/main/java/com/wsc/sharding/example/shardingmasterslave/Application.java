package com.wsc.sharding.example.shardingmasterslave;

import com.wsc.sharding.example.shardingmasterslave.service.SpringPojoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: WangSaiChao
 * @date: 2019/8/13
 * @description: 启动类
 */
public class Application {

	private static Logger logger = LoggerFactory.getLogger(Application.class);

	private static final String CONFIG_FILE = "spring/application-sharding-master-slave.xml";

	public static void main(String[] args) throws InterruptedException {

		try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(CONFIG_FILE)) {
			//获取要执行的Service
			SpringPojoService commonService = applicationContext.getBean(SpringPojoService.class);
			//初始化数据库
			commonService.initEnvironment();
			//插入数据
			commonService.process();
			logger.info("----------- main方法内打印数据开始 -----------");
			//打印数据
			commonService.printData();
			logger.info("----------- main方法内打印数据结束 -----------");
		}catch (Throwable e) {
			logger.error(" spring-mybatis-sharding-master-slave start fail ",e);
			System.exit(0);
		}

	}
	
}
