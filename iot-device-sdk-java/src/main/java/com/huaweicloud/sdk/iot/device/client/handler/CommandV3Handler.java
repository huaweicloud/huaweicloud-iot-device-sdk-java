package com.huaweicloud.sdk.iot.device.client.handler;

import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.requests.CommandV3;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandV3Handler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(CommandV3Handler.class);

    private final DeviceClient deviceClient;

    public CommandV3Handler(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        CommandV3 commandV3 = JsonUtil.convertJsonStringToObject(message.toString(), CommandV3.class);
        if (commandV3 == null) {
            log.error("invalid commandV3");
            return;
        }

        if (deviceClient.getCommandV3Listener() != null) {
            deviceClient.getCommandV3Listener().onCommandV3(commandV3);
        }
    }
}
