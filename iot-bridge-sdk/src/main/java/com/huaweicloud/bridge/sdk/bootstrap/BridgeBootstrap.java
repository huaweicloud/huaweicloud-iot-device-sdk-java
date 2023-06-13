package com.huaweicloud.bridge.sdk.bootstrap;

import com.huaweicloud.bridge.sdk.BridgeDevice;
import com.huaweicloud.sdk.iot.device.client.ClientConf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 网桥启动初始化
 */
public class BridgeBootstrap {
    private static final Logger log = LogManager.getLogger(BridgeBootstrap.class);

    // 网桥模式
    private static final int CONNECT_OF_BRIDGE_MODE = 3;

    private BridgeDevice bridgeDevice;

    /**
     * 从环境变量获取网桥配置信息，初始化网桥。
     */
    public void initBridge() {
        BridgeClientConf conf = BridgeClientConf.fromEnv();
        initBridge(conf);
    }

    /**
     * 根据网桥配置信息，初始化网桥
     *
     * @param conf 网桥配置
     */
    public void initBridge(BridgeClientConf conf) {
        if (conf == null) {
            conf = BridgeClientConf.fromEnv();
        }
        bridgeOnline(conf);
    }

    private void bridgeOnline(BridgeClientConf conf) {
        ClientConf clientConf = new ClientConf();
        if (conf.getServerIp() != null && conf.getServerPort() != null) {
            clientConf.setServerUri("ssl://" + conf.getServerIp() + ":" + conf.getServerPort());
        }
        clientConf.setDeviceId(conf.getBridgeId());
        clientConf.setSecret(conf.getBridgeSecret());
        clientConf.setMode(CONNECT_OF_BRIDGE_MODE);
        clientConf.setFile(conf.getCaFilePath());

        BridgeDevice bridgeDev = BridgeDevice.getInstance(clientConf);
        if (bridgeDev.init() != 0) {
            log.error("Bridge can't login. please check!");
        }
        this.bridgeDevice = bridgeDev;
    }

    public BridgeDevice getBridgeDevice() {
        return bridgeDevice;
    }

}
