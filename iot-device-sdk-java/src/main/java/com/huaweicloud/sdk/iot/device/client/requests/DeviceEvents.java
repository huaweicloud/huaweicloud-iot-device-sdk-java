package com.huaweicloud.sdk.iot.device.client.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 设备事件
 */
public class DeviceEvents {
    /**
     * 设备id
     */
    @JsonProperty("object_device_id")
    private String deviceId;

    /**
     * 服务事件列表
     */
    @JsonProperty("services")
    private List<DeviceEvent> services;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<DeviceEvent> getServices() {
        return services;
    }

    public void setServices(List<DeviceEvent> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "DeviceEvents{"
            + "deviceId='" + deviceId + '\''
            + ", services=" + services + '}';
    }
}
