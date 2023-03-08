package com.huaweicloud.sdk.iot.device.demo;

import io.netty.channel.Channel;

public class Session {
    private String nodeId;

    private String deviceId;

    private Channel channel;

    String getNodeId() {
        return nodeId;
    }

    void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    String getDeviceId() {
        return deviceId;
    }

    void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    Channel getChannel() {
        return channel;
    }

    void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "Session{"
            + "nodeId='" + nodeId + '\'' + ", channel="
            + channel + ", deviceId='" + deviceId + '\'' + '}';
    }
}
