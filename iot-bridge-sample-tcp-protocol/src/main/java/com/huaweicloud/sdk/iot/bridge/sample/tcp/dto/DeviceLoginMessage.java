package com.huaweicloud.sdk.iot.bridge.sample.tcp.dto;

/**
 * 设备登录消息
 */
public class DeviceLoginMessage extends BaseMessage {

    // 设备鉴权码
    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
