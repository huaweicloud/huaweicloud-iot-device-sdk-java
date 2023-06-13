package com.huaweicloud.bridge.sdk.handler;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.response.ResetDeviceSecretResponse;
import com.huaweicloud.sdk.iot.device.client.handler.MessageReceivedHandler;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SecretResetHandler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(
        SecretResetHandler.class);

    private static final String NEW_SECRET = "new_secret";

    private BridgeClient bridgeClient;

    public SecretResetHandler(BridgeClient bridgeClient) {
        this.bridgeClient = bridgeClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        log.debug("received the response of the bridge resets device secret, the  message is {}", message);
        String requestId = IotUtil.getRequestId(message.getTopic());
        String deviceId = IotUtil.getDeviceId(message.getTopic());

        ResetDeviceSecretResponse resetDeviceSecretResponse = JsonUtil.convertJsonStringToObject(message.toString(),
            ResetDeviceSecretResponse.class);

        if (resetDeviceSecretResponse == null) {
            log.warn("invalid response of resetting the device secret.");
            return;
        }

        String newSecret = resetDeviceSecretResponse.getParas() == null
            ? null
            : (String) resetDeviceSecretResponse.getParas().get(NEW_SECRET);

        if (bridgeClient.getResetDeviceSecretListener() != null) {
            bridgeClient.getResetDeviceSecretListener()
                .onResetDeviceSecret(deviceId, requestId, resetDeviceSecretResponse.getResultCode(), newSecret);
        }

    }
}
