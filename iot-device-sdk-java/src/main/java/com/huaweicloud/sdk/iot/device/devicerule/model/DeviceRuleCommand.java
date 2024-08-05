package com.huaweicloud.sdk.iot.device.devicerule.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.Map;

public class DeviceRuleCommand {
    @JsonProperty("commandName")
    private String commandName;

    @JsonProperty("serviceId")
    private String serviceId;

    @JsonProperty("commandBody")
    private Map<String, Object> commandBody;

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Map<String, Object> getCommandBody() {
        return commandBody;
    }

    public void setCommandBody(Map<String, Object> commandBody) {
        this.commandBody = commandBody;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
