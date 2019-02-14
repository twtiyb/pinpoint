package com.navercorp.pinpoint.web.vo.statistics;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author chunchun.xu on 2018/11/22.
 * @version 1.0
 * @descripte 调用链路明细
 */
@Data
public class LinkHistory {
	@Id
	String id;
	String LinkId;
	String agentId;
	Date createTime;
	BigDecimal elapsed;
}
