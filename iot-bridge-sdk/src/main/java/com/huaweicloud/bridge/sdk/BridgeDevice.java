package com.huaweicloud.bridge.sdk;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.constants.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class BridgeDevice extends IoTDevice {
    private static final Logger log = LogManager.getLogger(BridgeDevice.class);

    private static BridgeDevice instance;

    private BridgeClient bridgeClient;

    private BridgeDevice(ClientConf clientConf) {
        super(clientConf);
        if (Constants.CONNECT_OF_BRIDGE_MODE != clientConf.getMode()) {
            throw new IllegalArgumentException("the bridge mode is invalid which the value should be 3.");
        }
        this.bridgeClient = new BridgeClient(clientConf, this);
    }

    // 此处采用单例模式，默认一个网桥服务，只会启动一个网桥，且网桥参数一致
    public static BridgeDevice getInstance(ClientConf clientConf) {
        if (Objects.isNull(instance)) {
            instance = new BridgeDevice(clientConf);
        }
        return instance;
    }

    @Override
    public int init() {
        log.debug("the bridge client starts to init. ");
        return bridgeClient.connect();
    }

    /**
     * 获取网桥设备客户端。获取到网桥设备客户端后，可以直接调用客户端提供的消息、属性、命令等接口
     *
     * @return 设备客户端实例
     */
    public BridgeClient getClient() {
        return this.bridgeClient;
    }

}
