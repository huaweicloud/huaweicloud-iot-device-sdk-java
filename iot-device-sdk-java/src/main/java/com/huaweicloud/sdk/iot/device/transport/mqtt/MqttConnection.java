/*
 * Copyright (c) 2020-2023 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.huaweicloud.sdk.iot.device.transport.mqtt;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.listener.DefaultPublishListenerImpl;
import com.huaweicloud.sdk.iot.device.client.listener.DefaultSubscribeListenerImpl;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.ConnectActionListener;
import com.huaweicloud.sdk.iot.device.transport.ConnectListener;
import com.huaweicloud.sdk.iot.device.transport.Connection;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessageListener;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLContext;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * mqtt连接
 */
public class MqttConnection implements Connection {
    private static final Logger log = LogManager.getLogger(MqttConnection.class);

    private static final int DEFAULT_QOS = 1;

    private static final int DEFAULT_CONNECT_TIMEOUT = 60;

    private static final int DEFAULT_KEEPLIVE = 120;

    private static final String CONNECT_TYPE_OF_DEVICE = "0";

    private static final String CONNECT_TYPE_OF_BRIDGE_DEVICE = "3";

    private static final int CONNECT_OF_BRIDGE_MODE = 3;

    private static final int MAX_FLIGHT_COUNT = 1000;

    private ClientConf clientConf;

    private boolean connectFinished = false;

    private MqttAsyncClient mqttAsyncClient;

    private ConnectListener connectListener;

    private ConnectActionListener connectActionListener;

    private RawMessageListener rawMessageListener;

    private int connectResultCode;

    public MqttConnection(ClientConf clientConf, RawMessageListener rawMessageListener) {
        this.clientConf = clientConf;
        this.rawMessageListener = rawMessageListener;
    }

    private MqttCallback callback = new MqttCallbackExtended() {

        @Override
        public void connectionLost(Throwable cause) {
            log.error("Connection lost.", cause);
            if (connectListener != null) {
                connectListener.connectionLost(cause);
            }

            IotUtil.reConnect(MqttConnection.this);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            log.info("messageArrived topic =  {}, msg = {}", topic, message.toString());
            RawMessage rawMessage = new RawMessage(topic, message.toString());
            try {
                if (rawMessageListener != null) {
                    rawMessageListener.onMessageReceived(rawMessage);
                }

            } catch (Exception e) {
                log.error(ExceptionUtil.getBriefStackTrace(e));
            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            log.info("Mqtt client connected. address is {}", serverURI);

            if (connectListener != null) {
                connectListener.connectComplete(reconnect, serverURI);
            }
        }
    };

    @Override
    public int connect() {

        try {
            this.connectFinished = false;
            String timeStamp = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
            String clientId = generateClientId(timeStamp);

            try {
                mqttAsyncClient = new MqttAsyncClient(clientConf.getServerUri(), clientId, new MemoryPersistence());
            } catch (MqttException e) {
                log.error(ExceptionUtil.getBriefStackTrace(e));
            }

            DisconnectedBufferOptions bufferOptions = new DisconnectedBufferOptions();
            bufferOptions.setBufferEnabled(true);
            if (clientConf.getOfflineBufferSize() != null) {
                bufferOptions.setBufferSize(clientConf.getOfflineBufferSize());
            }
            if (createMqttConnection(timeStamp, bufferOptions)) {
                return -1;
            }

            synchronized (this) {
                while (!connectFinished) {
                    try {
                        wait(DEFAULT_CONNECT_TIMEOUT * 1000);
                    } catch (InterruptedException e) {
                        log.error(ExceptionUtil.getBriefStackTrace(e));
                    }
                }
            }

        } catch (MqttException e) {
            log.error("connect error, the deviceId is {}. exception is {}", clientConf.getDeviceId(),
                ExceptionUtil.getBriefStackTrace(e));
        }

        if (mqttAsyncClient.isConnected()) {
            return 0;
        }

        // 处理paho返回的错误码为0的异常
        if (connectResultCode == 0) {
            log.error("Client encountered an exception");
            return -1;
        }

        return connectResultCode;
    }

    private boolean createMqttConnection(String timeStamp, DisconnectedBufferOptions bufferOptions)
        throws MqttException {
        mqttAsyncClient.setBufferOpts(bufferOptions);

        MqttConnectOptions options = new MqttConnectOptions();
        if (clientConf.getServerUri().contains("ssl:")) {
            try {
                SSLContext sslContext = IotUtil.getSSLContext(clientConf);
                options.setSocketFactory(sslContext.getSocketFactory());
                options.setHttpsHostnameVerificationEnabled(false);
            } catch (Exception e) {
                log.error(ExceptionUtil.getBriefStackTrace(e));
                return true;
            }
        }

        options.setHttpsHostnameVerificationEnabled(false);
        options.setCleanSession(true);
        options.setUserName(clientConf.getDeviceId());
        options.setMaxInflight(MAX_FLIGHT_COUNT);

        String secret = clientConf.getSecret();

        if (secret != null && !secret.isEmpty()) {
            String passWord = IotUtil.shaHMac(secret, timeStamp, clientConf.getCheckStamp());
            Optional.ofNullable(passWord).ifPresent(s -> options.setPassword(s.toCharArray()));
        }

        options.setConnectionTimeout(DEFAULT_CONNECT_TIMEOUT);
        options.setKeepAliveInterval(DEFAULT_KEEPLIVE);
        options.setAutomaticReconnect(false);
        mqttAsyncClient.setCallback(callback);

        log.info("try to connect to {}", clientConf.getServerUri());

        mqttAsyncClient.connect(options, null, getCallback());
        return false;
    }

    private String generateClientId(String timeStamp) {
        String clientId;
        if (clientConf.getMode() == CONNECT_OF_BRIDGE_MODE) {
            clientId = String.join("_", clientConf.getDeviceId(), CONNECT_TYPE_OF_BRIDGE_DEVICE, Integer.toString(clientConf.getCheckStamp()),
                timeStamp);
        } else if (clientConf.getScopeId() != null) {
            clientId = String.join("_", clientConf.getDeviceId(), CONNECT_TYPE_OF_DEVICE, clientConf.getScopeId());
        } else {
            clientId = String.join("_", clientConf.getDeviceId(), CONNECT_TYPE_OF_DEVICE, Integer.toString(clientConf.getCheckStamp()), timeStamp);
        }
        return clientId;
    }

    private IMqttActionListener getCallback() {
        return new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {

                log.info("connect success, the uri is {}", clientConf.getServerUri());

                if (connectActionListener != null) {
                    connectActionListener.onSuccess(iMqttToken);
                }

                synchronized (MqttConnection.this) {
                    connectFinished = true;
                    MqttConnection.this.notifyAll();
                }
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                log.info("connect failed, the reason is {}", throwable.toString());
                if(throwable instanceof MqttException) {
                    MqttException me = (MqttException) throwable;
                    connectResultCode = me.getReasonCode();
                }

                if (connectActionListener != null) {
                    connectActionListener.onFailure(iMqttToken, throwable);
                }

                synchronized (MqttConnection.this) {
                    connectFinished = true;
                    MqttConnection.this.notifyAll();
                }
            }
        };
    }

