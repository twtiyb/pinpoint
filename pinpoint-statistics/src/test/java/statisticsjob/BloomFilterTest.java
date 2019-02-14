//package com.navercorp.pinpoint.web.batch.statisticsjob;
//
//import com.google.common.base.Charsets;
//import com.google.common.hash.BloomFilter;
//import com.google.common.hash.Funnel;
//import com.google.common.hash.PrimitiveSink;
//import lombok.extern.log4j.Log4j;
//import org.apache.commons.lang3.RandomUtils;
//import org.junit.Assert;
//import org.junit.Test;
//
///**
// * @author chunchun.xu on 2018/12/21.
// * @version 1.0
// * @descripte
// */
//@Log4j
//public class BloomFilterTest {
//	final BloomFilter<String> filter = BloomFilter.create(new Funnel<String>() {
//		private static final long serialVersionUID = 1L;
//
//		@Override
//		public void funnel(String arg0, PrimitiveSink arg1) {
//			arg1.putString(arg0, Charsets.UTF_8);
//		}
//	}, 1024 * 1024 * 32, 0.0000001d);
//
//	@Test
//	public void bloomFilterBatchTest() {
//		int i = 0, m = 0;
//		for (; i < 9999999; i++) {
//			String str = String.valueOf(RandomUtils.nextInt());
//			if (filter.mightContain(str)) {
//				m++;
//			} else {
//				filter.put(str);
//			}
//		}
//		log.debug("错误率 " + (m++ / Double.valueOf(i)));
//	}
//
//	@Test
//	public void bloomFilterAssertTest() {
//		filter.put("abc");
//		filter.put("abc2");
//		filter.put("abc3");
//
//		Assert.assertTrue(filter.mightContain("abc"));
//		Assert.assertTrue(filter.mightContain("abc2"));
//		Assert.assertTrue(filter.mightContain("abc3"));
//		Assert.assertFalse(filter.mightContain("abc4"));
//	}
//}
