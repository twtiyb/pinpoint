//package com.navercorp.pinpoint.web.batch.statisticsjob;
//
//import com.navercorp.pinpoint.common.util.TransactionId;
//import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
//import com.navercorp.pinpoint.web.dao.mongo.LinkStatisticsRepository;
//import com.navercorp.pinpoint.web.service.AgentInfoService;
//import com.navercorp.pinpoint.web.service.SpanResult;
//import com.navercorp.pinpoint.web.service.SpanServiceImpl;
//import com.navercorp.pinpoint.web.service.TransactionInfoService;
//import com.navercorp.pinpoint.web.vo.ApplicationAgentList;
//import com.navercorp.pinpoint.web.vo.ApplicationAgentsList;
//import com.navercorp.pinpoint.web.vo.Range;
//import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.web.WebAppConfiguration;
//
//import javax.annotation.Resource;
//import java.util.Date;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * @author chunchun.xu on 2018/12/21.
// * @version 1.0
// * @descripte
// */
//@RunWith(JUnit4ClassRunner.class)
//@WebAppConfiguration
//@ContextConfiguration(locations = {"classpath:applicationContext-web.xml", "classpath:servlet-context.xml"})
//public class StatisticsAsyncTest {
//	@Resource
//	AgentInfoService agentInfoService;
//	@Resource
//	StatisticsAsync statisticsAsync;
//	@Resource
//	SpanServiceImpl spanService;
//	@Resource
//	LinkStatisticsRepository linkStatisticsRepository;
//	@Resource
//	TransactionInfoService transactionInfoService;
//	@Test
//	public void saveTransaction() {
//		TransactionId transactionId = new TransactionId("xuchun_test", 1547707160304L, 4L);
//		SpanResult result = spanService.selectSpan(transactionId, 0);
//		CallTreeIterator callTreeIterator = result.getCallTree();
//		RecordSet  set = transactionInfoService.createRecordSet(callTreeIterator, -1, transactionId.getAgentId(), -1);
//		System.out.println(set.toString());
//	}
//
//	@Test
//	public void testSaveOne() {
//	}
//	@Test
//	public void testSaveBatch() {
//
//	}
//
//	@Test
//	public void saceSignleApplication() throws InterruptedException {
//		Date from = new Date();
//		while (true) {
//			Date to = new Date();
//			ApplicationAgentsList applicationAgentList = agentInfoService.getAllApplicationAgentsList(ApplicationAgentsList.Filter.NONE, new Date().getTime());
//			List<ApplicationAgentList> list = applicationAgentList.getApplicationAgentLists().stream().filter(k ->
//					k.getGroupName().equals("uat_gateway")).collect(Collectors.toList());
//
//			for (ApplicationAgentList application : list) {
//				statisticsAsync.saveApplicationStatistics(new Range(from.getTime(), to.getTime()), application);
//			}
//			from = to;
//			Thread.sleep(1000);
//		}
//	}
//}