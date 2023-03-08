package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.listener.PropertyListener;

import io.netty.channel.Channel;

public class Session {
    private String deviceId;

    private String nodeId;

    private Channel channel;

    private DeviceClient deviceClient;

    private PropertyListener downlinkListener;

    String getDeviceId() {
        return deviceId;
    }

    void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getNodeId() {
        return nodeId;
    }

    void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Channel getChannel() {
        return channel;
    }

    void setChannel(Channel channel) {
        this.channel = channel;
    }

    DeviceClient getDeviceClient() {
        return deviceClient;
    }

    void setDeviceClient(DeviceClient deviceClient) {
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
