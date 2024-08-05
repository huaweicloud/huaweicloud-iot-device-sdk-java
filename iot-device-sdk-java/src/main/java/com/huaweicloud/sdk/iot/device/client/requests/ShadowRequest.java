package com.huaweicloud.sdk.iot.device.client.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

public class ShadowRequest {
    @JsonProperty("object_device_id")
    private String deviceId;

    @JsonProperty("service_id")
    private String serviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
