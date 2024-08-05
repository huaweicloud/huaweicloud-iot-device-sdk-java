package com.huaweicloud.sdk.iot.device.devicerule.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.List;

public class DeviceRuleCondition {
    @JsonProperty("type")
    private String type;

    @JsonProperty("startTime")
    private String startTime;

    @JsonProperty("repeatInterval")
    private int repeatInterval;

    @JsonProperty("repeatCount")
    private int repeatCount;

    @JsonProperty("time")
    private String time;

    @JsonProperty("daysOfWeek")
    private String daysOfWeek;

    @JsonProperty("operator")
    private String operator;

    @JsonProperty("deviceInfo")
    private DeviceInfo deviceInfo;

    @JsonProperty("value")
    private String value;

    @JsonProperty("inValues")
    private List<String> inValues;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getInValues() {
        return inValues;
    }

    public void setInValues(List<String> inValues) {
        this.inValues = inValues;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
