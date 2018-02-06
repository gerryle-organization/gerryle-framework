package com.gerryle.framework.mq;

import java.util.Map;
import java.util.Properties;

import ch.qos.logback.core.util.FileUtil;
import kafka.javaapi.consumer.ConsumerConnector;

/**
 * 消费者基类
 * @author gerryle 2018年2月6日 下午3:21:47
 */
public abstract class BaseConsumer implements Runnable {

	private ConsumerConnector consumer;
	private Map<String, Integer> topicCountMap;
	
	/**
	 * 线程数
	 * @return
	 * @author Gerryle 2018年2月6日 下午3:37:47
	 */
	public int nThreads(){
		return 1;
	}
	
	/**
	 * 开始消费
	 * @author Gerryle 2018年2月6日 下午3:38:36
	 */
	public void start(){
		new Thread(this).run();
	}
	
	/**
	 * 对应topic
	 * @return
	 * @author Gerryle 2018年2月6日 下午3:39:17
	 */
	public abstract String topic();
	
	 public BaseConsumer() {
		Properties properties=FileUtil
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
