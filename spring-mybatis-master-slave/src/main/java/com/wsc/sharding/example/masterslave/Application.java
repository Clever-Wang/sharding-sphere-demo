package com.wsc.sharding.example.masterslave;

import com.wsc.sharding.example.masterslave.service.SpringPojoService;
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

	private static final String CONFIG_FILE = "spring/application-master-slave.xml";

	public static void main(String[] args) throws InterruptedException {

		try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(CONFIG_FILE)) {
			//获取要执行的Service
			SpringPojoService commonService = applicationContext.getBean(SpringPojoService.class);
			//初始化数据库
			commonService.initEnvironment();
			//插入数据
			commonService.process();
			//打印数据
			commonService.printData();
		}catch (Throwable e) {
			logger.error(" spring-mybatis-sharding-databases start fail ",e);
			System.exit(0);
		}

	}
	
}