    @Override
    public void publishMessage(RawMessage message, ActionListener listener) {

        try {
            MqttMessage mqttMessage = new MqttMessage(message.getPayload());
            mqttMessage.setQos(message.getQos() == 0 ? 0 : DEFAULT_QOS);

            DefaultPublishListenerImpl defaultPublishListener = new DefaultPublishListenerImpl(listener, message);

            mqttAsyncClient.publish(message.getTopic(), mqttMessage, message.getTopic(), defaultPublishListener);
            log.info("publish message topic is {}, msg =  {}", message.getTopic(), message.toString());
        } catch (MqttException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            if (listener != null) {
                listener.onFailure(null, e);
            }
        }
    }

    public void close() {

        if (mqttAsyncClient.isConnected()) {
            try {
                mqttAsyncClient.disconnect();
            } catch (MqttException e) {
                log.error(ExceptionUtil.getBriefStackTrace(e));
            }
        }
    }

    @Override
    public boolean isConnected() {
        if (mqttAsyncClient == null) {
            return false;
        }
        return mqttAsyncClient.isConnected();
    }

    @Override
    public void setConnectListener(ConnectListener connectListener) {
        this.connectListener = connectListener;
    }

    @Override
    public void setConnectActionListener(ConnectActionListener connectActionListener) {
        this.connectActionListener = connectActionListener;
    }

    public void setRawMessageListener(RawMessageListener rawMessageListener) {
        this.rawMessageListener = rawMessageListener;
    }

    /**
     * 订阅指定主题
     *
     * @param topic 主题
     */
    public void subscribeTopic(String topic, ActionListener listener, int qos) {
        DefaultSubscribeListenerImpl defaultSubscribeListener = new DefaultSubscribeListenerImpl(topic, listener);

        try {
            mqttAsyncClient.subscribe(topic, qos, null, defaultSubscribeListener);
        } catch (MqttException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            if (listener != null) {
                listener.onFailure(topic, e);
            }
        }

    }

}
