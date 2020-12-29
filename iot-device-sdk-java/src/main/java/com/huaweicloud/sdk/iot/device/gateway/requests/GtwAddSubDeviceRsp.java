package com.huaweicloud.sdk.iot.device.gateway.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GtwAddSubDeviceRsp {

    @JsonProperty("successful_devices")
    List<AddedSubDeviceInfoRsp> successfulDevices;

    @JsonProperty("failed_devices")
    List<AddSubDeviceFailedReason> addSubDeviceFailedReasons;

    public List<AddedSubDeviceInfoRsp> getAddedSubDeviceInfoRsps() {
        return successfulDevices;
    }

    public void setAddedSubDeviceInfoRsps(List<AddedSubDeviceInfoRsp> addedSubDeviceInfoRsps) {
        this.successfulDevices = addedSubDeviceInfoRsps;
    }

    public List<AddSubDeviceFailedReason> getAddSubDeviceFailedReasons() {
        return addSubDeviceFailedReasons;
    }

    public void setAddSubDeviceFailedReasons(List<AddSubDeviceFailedReason> addSubDeviceFailedReasons) {
        this.addSubDeviceFailedReasons = addSubDeviceFailedReasons;
    }

    @Override
    public String toString() {
        return "GtwAddSubDeviceRsp{"
            + "addedSubDeviceInfoRsps=" + successfulDevices
            + ", addSubDeviceFailedReasons=" + addSubDeviceFailedReasons
            + '}';
    }
}
