package com.huaweicloud.sdk.iot.device.client.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.List;

public class Shadow {
    @JsonProperty("object_device_id")
    private String deviceId;

    @JsonProperty("shadow")
    private List<ShadowData> shadow;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<ShadowData> getShadow() {
        return shadow;
    }

    public void setShadow(List<ShadowData> shadow) {
        this.shadow = shadow;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
