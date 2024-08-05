package com.huaweicloud.sdk.iot.device.client;

import com.huaweicloud.sdk.iot.device.client.handler.CustomBackoffHandler;
import com.huaweicloud.sdk.iot.device.transport.ConnectListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;

/**
 * 自定义连接选项
 */
public class CustomOptions {
    /**
     * 是否使用自动重连功能
     */
    private boolean reConnect = true;

    /**
     * 退避系数， 默认为1000，单位毫秒
     */
    private long backoff = IotUtil.DEFAULT_BACKOFF;

    /**
     * 最小重连时间，默认为1000，单位毫秒
     */
    private long minBackoff = IotUtil.MIN_BACKOFF;

    /**
     * 最大重连时间，默认为30 * 1000，单位毫秒
     */
    private long maxBackoff = IotUtil.MAX_BACKOFF;

    private CustomBackoffHandler customBackoffHandler;

    /**
     * 正在传输但还未收到确认的消息数量，默认为65535
     */
    private int maxInflight = 65534;

    /**
     * 离线消息缓存队列大小，默认5000
     */
    private int offlineBufferSize = 5000;

    /**
     * 连接监听器，监听设备的连接状态
     */
    private ConnectListener connectListener;

    public long getBackoff() {
        return backoff;
    }

    public void setBackoff(long backoff) {
        this.backoff = backoff;
    }

    public long getMinBackoff() {
        return minBackoff;
    }

    public void setMinBackoff(long minBackoff) {
        this.minBackoff = minBackoff;
    }

    public long getMaxBackoff() {
        return maxBackoff;
    }

    public void setMaxBackoff(long maxBackoff) {
        this.maxBackoff = maxBackoff;
    }

    public boolean isReConnect() {
        return reConnect;
    }

    public void setReConnect(boolean reConnect) {
        this.reConnect = reConnect;
    }

    public CustomBackoffHandler getCustomBackoffHandler() {
        return customBackoffHandler;
    }

    public void setCustomBackoffHandler(CustomBackoffHandler customBackoffHandler) {
        this.customBackoffHandler = customBackoffHandler;
    }

    public int getMaxInflight() {
        return maxInflight;
    }

    public void setMaxInflight(int maxInflight) {
        this.maxInflight = maxInflight;
    }

    public int getOfflineBufferSize() {
        return offlineBufferSize;
    }

    public void setOfflineBufferSize(int offlineBufferSize) {
        this.offlineBufferSize = offlineBufferSize;
    }

    public ConnectListener getConnectListener() {
        return connectListener;
    }

    public void setConnectListener(ConnectListener connectListener) {
        this.connectListener = connectListener;
    }
}
