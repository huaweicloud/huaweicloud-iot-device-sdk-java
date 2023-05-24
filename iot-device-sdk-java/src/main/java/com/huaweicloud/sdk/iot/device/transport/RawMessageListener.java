package com.huaweicloud.sdk.iot.device.transport;

/**
 * 原始消息接收监听器
 */
public interface RawMessageListener {
    /**
     * 收到消息通知
     *
     * @param message 原始消息
     */
    void onMessageReceived(RawMessage message);
}
