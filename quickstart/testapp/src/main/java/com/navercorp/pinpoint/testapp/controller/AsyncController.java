package com.navercorp.pinpoint.testapp.controller;

import com.navercorp.pinpoint.testapp.service.remote.AsyncRemoteService;
import com.navercorp.pinpoint.testapp.util.Description;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller("asyncController")
@RequestMapping({"/async"})
public class AsyncController {
	private static final String EPL_LEAGUE_TABLE_URL = "http://api.football-data.org/v1/soccerseasons/398/leagueTable";
	@Resource
	AsyncRemoteService asyncRemoteService;

	public AsyncController() {
	}

	@RequestMapping({"/getAsyncWait3"})
	@ResponseBody
	@Description("Returns the server's current timestamp.")
	public Map<String, Object> getAsyncWait3() {
		Date date = null;

		try {
			date = this.asyncRemoteService.getByAsync("http://api.football-data.org/v1/soccerseasons/398/leagueTable");
		} catch (Exception var3) {
			var3.printStackTrace();
		}

		Map m1 = new HashMap();
		m1.put("date", date);
		return m1;
	}

	@RequestMapping({"/getDeepAsyncWait3"})
	@ResponseBody
	@Description("Returns the server's current timestamp.call async method ,the async method call async too.")
	public Map<String, Object> getDeepAsyncWait3() {
		Date date = null;

		try {
			date = this.asyncRemoteService.getByDeepAsync("http://api.football-data.org/v1/soccerseasons/398/leagueTable");
		} catch (Exception var3) {
			var3.printStackTrace();
		}

		Map m1 = new HashMap();
		m1.put("date", date);
		return m1;
	}
}
