package com.huaweicloud.sdk.iot.device.client.listener;

import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class DefaultPublishListenerImpl implements IMqttActionListener {
    private static final Logger log = LogManager.getLogger(DefaultActionListenerImpl.class);

    private final ActionListener listener;
    private final RawMessage message;

    public DefaultPublishListenerImpl(ActionListener listener,
        RawMessage message) {
        this.listener = listener;
        this.message = message;
    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        if (listener != null) {
            listener.onSuccess(null);
        }
    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        log.error("publish message failed  " + message);
        if (listener != null) {
            listener.onFailure(null, throwable);
        }
    }
}
