package com.navercorp.pinpoint.web.vo.link;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @author chunchun.xu on 2018/11/22.
 * @version 1.0
 * @descripte 单次调用链路
 */
@Data
public class Link {
	@Id
	String id;
	String className;
	String methonName;
	List<Method> methonList;
}
