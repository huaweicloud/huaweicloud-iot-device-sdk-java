package com.huaweicloud.sdk.iot.device.client.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


/**
 * 服务的事件
 */
public class DeviceEvent {

    /**
     * 事件所属服务id
     */
    @JsonProperty("service_id")
    String serviceId;

    /**
     * 事件类型
     */
    @JsonProperty("event_type")
    String eventType;

    /**
     * 事件发生的事件
     */
    @JsonProperty("event_time")
    String eventTime;

    /**
     * 事件具体的参数
     */
    Map<String, Object> paras;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public Map<String, Object> getParas() {
        return paras;
    }

    public void setParas(Map<String, Object> paras) {
        this.paras = paras;
    }
}
