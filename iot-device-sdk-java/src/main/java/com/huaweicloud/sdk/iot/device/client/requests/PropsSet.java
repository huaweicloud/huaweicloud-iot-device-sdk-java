package com.huaweicloud.sdk.iot.device.client.requests;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.List;

/**
 * 写属性操作
 */
public class PropsSet {

    @JsonProperty("object_device_id")
    private String deviceId;

    private List<ServiceProperty> services;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<ServiceProperty> getServices() {
        return services;
    }

    public void setServices(List<ServiceProperty> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
