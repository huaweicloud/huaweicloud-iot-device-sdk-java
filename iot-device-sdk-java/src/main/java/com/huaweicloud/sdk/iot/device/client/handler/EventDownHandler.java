package com.huaweicloud.sdk.iot.device.client.handler;

import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventDownHandler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(EventDownHandler.class);

    private static final String BRIDGE_TOPIC_KEYWORD = "$oc/bridges/";

    private final DeviceClient deviceClient;

    public EventDownHandler(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        String topic = message.getTopic();
        DeviceEvents deviceEvents = JsonUtil.convertJsonStringToObject(message.toString(), DeviceEvents.class);
        if (deviceEvents == null || topic == null) {
            log.error("invalid events");
            return;
        }

        // 网桥事件处理
        if (topic.contains(BRIDGE_TOPIC_KEYWORD)) {
            String deviceId = IotUtil.getDeviceId(topic);
            deviceClient.getDevice().onBridgeEvent(deviceId, deviceEvents);
            return;
        }

        deviceClient.getDevice().onEvent(deviceEvents);
    }
}
