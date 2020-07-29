package com.huaweicloud.sdk.iot.device.transport;

/**
 * IOT连接，代表设备和平台之间的一个连接
 */
public interface Connection {

    /**
     * 建立连接
     *
     * @return 连接建立结果，0表示成功，其他表示失败
     */
    int connect();

    /**
     * 发布消息
     *
     * @param message  消息
     * @param listener 发布监听器
     */
    void publishMessage(RawMessage message, ActionListener listener);

    /**
     * 关闭连接
     */
    void close();

    /**
     * 是否连接中
     *
     * @return true表示在连接中，false表示断连
     */
    boolean isConnected();

    /**
     * 设置连接监听器
     *
     * @param connectListener 连接监听器
     */
    void setConnectListener(ConnectListener connectListener);

    /**
     * @param topic          订阅自定义topic。注意SDK会自动订阅平台定义的topic
     * @param actionListener 监听订阅是否成功
     * @param qos            qos
     */
    void subscribeTopic(String topic, ActionListener actionListener, int qos);

}
