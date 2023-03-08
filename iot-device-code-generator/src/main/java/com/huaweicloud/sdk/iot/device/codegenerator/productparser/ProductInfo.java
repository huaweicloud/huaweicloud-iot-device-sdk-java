package com.huaweicloud.sdk.iot.device.codegenerator.productparser;

import java.util.Map;

public class ProductInfo {

    private DeviceCapability deviceCapability;

    private Map<String, DeviceService> serviceCapabilityMap;

    public DeviceCapability getDeviceCapability() {
        return deviceCapability;
    }

    void setDeviceCapability(DeviceCapability deviceCapability) {
        this.deviceCapability = deviceCapability;
    }

    public Map<String, DeviceService> getServiceCapabilityMap() {
        return serviceCapabilityMap;
    }

    void setServiceCapabilityMap(Map<String, DeviceService> serviceCapabilityMap) {
        this.serviceCapabilityMap = serviceCapabilityMap;
    }

    @Override
    public String toString() {
        return "ProductInfo{" + "deviceCapability=" + deviceCapability
            + ", serviceCapabilityMap=" + serviceCapabilityMap + '}';
    }
}
