package com.huaweicloud.sdk.iot.device.client.handler;

import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageHandler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(MessageHandler.class);

    private final DeviceClient deviceClient;

    public MessageHandler(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        DeviceMessage deviceMessage = JsonUtil.convertJsonStringToObject(message.toString(),
            DeviceMessage.class);
        if (deviceMessage == null) {
            log.error("invalid deviceMessage: " + message.toString());
            return;
        }

        if (deviceClient.getDeviceMessageListener() != null && (deviceMessage.getDeviceId() == null
            || deviceMessage.getDeviceId()
            .equals(deviceClient.getDeviceId()))) {
            deviceClient.getDeviceMessageListener().onDeviceMessage(deviceMessage);
            return;
        }
        deviceClient.getDevice().onDeviceMessage(deviceMessage);
    }
}
