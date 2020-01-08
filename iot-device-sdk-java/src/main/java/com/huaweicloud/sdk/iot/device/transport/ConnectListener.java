package com.huaweicloud.sdk.iot.device.transport;

/**
 * 连接监听器
 */
public interface ConnectListener {

    /**
     * 连接丢失通知
     *
     * @param cause 连接丢失原因
     */
    public void connectionLost(Throwable cause);

    /**
     * 连接成功通知
     *
     * @param reconnect 是否为重连
     * @param serverURI 服务端地址
     */
    public void connectComplete(boolean reconnect, String serverURI);
}
