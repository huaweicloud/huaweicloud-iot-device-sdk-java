package com.huaweicloud.sdk.iot.device.client.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * 服务的属性
 */
public class ServiceProperty {

    /**
     * 服务id，和设备模型里一致
     */
    @JsonProperty("service_id")
    String serviceId;

    /**
     * 属性值，具体字段由设备模型定义
     */
    Map<String, Object> properties;

    /**
     * 属性变化的时间，格式：yyyyMMddTHHmmssZ，可选，不带以平台收到的时间为准
     */
    @JsonProperty("event_time")
    String eventTime;


    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public String toString() {
        return "ServiceProperty{" +
                "serviceId='" + serviceId + '\'' +
                ", properties=" + properties +
                ", eventTime='" + eventTime + '\'' +
                '}';
    }
}
