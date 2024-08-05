package com.huaweicloud.sdk.iot.device.devicerule.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

public class DeviceRuleAction {
    @JsonProperty("type")
    private String type;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("command")
    private DeviceRuleCommand command;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DeviceRuleCommand getCommand() {
        return command;
    }

    public void setCommand(DeviceRuleCommand command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
