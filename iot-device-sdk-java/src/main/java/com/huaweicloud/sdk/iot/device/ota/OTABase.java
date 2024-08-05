package com.huaweicloud.sdk.iot.device.ota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

public class OTABase {
    @JsonProperty("task_id")
    private String taskId;

    @JsonProperty("sub_device_count")
    private Integer subDeviceCount;

    @JsonProperty("task_ext_info")
    private Object taskExtInfo;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getSubDeviceCount() {
        return subDeviceCount;
    }

    public void setSubDeviceCount(Integer subDeviceCount) {
        this.subDeviceCount = subDeviceCount;
    }

    public Object getTaskExtInfo() {
        return taskExtInfo;
    }

    public void setTaskExtInfo(Object taskExtInfo) {
        this.taskExtInfo = taskExtInfo;
    }

    @Override
    public String toString() {
        return "OTABase{" +
            "taskId='" + taskId + '\'' +
            ", subDeviceCount='" + subDeviceCount + '\'' +
            ", taskExtInfo=" + JsonUtil.convertObject2String(taskExtInfo) +
            '}';
    }
}
