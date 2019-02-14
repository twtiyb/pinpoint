package com.navercorp.pinpoint.web.vo.api;

import com.navercorp.pinpoint.web.vo.link.Link;
import lombok.Data;
import org.springframework.data.annotation.Id;


/**
 * @author chunchun.xu on 2018/11/22.
 * @version 1.0
 * @descripte
 */
@Data
public class Api {
	@Id
	String id;
	String url;
	String params;
	String releateLink;
	Link link;
}
