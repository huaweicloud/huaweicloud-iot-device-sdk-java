package com.huaweicloud.sdk.iot.bridge.sample.tcp.dto;

/**
 * 通用响应消息
 */
public class CommonResponse extends BaseMessage {

    // 响应码
    private int resultCode;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }
}
