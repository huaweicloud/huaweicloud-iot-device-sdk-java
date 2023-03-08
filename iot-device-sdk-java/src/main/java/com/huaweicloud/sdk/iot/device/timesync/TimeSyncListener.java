package com.huaweicloud.sdk.iot.device.timesync;

/**
 * 监听时间同步事件
 */
public interface TimeSyncListener {
    /**
     * 时间同步响应
     * 假设设备收到的设备侧时间为device_recv_time ，则设备计算自己的准确时间为：
     * (serverRecvTime + serverSendTime + device_recv_time - deviceSendTime) / 2
     *
     * @param deviceSendTime 设备发送时间
     * @param serverRecvTime 服务端接收时间
     * @param serverSendTime 服务端响应发送时间
     */
    void onTimeSyncResponse(long deviceSendTime, long serverRecvTime, long serverSendTime);
}
