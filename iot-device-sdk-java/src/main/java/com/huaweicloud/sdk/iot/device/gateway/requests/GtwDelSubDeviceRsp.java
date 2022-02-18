package com.huaweicloud.sdk.iot.device.gateway.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GtwDelSubDeviceRsp {
    @JsonProperty("successful_devices")
    private List<String> successfulDevices;

    @JsonProperty("failed_devices")
    private List<DelSubDeviceFailedReason> failedDevices;

    public List<String> getSuccessfulDevices() {
        return successfulDevices;
    }

    public void setSuccessfulDevices(List<String> successfulDevices) {
        this.successfulDevices = successfulDevices;
    }

    public List<DelSubDeviceFailedReason> getFailedDevices() {
        return failedDevices;
    }

    public void setFailedDevices(
        List<DelSubDeviceFailedReason> failedDevices) {
        this.failedDevices = failedDevices;
    }

    @Override
    public String toString() {
        return "GtwDelSubDeviceRsp{"
            + "successfulDevices='" + successfulDevices + '\''
            + ", failedDevices=" + failedDevices + '}';
    }
}
