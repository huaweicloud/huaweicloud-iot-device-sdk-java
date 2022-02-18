package com.huaweicloud.sdk.iot.device.client.handler;

import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventDownHandler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(EventDownHandler.class);

    private DeviceClient deviceClient;

    public EventDownHandler(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        DeviceEvents deviceEvents = JsonUtil.convertJsonStringToObject(message.toString(), DeviceEvents.class);
        if (deviceEvents == null) {
            log.error("invalid events");
            return;
        }
        deviceClient.getDevice().onEvent(deviceEvents);
    }
}
