package com.huaweicloud.sdk.iot.device.client.listener;


import java.util.Map;

/**
 * 命令监听器，用于直连设备接收平台下发的命令
 */
public interface CommandListener {


    /**
     * 命令处理
     *
     * @param requestId   请求id
     * @param serviceId   服务id
     * @param commandName 命令名
     * @param paras       命令参数
     */
    public void onCommand(String requestId, String serviceId, String commandName, Map<String, Object> paras);
}
