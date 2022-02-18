package com.huaweicloud.sdk.iot.device.demo;

/**
 * 设备接入标识
 */
public class DeviceIdentity {

    private String deviceId;

    private String secret;

    String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
