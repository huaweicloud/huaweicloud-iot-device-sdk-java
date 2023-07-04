package com.huaweicloud.bridge.sdk.handler;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.constants.BridgeSDKConstants;
import com.huaweicloud.sdk.iot.device.client.handler.MessageReceivedHandler;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BridgePropertyGetHandler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(BridgePropertyGetHandler.class);

    private final BridgeClient bridgeClient;

    public BridgePropertyGetHandler(BridgeClient bridgeClient) {
        this.bridgeClient = bridgeClient;
    }

    @Override
    public void messageHandler(RawMessage message) {

        PropsGet propsGet = JsonUtil.convertJsonStringToObject(message.toString(), PropsGet.class);
        if (propsGet == null) {
            log.warn("invalid property getting");
            return;
        }
        String topic = message.getTopic();

        if (!topic.contains(BridgeSDKConstants.BRIDGE_TOPIC_KEYWORD)) {
            log.error("invalid topic. ");
        }
        // 网桥属性查询处理逻辑
        if (bridgeClient.getBridgePropertyListener() != null) {
            String deviceId = IotUtil.getDeviceId(topic);
            String requestId = IotUtil.getRequestId(message.getTopic());
            String serviceId = propsGet.getServiceId();
            bridgeClient.getBridgePropertyListener().onPropertiesGet(deviceId, requestId, serviceId);
        }
    }
}
