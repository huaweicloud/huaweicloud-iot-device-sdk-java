package com.huaweicloud.sdk.iot.bridge.sample.tcp;

import com.huaweicloud.sdk.iot.bridge.sample.tcp.bridge.BridgeService;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.server.TcpServer;

public class Main {

    public static void main(String[] args) {

        // 启动时初始化网桥连接
        BridgeService bridgeService = new BridgeService();
        bridgeService.init();

        // 启动TCP服务
        TcpServer tcpServer = new TcpServer();
        tcpServer.start("localhost", 8900);

    }
}
