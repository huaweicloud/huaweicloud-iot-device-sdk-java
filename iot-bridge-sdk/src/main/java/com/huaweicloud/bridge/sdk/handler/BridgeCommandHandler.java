package com.huaweicloud.bridge.sdk.handler;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.constants.BridgeSDKConstants;
import com.huaweicloud.bridge.sdk.request.BridgeCommand;
import com.huaweicloud.sdk.iot.device.client.handler.MessageReceivedHandler;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BridgeCommandHandler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(BridgeCommandHandler.class);

    private BridgeClient bridgeClient;

    public BridgeCommandHandler(BridgeClient bridgeClient) {
        this.bridgeClient = bridgeClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        String topic = message.getTopic();
        String requestId = IotUtil.getRequestId(topic);

        Command command = JsonUtil.convertJsonStringToObject(message.toString(), Command.class);
        if (command == null) {
            log.warn("the invalid command");
            return;
        }

        if (!topic.contains(BridgeSDKConstants.BRIDGE_TOPIC_KEYWORD)) {
            log.error("invalid topic. ");
        }

        // 网桥命令处理逻辑
        if (bridgeClient.getBridgeCommandListener() != null) {
            BridgeCommand bridgeCommand = new BridgeCommand();
            bridgeCommand.setCommand(command);
            String deviceId = IotUtil.getDeviceId(topic);
            bridgeClient.getBridgeCommandListener().onCommand(deviceId, requestId, bridgeCommand);
        }

    }

}
