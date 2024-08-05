package com.huaweicloud.sdk.iot.device.devicerule.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.List;

public class DeviceRuleEventInfo {
    @JsonProperty("rulesInfos")
    private List<DeviceRuleInfo> ruleInfos;

    public List<DeviceRuleInfo> getRuleInfos() {
        return ruleInfos;
    }

    public void setRuleInfos(List<DeviceRuleInfo> ruleInfos) {
        this.ruleInfos = ruleInfos;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
