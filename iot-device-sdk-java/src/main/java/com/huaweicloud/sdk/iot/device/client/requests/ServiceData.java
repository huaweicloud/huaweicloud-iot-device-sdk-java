package com.huaweicloud.sdk.iot.device.client.requests;

import java.util.Map;

/**
 * 设备属性（V3）
 */
public class ServiceData {
    /**
     * 服务id，和设备模型里一致
     */
    private String serviceId;

    /**
     * 属性变化的时间，格式：yyyyMMddTHHmmssZ，可选，不带以平台收到的时间为准
     */
    private String eventTime;

    /**
     * 属性值，具体字段由设备模型定义
     */
    private Map<String, Object> serviceData;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public Map<String, Object> getServiceData() {
        return serviceData;
    }

    public void setServiceData(Map<String, Object> serviceData) {
        this.serviceData = serviceData;
    }

    @Override
    public String toString() {
        return "ServiceData{" + "serviceId='" + serviceId + '\'' + ", eventTime='"
            + eventTime + '\'' + ", serviceData=" + serviceData + '}';
    }
}
