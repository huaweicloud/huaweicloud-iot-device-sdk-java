package com.huaweicloud.sdk.iot.device.client.handler;

import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertySetHandler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(PropertySetHandler.class);

    private DeviceClient deviceClient;

    public PropertySetHandler(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        String requestId = IotUtil.getRequestId(message.getTopic());

        PropsSet propsSet = JsonUtil.convertJsonStringToObject(message.toString(), PropsSet.class);
        if (propsSet == null) {
            log.error("invalid property setting");
            return;
        }

        // 只处理直连设备的，子设备的由AbstractGateway处理
        if (deviceClient.getPropertyListener() != null && (propsSet.getDeviceId() == null || propsSet.getDeviceId()
            .equals(deviceClient.getDeviceId()))) {

            deviceClient.getPropertyListener().onPropertiesSet(requestId, propsSet.getServices());
            return;

        }

        deviceClient.getDevice().onPropertiesSet(requestId, propsSet);
    }
}
