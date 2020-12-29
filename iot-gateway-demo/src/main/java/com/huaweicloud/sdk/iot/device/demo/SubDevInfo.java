package com.huaweicloud.sdk.iot.device.demo;


import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;

import java.util.Map;

public class SubDevInfo {

    long version;

    Map<String, DeviceInfo> subdevices;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Map<String, DeviceInfo> getSubdevices() {
        return subdevices;
    }

    public void setSubdevices(Map<String, DeviceInfo> subdevices) {
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
