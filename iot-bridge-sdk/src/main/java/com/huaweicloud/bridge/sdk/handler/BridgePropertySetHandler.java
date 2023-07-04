package com.huaweicloud.bridge.sdk.handler;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.constants.BridgeSDKConstants;
import com.huaweicloud.sdk.iot.device.client.handler.MessageReceivedHandler;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class BridgePropertySetHandler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(BridgePropertySetHandler.class);

    private final BridgeClient bridgeClient;

    public BridgePropertySetHandler(BridgeClient bridgeClient) {
        this.bridgeClient = bridgeClient;
    }

    @Override
    public void messageHandler(RawMessage message) {

        PropsSet propsSet = JsonUtil.convertJsonStringToObject(message.toString(), PropsSet.class);
        if (propsSet == null) {
            log.warn("invalid property setting");
            return;
        }
        String topic = message.getTopic();

        if (!topic.contains(BridgeSDKConstants.BRIDGE_TOPIC_KEYWORD)) {
            log.error("invalid topic. ");
        }
        // 网桥属性设置处理逻辑
        if (bridgeClient.getBridgePropertyListener() != null) {
            List<ServiceProperty> services = propsSet.getServices();
            String deviceId = IotUtil.getDeviceId(topic);
            String requestId = IotUtil.getRequestId(message.getTopic());

            bridgeClient.getBridgePropertyListener().onPropertiesSet(deviceId, requestId, services);
        }

    }
}
