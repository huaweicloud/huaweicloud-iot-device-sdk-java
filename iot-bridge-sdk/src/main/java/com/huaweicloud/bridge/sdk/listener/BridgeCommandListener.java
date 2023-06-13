package com.huaweicloud.bridge.sdk.listener;

import com.huaweicloud.bridge.sdk.request.BridgeCommand;

public interface BridgeCommandListener {

    /**
     * 网桥命令处理
     * @param deviceId      设备Id
     * @param requestId     请求Id
     * @param bridgeCommand 网桥命令
     */
    void onCommand(String deviceId, String requestId, BridgeCommand bridgeCommand);
}
