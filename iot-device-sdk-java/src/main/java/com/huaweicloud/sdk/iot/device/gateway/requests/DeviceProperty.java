package com.huaweicloud.sdk.iot.device.gateway.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;

import java.util.List;

/**
 * 设备属性
 */
public class DeviceProperty {

    @JsonProperty("device_id")
    String deviceId;

    List<ServiceProperty> services;

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
}
