package com.huaweicloud.sdk.iot.device.gateway.requests;

import java.util.List;

/**
 * 子设备信息
 */
public class SubDevicesInfo {
    private List<DeviceInfo> devices;

    private long version;

    public List<DeviceInfo> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceInfo> devices) {
        this.devices = devices;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
