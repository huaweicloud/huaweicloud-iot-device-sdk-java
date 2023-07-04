package com.huaweicloud.sdk.iot.device.client.handler;

import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandHandler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(CommandHandler.class);

    private final DeviceClient deviceClient;

    public CommandHandler(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        String topic = message.getTopic();
        String requestId = IotUtil.getRequestId(topic);

        Command command = JsonUtil.convertJsonStringToObject(message.toString(), Command.class);
        if (command == null) {
            log.error("invalid command");
            return;

        }

        if (deviceClient.getCommandListener() != null && (command.getDeviceId() == null || command.getDeviceId()
            .equals(deviceClient.getDeviceId()))) {
            deviceClient.getCommandListener().onCommand(requestId, command.getServiceId(),
                command.getCommandName(), command.getParas());
            return;
        }

        deviceClient.getDevice().onCommand(requestId, command);

    }

}
