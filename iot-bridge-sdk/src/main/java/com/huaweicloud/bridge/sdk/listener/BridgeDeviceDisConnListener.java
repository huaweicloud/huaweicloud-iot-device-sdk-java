package com.huaweicloud.bridge.sdk.listener;

public interface BridgeDeviceDisConnListener {

    /**
     * 网桥设备断链通知处理
     *
     * @param deviceId 设备Id
     */
    void onDisConnect(String deviceId);
}
