package com.huaweicloud.sdk.iot.device.client.handler;

import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;

public class ShadowResponseHandler implements MessageReceivedHandler {
    private final DeviceClient deviceClient;

    public ShadowResponseHandler(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        deviceClient.getRequestManager().onRequestResponse(message);
    }
}
