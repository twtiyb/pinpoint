//package com.navercorp.pinpoint.web.batch.statisticsjob;
//
//import com.navercorp.pinpoint.web.service.AgentInfoService;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.web.WebAppConfiguration;
//
//import javax.annotation.Resource;
//
///**
// * @author chunchun.xu on 2018/11/28.
// * @version 1.0
// * @descripte
// */
//@RunWith(JUnit4ClassRunner.class)
//@WebAppConfiguration
//@ContextConfiguration(locations = {"classpath:applicationContext-web.xml", "classpath:servlet-context.xml"})
//public class StatisticsJobTest {
//
//	@Autowired
//	StatisticsJob statisticsJob;
//
//	@Autowired
//	private AgentInfoService agentInfoService;
//
//	@Resource
//	private StatisticsAsync statisticsAsync;
//
//	@Test
//	public void run() {
//		try {
//			Thread.sleep(10000000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
//}