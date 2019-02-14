package com.navercorp.pinpoint.web.vo.statistics;

import com.navercorp.pinpoint.web.vo.link.Method;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * @author chunchun.xu on 2018/11/22.
 * @version 1.0
 * @descripte
 */
@Data
public class MethodStatistics {
	@Id
	String id;
	Method method;
	Long callTimes;
	Long totalElapsed;
	Double avgElapsed;
	Date createTime;
	Date updateTime;
}
