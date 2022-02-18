package com.huaweicloud.sdk.iot.device.client.listener;

import com.huaweicloud.sdk.iot.device.client.requests.CommandV3;

/**
 * 命令监听器，用于接收平台下发的V3命令
 */
public interface CommandV3Listener {
    /**
     * 处理命令
     * @param commandV3
     */
    public void onCommandV3(CommandV3 commandV3);
}
