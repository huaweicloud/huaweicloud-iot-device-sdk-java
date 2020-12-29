package com.huaweicloud.sdk.iot.device.transport;

import org.eclipse.paho.client.mqttv3.IMqttToken;

/**
 * 连接动作监听器
 */
public interface ConnectActionListener {

    /**
     * 连接成功
     *
     * @param iMqttToken 返回token
     */
    void onSuccess(IMqttToken iMqttToken);

    /**
     * 连接失败
     *
     * @param iMqttToken 返回token
     * @param throwable  失败异常
     */
    void onFailure(IMqttToken iMqttToken, Throwable throwable);

}
