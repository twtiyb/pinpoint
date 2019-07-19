package com.navercorp.pinpoint.web.vo;

import com.google.gson.annotations.SerializedName;

/**
 * @author chunchun.xu on 2019/2/25.
 * @version 1.0
 * @descripte
 */
public class Attachment {
    private String userId;
    private String version;
    private String traceId;
    private String timeout;
    private String path;
    @SerializedName(value = "interface")
    private String clazz;
    private String group;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
