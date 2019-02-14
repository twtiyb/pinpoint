package com.navercorp.pinpoint.web.vo.link;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Set;


/**
 * @author chunchun.xu on 2018/11/22.
 * @version 1.0
 * @descripte
 */
@Data
public class Method {
	@Id
	String id;
	String serviceType;
	Set<String> parentIds;
	String annotation;
}
