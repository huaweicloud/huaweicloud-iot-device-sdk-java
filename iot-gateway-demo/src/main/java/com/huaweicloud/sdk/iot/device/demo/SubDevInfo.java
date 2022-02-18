package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;

import java.util.Map;

public class SubDevInfo {
    private long version;

    private Map<String, DeviceInfo> subdevices;

    long getVersion() {
        return version;
    }

    void setVersion(long version) {
        this.version = version;
    }

    Map<String, DeviceInfo> getSubdevices() {
        return subdevices;
    }

    void setSubdevices(Map<String, DeviceInfo> subdevices) {
        this.subdevices = subdevices;
    }

    @Override
    public String toString() {
        return "SubDevInfo{"
            + "version=" + version
            + ", subdevices=" + subdevices
            + '}';
    }
}
