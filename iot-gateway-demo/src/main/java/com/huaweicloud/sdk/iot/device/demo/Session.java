package com.huaweicloud.sdk.iot.device.demo;

import io.netty.channel.Channel;

public class Session {

    String nodeId;
    Channel channel;
    String deviceId;


    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "Session{" +
                "nodeId='" + nodeId + '\'' +
                ", channel=" + channel +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}
