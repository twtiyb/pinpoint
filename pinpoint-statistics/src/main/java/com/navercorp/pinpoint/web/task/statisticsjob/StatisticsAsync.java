//package com.navercorp.pinpoint.web.batch.statisticsjob;
//
//import com.google.common.base.Charsets;
//import com.google.common.collect.Sets;
//import com.google.common.hash.BloomFilter;
//import com.google.common.hash.Funnel;
//import com.google.common.hash.PrimitiveSink;
//import com.navercorp.pinpoint.common.util.TransactionId;
//import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
//import com.navercorp.pinpoint.web.dao.hbase.HbaseApplicationTraceIndexDao;
//import com.navercorp.pinpoint.web.dao.hbase.HbaseTraceDaoV2;
//import com.navercorp.pinpoint.web.dao.mongo.LinkStatisticsRepository;
//import com.navercorp.pinpoint.web.dao.mongo.MethodRepository;
//import com.navercorp.pinpoint.web.dao.mongo.MethodStatisticsRepository;
//import com.navercorp.pinpoint.web.service.SpanResult;
//import com.navercorp.pinpoint.web.service.SpanService;
//import com.navercorp.pinpoint.web.service.TransactionInfoServiceImpl;
//import com.navercorp.pinpoint.web.vo.ApplicationAgentList;
//import com.navercorp.pinpoint.web.vo.Range;
//import com.navercorp.pinpoint.web.vo.callstacks.AnnotationRecord;
//import com.navercorp.pinpoint.web.vo.callstacks.Record;
//import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;
//import com.navercorp.pinpoint.web.vo.link.Method;
//import com.navercorp.pinpoint.web.vo.statistics.LinkStatistics;
//import com.navercorp.pinpoint.web.vo.statistics.MethodStatistics;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Example;
//import org.springframework.data.domain.ExampleMatcher;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * @author chunchun.xu on 2018/11/28.
// * @version 1.0
// * @descripte
// */
//@Component
//public class StatisticsAsync {
//	private final Logger logger = LoggerFactory.getLogger(this.getClass());
//
//
//	@Autowired
//	public HbaseApplicationTraceIndexDao hbaseApplicationTraceIndexDao;
//
//	@Autowired
//	public HbaseTraceDaoV2 hbaseTraceDaoV2;
//
//	@Autowired
//	SpanService spanService;
//
//	@Autowired
//	MethodRepository methodRepository;
//	@Autowired
//	LinkStatisticsRepository linkStatisticsRepository;
//	@Autowired
//	MethodStatisticsRepository methonStatisticsRepository;
//	@Autowired
//	TransactionInfoServiceImpl transactionInfoService;
//
//	public final static Map<String, MethodStatistics> statisticsMap = new HashMap<>(10000);
//	final com.google.common.hash.BloomFilter<String> filter = BloomFilter.create(new Funnel<String>() {
//		private static final long serialVersionUID = 1L;
//
//		@Override
//		public void funnel(String arg0, PrimitiveSink arg1) {
//			arg1.putString(arg0, Charsets.UTF_8);
//		}
//	}, 1024 * 1024 * 32, 0.0000001d);
//
//	@Async
//	public void saveApplicationStatistics(Range range, ApplicationAgentList application) {
//		List<TransactionId> idList = hbaseApplicationTraceIndexDao.scanTraceIndex(application.getAgentInfos().get(0).getApplicationName(), range, 9999, false).getScanData();
//		idList = idList.stream().distinct().filter(transactionId ->
//				!filter.mightContain(transactionId.toString())
//		).collect(Collectors.toList());
//
//		idList.forEach(k -> filter.put(k.toString()));
//		logger.info("application:{},rangeStart{},range:{},transactionQty:{}", application.getGroupName(), range.getFromDateTime(), range.prettyToString(), idList.size());
//		idList.forEach(id -> countTransaction(id));
////		if (CollectionUtils.isNotEmpty(idList)) {
////			countTransactions(idList);
////		}
//	}
//
//	public void countTransactions(List<TransactionId> listIds) {
//		logger.debug("save transaction:{}", String.join(",", listIds.stream().map(TransactionId::toString).collect(Collectors.toList())));
//		List<SpanResult> results = spanService.selectSpans(listIds, 0);
//		results.forEach(k -> {
//			CallTreeIterator callTreeIterator = k.getCallTree();
//			saveRecordSet(transactionInfoService.createRecordSet(callTreeIterator, -1, callTreeIterator.next().getValue().getAgentId(), -1));
//		});
//
//	}
//
//	public void countTransaction(TransactionId transactionId) {
//		logger.debug("save transaction:{}", transactionId.toString());
//		SpanResult result = spanService.selectSpan(transactionId, 0);
//		CallTreeIterator callTreeIterator = result.getCallTree();
//		saveRecordSet(transactionInfoService.createRecordSet(callTreeIterator, -1, transactionId.getAgentId(), -1));
//	}
//
//	private String getAnnotation(Map<Integer, List<Record>> annotationMap, int parentId) {
//		if (CollectionUtils.isEmpty(annotationMap.get(parentId))) {
//			return "";
//		}
//
//		return StringUtils.join(annotationMap.get(parentId), ",");
//	}
//
//	private void saveRecordSet(RecordSet recordSet) {
//		List<Record> recordList = recordSet.getRecordList();
//		List<Record> methodList = recordList.stream().filter(k -> !(k instanceof AnnotationRecord)
//				&& StringUtils.isNotEmpty(k.getFullApiDescription()))
//				.collect(Collectors.toList());
//		Map<Integer, String> methodMap = recordList.stream().collect(Collectors.toMap(Record::getId, Record::getFullApiDescription));
//		Map<Integer, List<Record>> annotationMap = recordList.stream().filter(k -> k.getClass().getSimpleName().equals("AnnotationRecord")
////				&& k.get//排除 mysql
//		).collect(Collectors.groupingBy(Record::getParentId));
//
//		List<LinkStatistics> LinkStatisticsList = new ArrayList<>();
//		for (int i = 0; i < methodList.size(); i++) {
//			Record record = methodList.get(i);
//			String remark = record.getFullApiDescription();
//			if (!statisticsMap.containsKey(remark)) {
//				Method method = methodRepository.findOne(remark);
//				if (method == null) {
//					method = new Method();
//					method.setId(remark);
//					method.setAnnotation(getAnnotation(annotationMap, record.getId()));
//					method.setServiceType(record.getApiType());
//					method.setParentIds(Sets.newHashSet(methodMap.get(record.getParentId())));
//					method = methodRepository.save(method);
//
//					MethodStatistics methodStatistics = new MethodStatistics();
//					methodStatistics.setMethod(method);
//					methodStatistics.setCallTimes(0L);
//					methodStatistics.setTotalElapsed(0L);
//					methodStatistics.setAvgElapsed(0D);
//					methodStatistics.setCreateTime(new Date());
//					methodStatistics.setUpdateTime(methodStatistics.getCreateTime());
//					methodStatistics = methonStatisticsRepository.save(methodStatistics);
//					statisticsMap.put(remark, methodStatistics);
//				} else {
//					if (!method.getParentIds().contains(methodMap.get(record.getParentId()))) {
//						method.getParentIds().add(methodMap.get(record.getParentId()));
//						methodRepository.save(method);
//					}
//
//					MethodStatistics methodStatistics = new MethodStatistics();
//					Method paramMethod = new Method();
//					paramMethod.setId(method.getId());
//					methodStatistics.setMethod(paramMethod);
//
//					ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
//					methodStatistics = methonStatisticsRepository.findOne(Example.of(methodStatistics, matcher));
//					methodStatistics.setMethod(method);
//
//					statisticsMap.put(remark, methodStatistics);
//				}
//			}
//
//			//数据量有点儿大，只保留父级的
//			if (remark.contains("wwwarehouse") && !remark.contains("getMapper")) {
//				LinkStatistics linkStatistics = new LinkStatistics();
//				linkStatistics.setCreateTime(new Date());
//				linkStatistics.setMethodId(remark);
//				linkStatistics.setTransactionId(record.getTransactionId());
//
//				LinkStatisticsList.add(linkStatistics);
//			}
//
//			MethodStatistics methodStatistics = statisticsMap.get(remark);
//			methodStatistics.setCallTimes(methodStatistics.getCallTimes() + 1);
//			methodStatistics.setTotalElapsed(methodStatistics.getTotalElapsed() + record.getExecutionMilliseconds());
//			methodStatistics.setAvgElapsed((double) (methodStatistics.getTotalElapsed() / methodStatistics.getCallTimes()));
//			methodStatistics.setUpdateTime(new Date());
//
//			linkStatisticsRepository.insert(LinkStatisticsList);
//		}
//	}
//
//
//	private Set<Integer> ignoreMethod = Sets.newHashSet(
//			14 //找不到名字的
//			, 200//网关，固定的
//			, 10000013 //AnnotationBo{key=10000013, value=API-DynamicID not found. api:181, isAuthorized=true}
////			, -1 //redis 调用redis
//	);
//}
