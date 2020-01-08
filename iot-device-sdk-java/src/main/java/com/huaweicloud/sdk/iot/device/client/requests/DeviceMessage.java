package com.huaweicloud.sdk.iot.device.client.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 设备消息
 */
public class DeviceMessage {

    /**
     * 设备id，可选，默认为客户端本身的设备id
     */
    @JsonProperty("object_device_id")
    String deviceId;

    /**
     * 消息名，可选
     */
    String name;


    /**
     * 消息id，可选
     */
    String id;


    /**
     * 消息具体内容
     */
    String content;

    public DeviceMessage() {
    }

    public DeviceMessage(String message) {
        content = message;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
