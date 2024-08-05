package com.huaweicloud.sdk.iot.device.devicerule.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.List;

public class DeviceRuleInfo {
    @JsonProperty("ruleId")
    private String ruleId;

    @JsonProperty("ruleName")
    private String ruleName;

    @JsonProperty("logic")
    private String logic;

    @JsonProperty("timeRange")
    private TimeRange timeRange;

    @JsonProperty("status")
    private String status;

    @JsonProperty("conditions")
    private List<DeviceRuleCondition> conditions;

    @JsonProperty("actions")
    private List<DeviceRuleAction> actions;

    @JsonProperty("ruleVersionInShadow")
    private int ruleVersionInShadow;

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getLogic() {
        return logic;
    }

    public void setLogic(String logic) {
        this.logic = logic;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(TimeRange timeRange) {
        this.timeRange = timeRange;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<DeviceRuleCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<DeviceRuleCondition> conditions) {
        this.conditions = conditions;
    }

    public List<DeviceRuleAction> getActions() {
        return actions;
    }

    public void setActions(List<DeviceRuleAction> actions) {
        this.actions = actions;
    }

    public int getRuleVersionInShadow() {
        return ruleVersionInShadow;
    }

    public void setRuleVersionInShadow(int ruleVersionInShadow) {
        this.ruleVersionInShadow = ruleVersionInShadow;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
