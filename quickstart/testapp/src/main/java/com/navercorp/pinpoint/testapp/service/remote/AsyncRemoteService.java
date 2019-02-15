
package com.navercorp.pinpoint.testapp.service.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class AsyncRemoteService {
    @Autowired
    @Qualifier("httpRemoteService")
    RemoteService remoteService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public AsyncRemoteService() {
    }

    @Async
    public Date getByAsync(String url) throws Exception {
        Thread.sleep(3000L);
        this.doGetHttp(url, Map.class);
        return new Date();
    }

    private <R> R doGetHttp(String url, Class<R> responseType) throws Exception {
        return this.remoteService.get(url, responseType);
    }

    @Async
    public Date getByDeepAsync(String url) throws Exception {
        Thread.sleep(3000L);
        this.doGetHttp(url, Map.class);
        return this.getByAsync(url);
    }
}
