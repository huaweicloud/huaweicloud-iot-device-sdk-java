package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.listener.PropertyListener;

import io.netty.channel.Channel;

public class Session {
    String deviceId;

    String nodeId;

    Channel channel;

    DeviceClient deviceClient;


    PropertyListener downlinkListener;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

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

    public DeviceClient getDeviceClient() {
        return deviceClient;
    }

    public void setDeviceClient(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    public PropertyListener getDownlinkListener() {
        return downlinkListener;
    }

    public void setDownlinkListener(PropertyListener downlinkListener) {
        this.downlinkListener = downlinkListener;
    }

    @Override
    public String toString() {
        return "Session{" + "deviceId='" + deviceId + '\''
            + ", nodeId='" + nodeId + '\'' + ", channel="
            + channel + '}';
    }
}
