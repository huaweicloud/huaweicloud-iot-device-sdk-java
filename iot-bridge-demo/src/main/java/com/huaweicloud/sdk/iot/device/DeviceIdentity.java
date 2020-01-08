package com.huaweicloud.sdk.iot.device;

/**
 * 设备接入标识
 */
public class DeviceIdentity {


    String deviceId;
    String secret;


    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
