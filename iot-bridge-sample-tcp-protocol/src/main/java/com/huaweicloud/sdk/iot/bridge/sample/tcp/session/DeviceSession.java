package com.huaweicloud.sdk.iot.bridge.sample.tcp.session;

import io.netty.channel.Channel;

/**
 * 设备会话信息
 */
public class DeviceSession {

    private static final int MAX_FLOW_NO = 0xFFFF;

    private String deviceId;

    private Channel channel;

    private int seqId;

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

    public int getAndUpdateSeqId() {
        if (seqId >= MAX_FLOW_NO - 1) {
            seqId = 0;
        } else {
            seqId++;
        }
        return seqId;
    }

}
