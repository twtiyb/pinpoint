package com.navercorp.pinpoint.web.vo.statistics;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * @author chunchun.xu on 2018/11/22.
 * @version 1.0
 * @descripte
 */
@Data
public class LinkStatistics {
	@Id
	String id;
	String methodId;
	String transactionId;
	Date createTime;
}
