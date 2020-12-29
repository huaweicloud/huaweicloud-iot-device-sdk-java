package com.huaweicloud.sdk.iot.device.client.listener;

import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class DefaultSubscribeListenerImpl implements IMqttActionListener {

    private static final Logger log = LogManager.getLogger(DefaultActionListenerImpl.class);
    private String topic;
    private ActionListener listener;

    public DefaultSubscribeListenerImpl(String topic, ActionListener listener) {
        this.topic = topic;
        this.listener = listener;
    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        if (listener != null) {
            listener.onSuccess(topic);
        }
    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        log.error("subscribe topic failed:" + topic);
        if (listener != null) {
            listener.onFailure(topic, throwable);
        }
    }
}
