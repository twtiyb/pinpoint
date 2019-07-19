/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.controller;

import com.google.gson.*;
import com.navercorp.pinpoint.common.util.*;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.service.FilteredMapService;
import com.navercorp.pinpoint.web.service.SpanResult;
import com.navercorp.pinpoint.web.service.SpanService;
import com.navercorp.pinpoint.web.service.TransactionInfoService;
import com.navercorp.pinpoint.web.util.DefaultMongoJsonParser;
import com.navercorp.pinpoint.web.util.DubboUtil;
import com.navercorp.pinpoint.web.util.MongoJsonParser;
import com.navercorp.pinpoint.web.util.OutputParameterMongoJsonParser;
import com.navercorp.pinpoint.web.view.TransactionInfoViewModel;
import com.navercorp.pinpoint.web.vo.Attachment;
import com.navercorp.pinpoint.web.vo.EsTransaction;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.util.MD5Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * @author emeroad
 * @author jaehong.kim
 */
@Controller
public class BusinessTransactionController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SpanService spanService;

    @Autowired
    private TransactionInfoService transactionInfoService;

    @Autowired
    private FilteredMapService filteredMapService;
    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    @Resource
    JestClient jestClient;
    @Value("#{pinpointWebProps['log.enable'] ?: false}")
    private boolean logLinkEnable;

    @Value("#{pinpointWebProps['log.button.name'] ?: ''}")
    private String logButtonName;

    @Value("#{pinpointWebProps['log.page.url'] ?: ''}")
    private String logPageUrl;

    @Value("#{pinpointWebProps['log.button.disable.message'] ?: ''}")
    private String disableButtonMessage;

    private SqlParser sqlParser = new DefaultSqlParser();
    private OutputParameterParser parameterParser = new OutputParameterParser();

    private MongoJsonParser mongoJsonParser = new DefaultMongoJsonParser();
    private OutputParameterMongoJsonParser parameterJsonParser = new OutputParameterMongoJsonParser();

    /**
     * info lookup for a selected transaction
     *
     * @param traceIdParam
     * @param focusTimestamp
     * @return
     */
    @RequestMapping(value = "/transactionInfo", method = RequestMethod.GET)
    @ResponseBody
    public TransactionInfoViewModel transactionInfo(@RequestParam("traceId") String traceIdParam,
                                                    @RequestParam(value = "focusTimestamp", required = false, defaultValue = "0") long focusTimestamp,
                                                    @RequestParam(value = "agentId", required = false) String agentId,
                                                    @RequestParam(value = "spanId", required = false, defaultValue = "-1") long spanId,
                                                    @RequestParam(value = "v", required = false, defaultValue = "0") int viewVersion) {
        logger.debug("GET /transactionInfo params {traceId={}, focusTimestamp={}, agentId={}, spanId={}, v={}}", traceIdParam, focusTimestamp, agentId, spanId, viewVersion);

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(traceIdParam);

        // select spans
        final SpanResult spanResult = this.spanService.selectSpan(transactionId, focusTimestamp);
        final CallTreeIterator callTreeIterator = spanResult.getCallTree();

        // application map
        ApplicationMap map = filteredMapService.selectApplicationMap(transactionId, viewVersion);
        RecordSet recordSet = this.transactionInfoService.createRecordSet(callTreeIterator, focusTimestamp, agentId, spanId);

        TransactionInfoViewModel result = new TransactionInfoViewModel(transactionId, map.getNodes(), map.getLinks(), recordSet, spanResult.getCompleteTypeString(), logLinkEnable, logButtonName, logPageUrl, disableButtonMessage);
        return result;
    }

    @RequestMapping(value = "/bind", method = RequestMethod.POST)
    @ResponseBody
    public String metaDataBind(@RequestParam("type") String type,
                               @RequestParam("metaData") String metaData,
                               @RequestParam("bind") String bind) {
        if (logger.isDebugEnabled()) {
            logger.debug("POST /bind params {metaData={}, bind={}}", metaData, bind);
        }

        if (metaData == null) {
            return "";
        }

        List<String> bindValues;
        String combinedResult = "";

        if (type.equals("sql")) {
            bindValues = parameterParser.parseOutputParameter(bind);
            combinedResult = sqlParser.combineBindValues(metaData, bindValues);
        } else if (type.equals("mongoJson")) {
            bindValues = parameterJsonParser.parseOutputParameter(bind);
            combinedResult = mongoJsonParser.combineBindValues(metaData, bindValues);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Combined result={}", combinedResult);
        }

        if (type.equals("mongoJson")) {
            return StringEscapeUtils.unescapeHtml4(combinedResult);
        }

        return StringEscapeUtils.escapeHtml4(combinedResult);
    }


    @RequestMapping(value = "/transactionIndexList", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, List<EsTransaction>> transactionIndexList(@RequestParam(name = "traceId", defaultValue = "", required = false) String traceId,
                                                                 @RequestParam(name = "userId", defaultValue = "", required = false) String userId,
                                                                 @RequestParam(name = "rpc", defaultValue = "", required = false) String rpc,
                                                                 @RequestParam(name = "applicationId", defaultValue = "", required = false) String applicationId,
                                                                 @RequestParam(name = "period", defaultValue = "", required = false) String period) {
        Range range = parseRange(period);

        //先暂时不加时间的限制，直接Index，  index 是以1天为准的
        JsonObject root = new JsonObject();
        JsonObject query = new JsonObject();
        JsonObject filter = new JsonObject();
        JsonObject bool = new JsonObject();
        JsonObject sort = new JsonObject();
        JsonObject order = new JsonObject();

        JsonArray mustArry = new JsonArray();
        JsonArray shouldArry = new JsonArray();
        setMatchQuery("traceId.keyword", traceId, mustArry);
        setMatchQuery("userId.keyword", userId, mustArry);
        setMatchQuery("applicationId.keyword", applicationId, mustArry);
        setFilterQuery("startTime", range, filter);
        setMultiMatchQuery(new String[]{"rpc_arr.keyword", "rpc.keyword"}, rpc, shouldArry);
        bool.add("must", mustArry);
        bool.add("should", shouldArry);
        bool.add("filter", filter);
        query.add("bool", bool);

        order.addProperty("order", "desc");
        sort.add("_score", order);
        sort.add("@timestamp", order);

        root.add("query", query);
        root.add("sort", sort);
        root.addProperty("size", "30");

        Search search = new Search.Builder(gson.toJson(root))
                .addIndex(String.join(",", getIndexList(range)))
                .build();
        List<EsTransaction> transactions = null;
        try {
            logger.info("search query: {},queryStr: {}", search.toString(), root.toString());
            transactions = jestClient.execute(search).getSourceAsObjectList(EsTransaction.class);
            transactions.forEach(k -> k.setApplication(k.getRpc()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map map = new HashMap();
        map.put("metadata", transactions);
        return map;
    }


    private List<String> getIndexList(Range range) {
        List<String> indexList = new ArrayList<>();
        Date date = org.apache.commons.lang3.time.DateUtils.truncate(new Date(range.getFrom()), Calendar.DAY_OF_MONTH);
        while (date.getTime() <= range.getTo()) {
            indexList.add("pp_log_" + DateUtils.longToDateStr(new Date().getTime(), "yyyy.MM.dd"));
            date = org.apache.commons.lang3.time.DateUtils.addDays(date, 1);
        }
        return indexList;
    }

    private Range parseRange(String period) {
        Date startTime = org.apache.commons.lang3.time.DateUtils.addDays(new Date(), -2);
        if (StringUtils.isNotEmpty(period)) {
            if (period.contains("h")) {
                startTime = org.apache.commons.lang3.time.DateUtils.addHours(new Date(), -(Integer.parseInt(period.split("h")[0])));
            } else if (period.contains("m")) {
                startTime = org.apache.commons.lang3.time.DateUtils.addMinutes(new Date(), -(Integer.parseInt(period.split("m")[0])));
            } else if (period.contains("d")) {
                startTime = org.apache.commons.lang3.time.DateUtils.addDays(new Date(), -(Integer.parseInt(period.split("d")[0])));
            }
        }
        return new Range(startTime.getTime(), new Date().getTime());
    }

    @RequestMapping(value = "/recall", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> recall(@RequestParam(name = "traceId", defaultValue = "") String traceId, @RequestParam(name = "applicationId", defaultValue = "") String applicationId) {
        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [ \n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"traceId.keyword\": \"" + traceId + "\"" +
                "          }\n" +
                "        }\n," +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"applicationId.keyword\": \"" + applicationId + "\"" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Search search = new Search.Builder(query)
                .addIndex("pp_log_" + DateUtils.longToDateStr(new Date().getTime(), "yyyy.MM.") + "*")
                .build();
        HashMap res = new HashMap();

        EsTransaction transaction = null;
        try {
            transaction = jestClient.execute(search).getSourceAsObject(EsTransaction.class);
            if (transaction == null) {
                res.put("message", "traceId not found!");
                return res;
            }
            Attachment attachment = transaction.getAttachment();

            Map<String, String> params = new HashMap<>();
            params.put("userId", transaction.getUserId());
            params.put("traceId", "pp" + MD5Hash.getMD5AsHex((attachment.getTraceId() + new Date().getTime()).getBytes()));
            Object[] args = new Object[0];
            String[] argTypes = new String[0];
            if (StringUtils.isNotEmpty(transaction.getArgs())) {
                args = gson.fromJson(StringEscapeUtils.unescapeJson(transaction.getArgs()), Object[].class);
                argTypes = gson.fromJson(StringEscapeUtils.unescapeJson(transaction.getArgTypes()), String[].class);
            }
            Object result = DubboUtil.request("dubbo://" + transaction.getEndpoint() + "/" + attachment.getPath(), attachment.getVersion(), attachment.getClazz(), transaction.getRpcMethod(), params, argTypes, args);
            res.put("message", gson.toJson(result));
        } catch (Exception e) {
            e.printStackTrace();
            res.put("message", e.getMessage());
        }
        return res;
    }

    private void setMatchQuery(String name, String value, JsonArray mustArry) {
        if (StringUtils.isNotEmpty(value)) {
            JsonObject match = new JsonObject();
            JsonObject obj = new JsonObject();
            obj.addProperty(name, value);
            match.add("match", obj);
            mustArry.add(match);
        }
    }

    private void setMultiMatchQuery(String[] fields, String value, JsonArray shouldJson) {
        if (StringUtils.isNotEmpty(value)) {
            JsonObject jsonObject = new JsonObject();
            JsonObject match = new JsonObject();
            JsonArray arr = new JsonArray();
            for (String s : fields) {
                arr.add(s);
            }
            match.addProperty("query", value);
            match.add("fields", arr);
            jsonObject.add("multi_match", match);
            shouldJson.add(jsonObject);
        }
    }

    private void setFilterQuery(String name, Range value, JsonObject filter) {
        JsonObject valueJson = new JsonObject();
        JsonObject rangeJson = new JsonObject();
        valueJson.addProperty("gte", value.getFrom());
        valueJson.addProperty("lte", value.getTo());
        rangeJson.add(name, valueJson);
        filter.add("range", rangeJson);
    }

}