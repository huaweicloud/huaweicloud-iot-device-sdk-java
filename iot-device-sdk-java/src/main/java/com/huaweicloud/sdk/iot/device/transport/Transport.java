package com.huaweicloud.sdk.iot.device.transport;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.transport.mqtt.MqttConnection;

/**
 * IOT传输类，提供传输层能力
 */
public class Transport {

    private Connection connection;

    /**
     * 构造函数
     *
     * @param clientConf 客户端配置
     */
    public Transport(ClientConf clientConf) {

        if (clientConf.getProtocol() == null || clientConf.getProtocol().equalsIgnoreCase("mqtt")) {
            connection = new MqttConnection(clientConf);
        }
    }

    /**
     * 建立连接
     *
     * @return 执行结果，0表示成功
     */
    public int connect() {

        if (!connection.isConnected()) {
            return connection.connect();
        }

        return 0;
    }

    /**
     * 发布消息
     *
     * @param message  消息
     * @param listener 发布监听器
     */
    public void publishMsg(RawMessage message, ActionListener listener) {
        connection.publishMessage(message, listener);

    }

    /**
     * 关闭连接
     */
    public void close() {

        connection.close();
    }

    /**
     * 设置连接监听器
     *
     * @param connectListener 连接监听器
     */
    public void setConnectListener(ConnectListener connectListener) {
        connection.setConnectListener(connectListener);
    }


    /**
     * 设置消息监听器
     *
     * @param rawMessageListener 消息监听器
     */
    public void setMessageListener(RawMessageListener rawMessageListener) {
        connection.setRawMessageListener(rawMessageListener);
    }


    /**
     * 订阅自定义topic。注意SDK会自动订阅平台定义的topic
     *
     * @param topic    自定义topic
     * @param listener 监听器
     */
    public void subscribeTopic(String topic, ActionListener listener) {
        connection.subscribeTopic(topic, listener);
    }


}
