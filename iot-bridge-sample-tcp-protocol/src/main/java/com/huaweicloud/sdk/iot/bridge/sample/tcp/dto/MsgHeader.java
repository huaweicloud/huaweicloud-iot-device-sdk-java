package com.huaweicloud.sdk.iot.bridge.sample.tcp.dto;

/**
 * 消息头
 */
public class MsgHeader {

    // 设备号
    private String deviceId;

    // 流水号
    private String flowNo;

    // 接口消息类型
    private String msgType;

    // 通信方向 1、平台下发请求 2、设备返回平台的响应 3、设备上报请求 4、平台返回设备上报的响应
    private int direct;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFlowNo() {
        return flowNo;
    }

    public void setFlowNo(String flowNo) {
        this.flowNo = flowNo;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public int getDirect() {
        return direct;
    }

    public void setDirect(int direct) {
        this.direct = direct;
    }

}
