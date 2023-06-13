package com.huaweicloud.bridge.sdk.request;

import com.huaweicloud.sdk.iot.device.client.requests.Command;

/**
 * 网桥设备命令
 */
public class BridgeCommand {

    private String deviceId;

    private Command command;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "BridgeCommand{" + "deviceId='" + deviceId + '\'' + ", command=" + command + '}';
    }
}
