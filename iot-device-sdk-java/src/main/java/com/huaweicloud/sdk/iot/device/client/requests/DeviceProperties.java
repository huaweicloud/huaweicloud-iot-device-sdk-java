package com.huaweicloud.sdk.iot.device.client.requests;

import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.List;

/**
 * 设备属性内容
 */
public class DeviceProperties {
    /**
     * 服务属性列表
     */
    private List<ServiceProperty> services;

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
