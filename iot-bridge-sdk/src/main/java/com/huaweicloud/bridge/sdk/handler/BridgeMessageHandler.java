package com.huaweicloud.bridge.sdk.handler;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.constants.BridgeSDKConstants;
import com.huaweicloud.sdk.iot.device.client.handler.MessageReceivedHandler;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BridgeMessageHandler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(BridgeMessageHandler.class);

    private final BridgeClient bridgeClient;

    public BridgeMessageHandler(BridgeClient bridgeClient) {
        this.bridgeClient = bridgeClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        DeviceMessage deviceMessage = JsonUtil.convertJsonStringToObject(message.toString(),
            DeviceMessage.class);
        if (deviceMessage == null) {
            log.warn("the invalid device message is : {}", message);
            return;
        }

        String topic = message.getTopic();

        if (!topic.contains(BridgeSDKConstants.BRIDGE_TOPIC_KEYWORD)) {
            log.error("invalid topic. ");
        }

        // 处理网桥相关的消息
        String deviceId = IotUtil.getDeviceId(topic);
        if (bridgeClient.getBridgeDeviceMessageListener() != null) {
            bridgeClient.getBridgeDeviceMessageListener().onDeviceMessage(deviceId, deviceMessage);
        }

    }
}
