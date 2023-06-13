package com.huaweicloud.bridge.sdk.listener;

import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;

public interface BridgeDeviceMessageListener {

    /**
     * 处理平台给网桥下发的消息
     *
     * @param deviceId      设备Id
     * @param deviceMessage 消息体
     */
    void onDeviceMessage(String deviceId, DeviceMessage deviceMessage);
}
