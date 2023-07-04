package com.huaweicloud.sdk.iot.device.client.handler;

import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

public class PropertyGetHandler implements MessageReceivedHandler {
    private final DeviceClient deviceClient;

    public PropertyGetHandler(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        String requestId = IotUtil.getRequestId(message.getTopic());

        PropsGet propsGet = JsonUtil.convertJsonStringToObject(message.toString(), PropsGet.class);
        if (propsGet == null) {
            return;
        }

        if (deviceClient.getPropertyListener() != null && (propsGet.getDeviceId() == null || propsGet.getDeviceId()
            .equals(deviceClient.getDeviceId()))) {
            deviceClient.getPropertyListener().onPropertiesGet(requestId, propsGet.getServiceId());
            return;
        }

        deviceClient.getDevice().onPropertiesGet(requestId, propsGet);
    }
}
