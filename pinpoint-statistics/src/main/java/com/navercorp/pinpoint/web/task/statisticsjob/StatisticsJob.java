//package com.navercorp.pinpoint.web.batch.statisticsjob;
//
//import com.navercorp.pinpoint.web.dao.hbase.HbaseApplicationTraceIndexDao;
//import com.navercorp.pinpoint.web.dao.hbase.HbaseTraceDaoV2;
//import com.navercorp.pinpoint.web.dao.mongo.LinkStatisticsRepository;
//import com.navercorp.pinpoint.web.dao.mongo.MethodStatisticsRepository;
//import com.navercorp.pinpoint.web.service.AgentInfoService;
//import com.navercorp.pinpoint.web.service.SpanService;
//import com.navercorp.pinpoint.web.service.TransactionInfoService;
//import com.navercorp.pinpoint.web.vo.ApplicationAgentList;
//import com.navercorp.pinpoint.web.vo.ApplicationAgentsList;
//import com.navercorp.pinpoint.web.vo.Range;
//import org.apache.commons.collections.MapUtils;
//import org.apache.commons.lang3.time.DateUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.Date;
//import java.util.concurrent.atomic.AtomicLong;
//
///**
// * @author chunchun.xu on 2018/11/28.
// * @version 1.0
// * @descripte
// */
//@Component
//public class StatisticsJob {
//	private final Logger logger = LoggerFactory.getLogger(this.getClass());
//
//
//	@Autowired
//	private AgentInfoService agentInfoService;
//
//	@Autowired
//	public HbaseApplicationTraceIndexDao hbaseApplicationTraceIndexDao;
//
//	@Autowired
//	public HbaseTraceDaoV2 hbaseTraceDaoV2;
//
//	@Autowired
//	SpanService spanService;
//	@Autowired
//	LinkStatisticsRepository linkStatisticsRepository;
//	@Autowired
//	MethodStatisticsRepository methonStatisticsRepository;
//	@Autowired
//	TransactionInfoService transactionInfoService;
//	@Resource
//	StatisticsAsync statisticsAsync;
//
//	final AtomicLong processTimes = new AtomicLong();
//	Date lastDate = DateUtils.addMinutes(new Date(), -1);
//
//
//	@Scheduled(cron = "0 0/1 * * * ?")
//	public void statisticsJob() {
//		logger.info("start job ,runs {}", processTimes.getAndAdd(1));
//		long timestamp = System.currentTimeMillis();
//		Range range = new Range(lastDate.getTime(), timestamp);
//		ApplicationAgentsList applicationAgentList = agentInfoService.getAllApplicationAgentsList(ApplicationAgentsList.Filter.NONE, timestamp);
//		for (ApplicationAgentList application : applicationAgentList.getApplicationAgentLists()) {
//			statisticsAsync.saveApplicationStatistics(range, application);
//		}
//		lastDate = new Date(timestamp);
//		logger.info("job end");
//	}
//
//	@Scheduled(cron = "0 0 0 0/1 * ?")
//	public void clearnStatistic() {
//		logger.info("clearn job start");
//		statisticsAsync.statisticsMap.clear();
//	}
//
//
//	@Scheduled(cron = "0 0/1 * * * ?")
//	public void saveStatistic() {
//		logger.info("saveStatistic job start");
//		if (MapUtils.isEmpty(statisticsAsync.statisticsMap)) {
//			return;
//		}
//
//		statisticsAsync.statisticsMap.forEach((k, v) ->
//		{
//			if (DateUtils.addMinutes(new Date(), -1).before(v.getUpdateTime())) {
//				methonStatisticsRepository.save(v);
//			}
//		});
//	}
//}
