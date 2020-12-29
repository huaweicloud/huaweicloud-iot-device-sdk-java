package com.huaweicloud.sdk.iot.device.demo;

import io.netty.channel.Channel;

public class Session {

    String nodeId;

    String deviceId;

    Channel channel;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "Session{"
            + "nodeId='" + nodeId + '\'' + ", channel="
            + channel + ", deviceId='" + deviceId + '\'' + '}';
    }
}
