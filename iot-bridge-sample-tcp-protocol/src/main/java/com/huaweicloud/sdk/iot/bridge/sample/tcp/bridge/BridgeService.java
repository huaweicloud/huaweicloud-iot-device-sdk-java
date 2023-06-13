package com.huaweicloud.sdk.iot.bridge.sample.tcp.bridge;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.bootstrap.BridgeBootstrap;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.handler.DownLinkHandler;

/**
 * 网桥初始化：初始化同IoT平台的连接，设置平台下行数据监听
 */
public class BridgeService {

    private static BridgeClient bridgeClient;

    public void init() {

        // 网桥启动初始化
        BridgeBootstrap bridgeBootstrap = new BridgeBootstrap();

        // 从环境变量获取配置进行初始化
        bridgeBootstrap.initBridge();

        bridgeClient = bridgeBootstrap.getBridgeDevice().getClient();

        // 设置平台下行数据监听器
        DownLinkHandler downLinkHandler = new DownLinkHandler();
        bridgeClient.setBridgeCommandListener(downLinkHandler)   // 设置平台命令下发监听器
            .setBridgeDeviceMessageListener(downLinkHandler)    // 设置平台消息下发监听器
            .setBridgeDeviceDisConnListener(downLinkHandler);   // 设置平台通知网桥主动断开设备连接的监听器
    }

    public static BridgeClient getBridgeClient() {
        return bridgeClient;
    }
}
