package com.huaweicloud.sdk.iot.device.gateway.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DelSubDeviceFailedReason {
    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_msg")
    private String errorMSg;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMSg() {
        return errorMSg;
    }

    public void setErrorMSg(String errorMSg) {
        this.errorMSg = errorMSg;
    }

    @Override
    public String toString() {
        return "DelSubDeviceFailedReason{"
            + "deviceId='" + deviceId + '\''
            + ", errorCode='" + errorCode + '\''
            + ", errorMSg='" + errorMSg + '\''
            + '}';
    }
}
