package com.huaweicloud.bridge.sdk.handler;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.sdk.iot.device.client.handler.MessageReceivedHandler;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeviceDisConnHandler implements MessageReceivedHandler {

    private static final Logger log = LogManager.getLogger(
        DeviceDisConnHandler.class);

    private final BridgeClient bridgeClient;

    public DeviceDisConnHandler(BridgeClient bridgeClient) {
        this.bridgeClient = bridgeClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        log.debug("received the message of the device under one bridge disconnects, the  message is {}",
            message);
        String deviceId = IotUtil.getDeviceId(message.getTopic());
        if (bridgeClient.getBridgeDeviceDisConnListener() != null) {
            bridgeClient.getBridgeDeviceDisConnListener().onDisConnect(deviceId);
        }
    }
}
