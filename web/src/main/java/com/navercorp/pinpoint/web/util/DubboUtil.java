package com.navercorp.pinpoint.web.util;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.service.GenericService;

import java.util.Map;

/**
 * @author chunchun.xu on 2019/2/22.
 * @version 1.0
 * @descripte
 */
public class DubboUtil {
    public static Object request(String url, String version, String clazz, String methodName, Map<String, String> attachments,String[] argTypes, Object... parms) throws Exception {
        ReferenceConfig<GenericService> consumer;
        consumer = new ReferenceConfig<>();
        consumer.setApplication(new ApplicationConfig("consumer"));
        consumer.setInterface(clazz);
        consumer.setUrl(url);
        consumer.setGeneric(true);
        consumer.setProtocol("dubbo");
        consumer.setVersion(version);
        consumer.setGroup("impl");
        RpcContext.getContext().setAttachments(attachments);
        Object result;
        try {
            result = consumer.get().$invoke(methodName, argTypes, parms);
        } finally {
            consumer.destroy();
        }
        return result;
    }
}
