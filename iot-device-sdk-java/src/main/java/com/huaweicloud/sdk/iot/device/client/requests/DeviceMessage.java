package com.huaweicloud.sdk.iot.device.client.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

/**
 * 设备消息
 */
public class DeviceMessage {
    /**
     * 设备id，可选，默认为客户端本身的设备id
     */
    @JsonProperty("object_device_id")
    private String deviceId;

    /**
     * 消息名，可选
     */
    private String name;

    /**
     * 消息id，可选
     */
    private String id;

    /**
     * 消息具体内容
     */
    private String content;

    /**
     * 默认构造函数
     */
    public DeviceMessage() {

    }

    /**
     * 构造函数
     *
     * @param message 消息内容
     */
    public DeviceMessage(String message) {
        content = message;
    }

    /**
     * 查询设备id
     *
     * @return 设备id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 设置设备id，不设置默认为客户端的设备id
     *
     * @param deviceId 设备id
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * 查询消息名
     *
     * @return 消息名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置消息名，默认为空
     *
     * @param name 消息名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 查询消息id
     *
     * @return 消息id
     */
    public String getId() {
        return id;
    }

    /**
     * 设置消息id
     *
     * @param id 消息id，默认为空
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 查询消息内容
     *
     * @return 消息内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置消息内容
     *
     * @param content 消息内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
