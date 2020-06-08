package com.huaweicloud.sdk.iot.device.client.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.List;

/**
 * 设备上报数据格式（V3接口）
 */
public class DevicePropertiesV3 {
    /**
     * 消息类型
     */
    @JsonView
    private String msgType;

    /**
     * 上报的属性列表
     */
    @JsonProperty("data")
    private List<ServiceData> serviceDatas;

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public List<ServiceData> getServiceDatas() {
        return serviceDatas;
    }

    public void setServiceDatas(List<ServiceData> serviceDatas) {
        this.serviceDatas = serviceDatas;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
