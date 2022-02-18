package com.huaweicloud.sdk.iot.device.client.handler;

import com.huaweicloud.sdk.iot.device.transport.RawMessage;

public interface MessageReceivedHandler {
    void messageHandler(RawMessage message);
}
