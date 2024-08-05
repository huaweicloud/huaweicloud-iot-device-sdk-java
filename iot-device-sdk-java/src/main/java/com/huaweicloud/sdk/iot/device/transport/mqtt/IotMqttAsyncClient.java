package com.huaweicloud.sdk.iot.device.transport.mqtt;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.internal.DisconnectedMessageBuffer;
import org.eclipse.paho.client.mqttv3.internal.HighResolutionTimer;

import java.util.concurrent.ScheduledExecutorService;

public class IotMqttAsyncClient extends MqttAsyncClient {
    public IotMqttAsyncClient(String serverURI, String clientId) throws MqttException {
        super(serverURI, clientId);
    }

    public IotMqttAsyncClient(String serverURI, String clientId,
        MqttClientPersistence persistence) throws MqttException {
        super(serverURI, clientId, persistence);
    }

    public IotMqttAsyncClient(String serverURI, String clientId, MqttClientPersistence persistence,
        MqttPingSender pingSender) throws MqttException {
        super(serverURI, clientId, persistence, pingSender);
    }

    public IotMqttAsyncClient(String serverURI, String clientId, MqttClientPersistence persistence,
        MqttPingSender pingSender, ScheduledExecutorService executorService) throws MqttException {
        super(serverURI, clientId, persistence, pingSender, executorService);
    }

    public IotMqttAsyncClient(String serverURI, String clientId, MqttClientPersistence persistence,
        MqttPingSender pingSender, ScheduledExecutorService executorService,
        HighResolutionTimer highResolutionTimer) throws MqttException {
        super(serverURI, clientId, persistence, pingSender, executorService, highResolutionTimer);
    }

    public void setDisconnectedMessageBuffer(DisconnectedMessageBuffer disconnectedMessageBuffer) {
        this.comms.setDisconnectedMessageBuffer(disconnectedMessageBuffer);
    }
}
