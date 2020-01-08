package com.huaweicloud.sdk.iot.device.client.listener;

import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;

/**
 * 设备消息监听器，用于接收平台下发的设备消息
 */
public interface DeviceMessageListener {

    /**
     * 处理平台下发的设备消息
     *
     * @param deviceMessage 设备消息内容
     */
    public void onDeviceMessage(DeviceMessage deviceMessage);
}
